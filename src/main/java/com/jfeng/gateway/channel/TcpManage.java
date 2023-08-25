package com.jfeng.gateway.channel;

import com.jfeng.gateway.GatewayApplication;
import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.comm.ThreadFactoryImpl;
import com.jfeng.gateway.config.GateWayConfig;
import com.jfeng.gateway.util.DateTimeUtils;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.RedisUtils;
import com.jfeng.gateway.util.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tcp连接管理器
 *
 * @param <T>
 */
@Slf4j
@Getter
public class TcpManage<T extends TcpChannel> implements ChannelEventListener, OnlineStateChangeListener {
    private static Map<String, TcpManage> instances = new ConcurrentHashMap<>();
    private final String localAddress;

    public static TcpManage getInstance(String localAddress) {
        TcpManage tcpManage = instances.get(localAddress);
        if (tcpManage == null) {
            tcpManage = new TcpManage(localAddress);
            TcpManage old = instances.putIfAbsent(localAddress, tcpManage);
            if (old != null) {
                tcpManage = old;
            }
        }
        return tcpManage;
    }

    private TcpManage(String localAddress) {
        this.localAddress = localAddress;
        init();
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("设备在线")).submit(() -> {
            while (isRunning) {
                try {
                    StateChangeEvent event = stateChangeEvent.take();
                    TcpChannel tcpChannel = event.session;
                    String mappingKey = Constant.ONLINE_MAPPING + tcpChannel.getPacketId();

                    if (event.state == 1) {
                        Map<String, String> mapping = new HashMap<>();
                        mapping.put(Constant.MACHINE, tcpChannel.getLocalAddress());
                        mapping.put(Constant.LAST_REFRESH_TIME, DateTimeUtils2.outNow());
                        redisUtils.putAll(mappingKey, mapping);
                    } else {
                        redisUtils.delete(mappingKey);
                    }

                } catch (Exception e) {
                    log.warn("在线列表存储异常：", e);
                } finally {
                    try {
                        Thread.sleep(checkPeriod);
                    } catch (InterruptedException e) {

                    }
                }
            }
        });
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("定时检测")).submit(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    //1.移除所有已关闭连接、登录超时连接
                    Iterator<Map.Entry<String, TcpChannel>> iterConnected = connected.entrySet().iterator();
                    while (iterConnected.hasNext()) {
                        TcpChannel value = iterConnected.next().getValue();
                        if (value == null || value.getChannelStatus() == ChannelStatus.CLOSED) {
                            iterConnected.remove();
                        } else if (value.getChannelStatus() == ChannelStatus.CONNECTED) {
                            if (loginTimeout > 0 && timeOut(value.getCreateTime(), loginTimeout)) {
                                value.close("登陆超时,超时间隔：" + loginTimeout / 1000 + "s. 连接创建时间:" + DateTimeUtils.outEpochMilli(value.getCreateTime()));
                            }
                        }
                    }

                    //2. 心跳超时，已登录的终端指定时间没有发送发送
                    Iterator<Map.Entry<String, TcpChannel>> iterLogined = onLines.entrySet().iterator();
                    while (iterLogined.hasNext()) {
                        TcpChannel session = iterLogined.next().getValue();
                        if (session == null || session.getChannelStatus() == ChannelStatus.CLOSED) {
                            iterLogined.remove();
                        } else if (session.getChannelStatus() == ChannelStatus.LOGIN) {
                            if (heartTimeout > 0 && timeOut(session.getLastReadTime(), heartTimeout)) {
                                session.close("心跳超时,超时间隔：" + heartTimeout / 1000 + "s. 最后接收时间：" + DateTimeUtils.outEpochMilli(session.getLastReadTime()));
                            }
                        } else {
                            session.close("设备状态异常关闭：" + session.getChannel().toString());
                        }
                    }
                } catch (Exception e) {
                    log.warn("超时检测", e);
                } finally {
                    try {
                        Thread.sleep(timeoutCheckInterval);
                    } catch (InterruptedException e) {
                        log.warn("超时检测休眠异常：", e);
                    }
                }
            }
        });
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("总流量统计")).submit(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    Map<String, String> hashValue = new HashMap<>();
                    hashValue.put(Constant.TOTAL_ONLINE, String.valueOf(onLines.size()));
                    hashValue.put(Constant.TOTAL_CONNECTED, String.valueOf(connected.size()));
                    hashValue.put(Constant.TOTAL_CONNECT_NUM, String.valueOf(totalConnectNum));
                    hashValue.put(Constant.TOTAL_CLOSE_NUM, String.valueOf(totalCloseNum));
                    hashValue.put(Constant.TOTAL_SEND_PACKETS, String.valueOf(totalSendPackets));
                    hashValue.put(Constant.TOTAL_SEND_BYTES, String.valueOf(totalSendBytes));
                    hashValue.put(Constant.TOTAL_RECEIVE_PACKETS, String.valueOf(totalReceivePackets));
                    hashValue.put(Constant.TOTAL_RECEIVE_BYTES, String.valueOf(totalReceiveBytes));
                    hashValue.put(Constant.LAST_REFRESH_TIME, DateTimeUtils2.outNow());

                    redisUtils.putAll("iot:machine:" + localAddress + ":summary", hashValue);
                } catch (Exception e) {
                    log.warn("总流量统计", e);
                } finally {
                    try {
                        Thread.sleep(checkPeriod);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private RedisUtils redisUtils;
    private KafkaTemplate kafkaTemplate;

    private void init() {
        redisUtils = GatewayApplication.getObject(RedisUtils.class);
        kafkaTemplate = GatewayApplication.getObject(KafkaTemplate.class);
        GateWayConfig gateWayConfig = GatewayApplication.getObject(GateWayConfig.class);
        if (gateWayConfig.getParameter().get("checkPeriod") != null) {
            this.checkPeriod = Integer.parseInt(gateWayConfig.getParameter().get("checkPeriod"));
        } else {
            this.checkPeriod = 10000;
        }
        if (gateWayConfig.getParameter().get("loginTimeout") != null) {
            this.loginTimeout = Integer.parseInt(gateWayConfig.getParameter().get("loginTimeout"));
        }
        if (gateWayConfig.getParameter().get("heartTimeout") != null) {
            this.heartTimeout = Integer.parseInt(gateWayConfig.getParameter().get("heartTimeout"));
        }
    }


    private boolean timeOut(long time, int threshold) {
        return (System.currentTimeMillis() - time) > threshold;
    }

    public boolean contains(String identifyNo) {
        return onLines.containsKey(identifyNo);
    }


    private volatile boolean isRunning = true;
    private int loginTimeout;//登陆超时
    private int heartTimeout;//心跳超时
    private int timeoutCheckInterval = 3000;//检测周期
    private int checkPeriod;//检测周期

    private boolean allowMultiSocketPerDevice = false;//是否需要单设备多连接，如双卡设备
    private Map<String, TcpChannel> connected = new ConcurrentHashMap<>();//已连接集合
    private Map<String, TcpChannel> onLines = new ConcurrentHashMap<>();//已在线集合
    private Map<String, List<TcpChannel>> onLinesSpare = new ConcurrentHashMap<>();//已在线集合(备用)

    private BlockingQueue<StateChangeEvent> stateChangeEvent = new LinkedBlockingQueue<>(10000);//上下线事件
    private BlockingQueue<ConnectDetail> connectDetails = new LinkedBlockingQueue<>(10000);//连接明细

    private AtomicInteger totalConnectNum = new AtomicInteger(0);//总连接次数
    private AtomicInteger totalCloseNum = new AtomicInteger(0);//总关闭次数
    private AtomicLong totalSendPackets = new AtomicLong(0L);//总发送包数量
    private AtomicLong totalSendBytes = new AtomicLong(0L);//总发送字节数
    private AtomicLong totalReceivePackets = new AtomicLong(0L);//总接收包数量
    private AtomicLong totalReceiveBytes = new AtomicLong(0L);//总接收字节数

    @Override
    public void onConnect(TcpChannel tcpChannel) {
        String channelId = tcpChannel.getChannelId();

        if (this.connected.containsKey(channelId)) {
            this.connected.get(channelId).close("会话重复连接：" + tcpChannel);
        }
        totalConnectNum.getAndIncrement();
        connected.putIfAbsent(channelId, tcpChannel);

    }

    @Override
    public void onReceive(TcpChannel tcpChannel, byte[] data) {
        totalReceiveBytes.getAndIncrement();
        totalReceivePackets.getAndAdd(data.length);
    }


    @Override
    public void onReceiveComplete(TcpChannel tcpChannel, byte[] data) {

    }

    @Override
    public void onSend(TcpChannel tcpChannel, byte[] data) {
        totalSendPackets.getAndIncrement();
        totalSendBytes.getAndAdd(data.length);
    }

    @Override
    public void onDisConnect(TcpChannel tcpChannel, String reason) {
        totalCloseNum.getAndIncrement();

        String packetId = tcpChannel.getPacketId();

        //未登录连接
        if (StringUtils.isEmpty(packetId)) {
            TcpChannel removed = connected.remove(tcpChannel.getLocalAddress());
            if (removed != null) {
                Offline(tcpChannel, reason);
            }
        }
        //已登录连接
        else {
            TcpChannel removed = onLines.remove(packetId);
            if (removed != null) {
                Offline(tcpChannel, reason);
            }
        }
    }

    @Override
    public void online(TcpChannel tcpChannel) {
        if (!this.connected.remove(tcpChannel.getChannelId(), tcpChannel)) {
            log.warn("移除连接会话失败");
        }
        tcpChannel.getChannel().eventLoop().execute(() -> {
            String onlineKey = "iot:machine:" + tcpChannel.getLocalAddress() + ":connect:" + tcpChannel.getRemoteAddress();
            redisUtils.delete(onlineKey);
        });

        String packetId = tcpChannel.getPacketId();
        TcpChannel clientOld = this.onLines.putIfAbsent(packetId, tcpChannel);
        if (clientOld != null && allowMultiSocketPerDevice) {
            if (this.onLinesSpare.containsKey(packetId) == false) {
                this.onLinesSpare.put(packetId, new ArrayList<>());
            }
            this.onLinesSpare.get(packetId).add(tcpChannel);
        }
        stateChangeEvent.offer(new StateChangeEvent(tcpChannel, 1));
    }

    @Override
    public void Offline(TcpChannel tcpChannel, String message) {
        stateChangeEvent.offer(new StateChangeEvent(tcpChannel, 0));
    }

    /**
     * 连接状态切换通知
     */
    class StateChangeEvent {
        /**
         * 0:下线 1：上线
         */
        private int state;
        TcpChannel session;

        public StateChangeEvent(TcpChannel session, int state) {
            this.session = session;
            this.state = state;
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
