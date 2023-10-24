package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.comm.ThreadFactoryImpl;
import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.store.SessionLifeCycle;
import com.jfeng.gateway.store.SessionRecord;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.JsonUtils;
import com.jfeng.gateway.util.RedisUtils;
import io.netty.buffer.ByteBufUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Redis会话监听器。
 * <p>
 * 维护在线信息、会话历史及明细。
 */
@Component
@Setter
@Slf4j
@ConditionalOnProperty(name = "spring.redis")
@Primary
public class SessionInRedisListener implements SessionListener {
    private BlockingQueue<StateChangeEvent> stateChangeEvent = new LinkedBlockingQueue<>(10000);//上下线事件
    private BlockingQueue<ConnectDetail> connectDetails = new LinkedBlockingQueue<>(10000);//连接明细
    private volatile boolean isRunning;

    /**
     * 刷新类型。0：实时刷新 1:延时刷新
     */
    @Value("${spring.redis.flushType:0}")
    private int flushType;

    /**
     * 延时刷新间隔。当flushType=1时候生效
     */
    @Value("${spring.redis.flushDelay:30}")
    private int flushDelay;

    @Autowired
    private RedisUtils redisUtils;

    @PostConstruct
    private void init() {
        isRunning = true;
        //TODO 清空在线redis
//        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("历史连接明细")).submit(() -> {
//            while (isRunning) {
//                try {
//                    //TODO 维护历史连接明细
//                    ConnectDetail connectDetail = connectDetails.take();
//
//                } catch (Exception e) {
//                    log.warn("在线列表存储异常：", e);
//                }
//            }
//        });
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("当前设备在线")).submit(() -> {
            while (isRunning) {
                try {
                    StateChangeEvent event = stateChangeEvent.take();
                    TcpSession tcpSession = event.session;

                    String onlineKey = "iot:" + tcpSession.getLocalAddress() + ":online:" + tcpSession.getDeviceId();
                    String mappingKey = Constant.ONLINE_MAPPING + tcpSession.getDeviceId();

//                    Map<String, String> oldOnlineInfo;
                    //1. 维护上一次因为异常未保存的历史连接信息
//                    if (redisUtils.hasKey(onlineKey) && ((oldOnlineInfo = redisUtils.entries(onlineKey)) != null)) {
//                        LocalDateTime createTime = DateTimeUtils2.parse(oldOnlineInfo.get(Constant.SESSION_CREATE_TIME), "yyyy-MM-dd HH:mm:ss");
//                        LocalDateTime endTime = event.deviceStatus == DeviceStatus.ONLINE ? DateTimeUtils2.parse(oldOnlineInfo.get(Constant.SESSION_LAST_REFRESH_TIME), "yyyy-MM-dd HH:mm:ss") : LocalDateTime.now();
//                        long totalMill = Duration.between(endTime, createTime).getSeconds();
//
//                        oldOnlineInfo.put(Constant.SESSION_TOTAL_MILL, String.valueOf(totalMill));
//                        connectDetails.offer(new ConnectDetail(oldOnlineInfo));
//                    }
                    //2. 维护映射关系
                    if (event.deviceStatus == DeviceStatus.ONLINE) {
                        String connectKey = Constant.SYSTEM_PREFIX + tcpSession.getTcpServer().getLocalAddress() + ":connect:" + tcpSession.getRemoteAddress();
                        redisUtils.delete(connectKey);

                        Map<String, String> mapping = new HashMap<>();
                        mapping.put(Constant.MACHINE, tcpSession.getLocalAddress());
                        mapping.put(Constant.SERVER_LAST_REFRESH_TIME, DateTimeUtils2.outNow());
                        redisUtils.putAll(mappingKey, mapping);
                    } else {
                        //如果当前所在机器不是本机,表示已在其他机器上线，不需要进行删除操作
                        Object existMachine = redisUtils.get(mappingKey, Constant.MACHINE);
                        if (Objects.equals(tcpSession.getLocalAddress(), String.valueOf(existMachine))) {
                            redisUtils.delete(mappingKey);
                        }
                    }
                } catch (Exception e) {
                    log.warn("在线列表存储异常：", e);
                }
            }
        });
    }

    @PreDestroy
    private void destroy() {
        isRunning = false;
    }

    @Override
    public void onConnect(TcpSession tcpSession) {
        String onlineKey = Constant.SESSION_CURRENT + tcpSession.getDeviceId();

        //1. 会话结构(hash)
        redisUtils.put(onlineKey, DateTimeUtils2.outString(tcpSession.getCreateTime()), JsonUtils.serialize(tcpSession));

        //2. 连接信息(list)
        SessionRecord sessionRecord = new SessionRecord();
        sessionRecord.setDataType(0);
        sessionRecord.setTime(DateTimeUtils2.outNow());
        redisUtils.leftPush(tcpSession.getDeviceId() + ":" + DateTimeUtils2.outString(tcpSession.getCreateTime()), JsonUtils.serialize(sessionRecord));
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {
        SessionRecord sessionRecord = new SessionRecord();
        sessionRecord.setDataType(1);
        sessionRecord.setData(ByteBufUtil.hexDump(data));
        sessionRecord.setTime(DateTimeUtils2.outNow());
        redisUtils.leftPush(tcpSession.getDeviceId() + ":" + DateTimeUtils2.outString(tcpSession.getCreateTime()), JsonUtils.serialize(sessionRecord));
    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        SessionRecord sessionRecord = new SessionRecord();
        sessionRecord.setDataType(2);
        sessionRecord.setData(ByteBufUtil.hexDump(data));
        sessionRecord.setTime(DateTimeUtils2.outNow());
        redisUtils.leftPush(tcpSession.getDeviceId() + ":" + DateTimeUtils2.outString(tcpSession.getCreateTime()), JsonUtils.serialize(sessionRecord));
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {
        //1.存储连接信息
        SessionRecord sessionRecord = new SessionRecord();
        sessionRecord.setDataType(3);
        sessionRecord.setData(reason);
        sessionRecord.setTime(DateTimeUtils2.outNow());
        redisUtils.leftPush(tcpSession.getDeviceId() + ":" + DateTimeUtils2.outString(tcpSession.getCreateTime()), JsonUtils.serialize(sessionRecord));
        //2.存储会话信息
        SessionLifeCycle connectLifeCycle = tcpSession.createConnectLifeCycle(false);
        redisUtils.put(Constant.SESSION_CURRENT + tcpSession.getDeviceId(), DateTimeUtils2.outString(tcpSession.getCreateTime()), JsonUtils.serialize(connectLifeCycle));
    }

    @Override
    public void online(TcpSession tcpSession) {
        stateChangeEvent.offer(new StateChangeEvent(tcpSession, DeviceStatus.ONLINE));
        tcpSession.getTcpServer().getDownInfoSave().loadWaitToSend(tcpSession);
        tcpSession.getTcpServer().getDownInfoSave().loadSending(tcpSession);
    }

    @Override
    public void offline(TcpSession tcpSession, String message) {
        stateChangeEvent.offer(new StateChangeEvent(tcpSession, DeviceStatus.OFFLINE));
    }

    private void refreshMappingLocation(TcpSession tcpSession) {
        String onlineLocationKey = Constant.ONLINE_MAPPING + tcpSession.getDeviceId();
        Map<String, String> mapping = new HashMap<>();
        mapping.put(Constant.MACHINE, tcpSession.getLocalAddress());
        mapping.put(Constant.SERVER_LAST_REFRESH_TIME, DateTimeUtils2.outNow());
        redisUtils.putAll(onlineLocationKey, mapping);
    }


    /**
     * 连接状态切换通知
     */
    class StateChangeEvent {
        /**
         * 1：上线 0:下线
         */
        private DeviceStatus deviceStatus;
        TcpSession session;

        public StateChangeEvent(TcpSession session, DeviceStatus state) {
            this.session = session;
            this.deviceStatus = state;
        }
    }

    /**
     * 设备在线状态
     */
    enum DeviceStatus {
        ONLINE(1), OFFLINE(0);

        private final int value;

        DeviceStatus(int i) {
            this.value = i;
        }
    }

    /**
     * 连接明细
     */
    class ConnectDetail {

        Map<String, String> connectInfo;

        public ConnectDetail(Map<String, String> connectInfo) {
            this.connectInfo = connectInfo;
        }
    }
}
