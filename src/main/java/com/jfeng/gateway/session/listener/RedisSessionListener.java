package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.comm.ThreadFactoryImpl;
import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.RedisUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Redis会话监听器，维护在线信息、单个设备通讯流量
 */
@Component
@Setter
@Slf4j
@ConditionalOnProperty(name = "spring.redis")
@Primary
public class RedisSessionListener implements SessionListener {
    private BlockingQueue<StateChangeEvent> stateChangeEvent = new LinkedBlockingQueue<>(10000);//上下线事件
    private BlockingQueue<ConnectDetail> connectDetails = new LinkedBlockingQueue<>(10000);//连接明细
    private volatile boolean isRunning;

    @Resource
    private RedisUtils redisUtils;

    @PostConstruct
    private void init() {
        isRunning = true;
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("历史连接明细")).submit(() -> {
            while (isRunning) {
                try {
                    //TODO 维护历史连接明细
                    ConnectDetail connectDetail = connectDetails.take();

                } catch (Exception e) {
                    log.warn("在线列表存储异常：", e);
                }
            }
        });
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("当前设备在线")).submit(() -> {
            while (isRunning) {
                try {
                    StateChangeEvent event = stateChangeEvent.take();
                    TcpSession tcpSession = event.session;

                    String onlineKey = "iot:" + tcpSession.getLocalAddress() + ":online:" + tcpSession.getDeviceId();
                    String mappingKey = Constant.ONLINE_MAPPING + tcpSession.getDeviceId();

                    Map<String, String> oldOnlineInfo;
                    //1. 维护上一次因为异常未保存的历史连接信息
                    if (redisUtils.hasKey(onlineKey) && ((oldOnlineInfo = redisUtils.entries(onlineKey)) != null)) {
                        LocalDateTime createTime = DateTimeUtils2.parse(oldOnlineInfo.get(Constant.SESSION_CREATE_TIME), "yyyy-MM-dd HH:mm:ss");
                        LocalDateTime endTime = event.deviceStatus == DeviceStatus.ONLINE ? DateTimeUtils2.parse(oldOnlineInfo.get(Constant.SESSION_LAST_REFRESH_TIME), "yyyy-MM-dd HH:mm:ss") : LocalDateTime.now();
                        long totalMill = Duration.between(endTime, createTime).getSeconds();

                        oldOnlineInfo.put(Constant.SESSION_TOTAL_MILL, String.valueOf(totalMill));
                        connectDetails.offer(new ConnectDetail(oldOnlineInfo));
                    }
                    //2. 维护映射关系
                    if (event.deviceStatus == DeviceStatus.ONLINE) {
                        String connectKey = Constant.SYSTEM_PREFIX + tcpSession.getTcpServer().getLocalAddress() + ":connect:" + tcpSession.getRemoteAddress();
                        redisUtils.delete(connectKey);

                        Map<String, String> mapping = new HashMap<>();
                        mapping.put(Constant.MACHINE, tcpSession.getLocalAddress());
                        mapping.put(Constant.SERVER_LAST_REFRESH_TIME, DateTimeUtils2.outNow());
                        redisUtils.putAll(mappingKey, mapping);
                    } else {
                        redisUtils.delete(mappingKey);
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
        String onlineKey = Constant.SYSTEM_PREFIX + tcpSession.getTcpServer().getLocalAddress() + ":connect:" + tcpSession.getRemoteAddress();

        Map<String, String> hashValue = new HashMap<>();
        hashValue.put(Constant.SESSION_CREATE_TIME, DateTimeUtils2.outString(tcpSession.getCreateTime()));
        hashValue.put(Constant.SESSION_LAST_REFRESH_TIME, DateTimeUtils2.outNow());

        redisUtils.putAll(onlineKey, hashValue);
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {
        String onlineKey = Constant.SYSTEM_PREFIX + tcpSession.getTcpServer().getLocalAddress() + ":online:" + tcpSession.getDeviceId();
        Map<String, String> batch = new HashMap<>();
        batch.put(Constant.SESSION_RECEIVE_PACKETS, String.valueOf(tcpSession.getReceivedPackets()));
        batch.put(Constant.SESSION_RECEIVE_BYTES, String.valueOf(tcpSession.getReceivedBytes()));
        batch.put(Constant.SESSION_LAST_RECEIVE_TIME, DateTimeUtils2.outString(tcpSession.getLastReadTime()));
        batch.put(Constant.SESSION_LAST_REFRESH_TIME, DateTimeUtils2.outNow());
        redisUtils.putAll(onlineKey, batch);

        refreshMappingLocation(tcpSession);
    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        String onlineKey = Constant.SYSTEM_PREFIX + tcpSession.getTcpServer().getLocalAddress() + ":online:" + tcpSession.getDeviceId();
        Map<String, String> batch = new HashMap<>();
        batch.put(Constant.SESSION_SEND_PACKETS, String.valueOf(tcpSession.getSendPackets()));
        batch.put(Constant.SESSION_SEND_BYTES, String.valueOf(tcpSession.getSendBytes()));
        batch.put(Constant.SESSION_LAST_SEND_TIME, DateTimeUtils2.outString(tcpSession.getLastWriteTime()));
        batch.put(Constant.SESSION_LAST_REFRESH_TIME, DateTimeUtils2.outNow());
        redisUtils.putAll(onlineKey, batch);

        refreshMappingLocation(tcpSession);
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {

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
