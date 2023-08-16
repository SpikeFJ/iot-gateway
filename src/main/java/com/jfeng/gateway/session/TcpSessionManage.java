package com.jfeng.gateway.session;

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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tcp连接管理器
 *
 * @param <T>
 */
@Slf4j
@Getter
public class TcpSessionManage<T extends TcpSession> implements SessionListener, OnlineStateChangeListener {
    private static Map<String, TcpSessionManage> instances = new ConcurrentHashMap<>();
    private final String localAddress;

    public static TcpSessionManage getInstance(String localAddress) {
        TcpSessionManage tcpSessionManage = instances.get(localAddress);
        if (tcpSessionManage == null) {
            tcpSessionManage = new TcpSessionManage(localAddress);
            TcpSessionManage old = instances.putIfAbsent(localAddress, tcpSessionManage);
            if (old != null) {
                tcpSessionManage = old;
            }
        }
        return tcpSessionManage;
    }

    private TcpSessionManage(String localAddress) {
        this.localAddress = localAddress;
        init();
    }

    private RedisUtils redisUtils;
    private KafkaTemplate kafkaTemplate;

    private void initConfig() {
        redisUtils = GatewayApplication.getObject(RedisUtils.class);
        kafkaTemplate = GatewayApplication.getObject(KafkaTemplate.class);
        GateWayConfig gateWayConfig = GatewayApplication.getObject(GateWayConfig.class);
        GateWayConfig.ConfigItem item = gateWayConfig.getTcp();
        if (item.getParameter().get("checkPeriod") != null) {
            this.checkPeriod = Integer.parseInt(item.getParameter().get("checkPeriod"));
        }
        if (item.getParameter().get("loginTimeout") != null) {
            this.loginTimeout = Integer.parseInt(item.getParameter().get("loginTimeout"));
        }
        if (item.getParameter().get("heartTimeout") != null) {
            this.heartTimeout = Integer.parseInt(item.getParameter().get("heartTimeout"));
        }
    }

    public void init(){
        initConfig();

        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("连接明细")).submit(() -> {
            while (isRunning) {
                try {
                    //TODO 维护历史连接明细
                    ConnectDetail connectDetail = connectDetails.take();

                } catch (Exception e) {
                    log.warn("在线列表存储异常：", e);
                }
            }
        });
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("设备在线")).submit(() -> {
            while (isRunning) {
                try {
                    StateChangeEvent event = stateChangeEvent.take();
                    TcpSession tcpSession = event.session;

                    String onlineKey = "iot:machine:" + tcpSession.getLocalAddress() + ":online:" + tcpSession.getPacketId();
                    String mappingKey = Constant.ONLINE_MAPPING + tcpSession.getPacketId();

                    Map<String, String> oldOnlineInfo;
                    //维护上一次因为异常未保存的历史连接信息
                    if (redisUtils.hasKey(onlineKey) && ((oldOnlineInfo = redisUtils.entries(onlineKey)) != null)) {
                        LocalDateTime createTime = DateTimeUtils2.parse(oldOnlineInfo.get(Constant.ONLINE_INFO_CREATE_TIME), "yyyy-MM-dd HH:mm:ss");
                        LocalDateTime endTime = event.state == 1 ? DateTimeUtils2.parse(oldOnlineInfo.get(Constant.ONLINE_INFO_LAST_REFRESH_TIME), "yyyy-MM-dd HH:mm:ss") : LocalDateTime.now();
                        long totalMill = Duration.between(endTime, createTime).getSeconds();

                        oldOnlineInfo.put(Constant.ONLINE_INFO_TOTAL_MILL, String.valueOf(totalMill));
                        connectDetails.offer(new ConnectDetail(oldOnlineInfo));
                    }

                    if (event.state == 1) {
                        Map<String, String> mapping = new HashMap<>();
                        mapping.put(Constant.MACHINE, tcpSession.getLocalAddress());
                        mapping.put(Constant.LAST_REFRESH_TIME, DateTimeUtils2.outNow());
                        redisUtils.putAll(mappingKey, mapping);
                    } else {
                        redisUtils.delete(mappingKey);
                    }

                } catch (Exception e) {
                    log.warn("在线列表存储异常：", e);
                }
            }
        });
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("定时检测")).submit(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    //1.移除所有已关闭连接、登录超时连接
                    Iterator<Map.Entry<String, TcpSession>> iterConnected = connected.entrySet().iterator();
                    while (iterConnected.hasNext()) {
                        TcpSession value = iterConnected.next().getValue();
                        if (value == null || value.getSessionStatus() == SessionStatus.CLOSED) {
                            iterConnected.remove();
                        } else if (value.getSessionStatus() == SessionStatus.CONNECTED) {
                            if (loginTimeout > 0 && timeOut(value.getCreateTime(), loginTimeout)) {
                                value.close("登陆超时,超时间隔：" + loginTimeout / 1000 + "s. 连接创建时间:" + DateTimeUtils.outEpochMilli(value.getCreateTime()));
                            }
                        }
                    }

                    //2. 心跳超时，已登录的终端指定时间没有发送发送
                    Iterator<Map.Entry<String, TcpSession>> iterLogined = onLines.entrySet().iterator();
                    while (iterLogined.hasNext()) {
                        TcpSession session = iterLogined.next().getValue();
                        if (session == null || session.getSessionStatus() == SessionStatus.CLOSED) {
                            iterLogined.remove();
                        } else if (session.getSessionStatus() == SessionStatus.LOGIN) {
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


    private boolean timeOut(long time, int threshold) {
        return (System.currentTimeMillis() - time) > threshold;
    }

    public boolean contains(String identifyNo) {
        return onLines.containsKey(identifyNo);
    }


    private volatile boolean isRunning = true;
    private int loginTimeout;//登陆超时
    private int heartTimeout;//心跳超时
    private int timeoutCheckInterval;//检测周期
    private int checkPeriod;//检测周期

    private boolean allowMultiSocketPerDevice = false;//是否需要单设备多连接，如双卡设备
    private Map<String, TcpSession> connected = new ConcurrentHashMap<>();//已连接集合
    private Map<String, TcpSession> onLines = new ConcurrentHashMap<>();//已在线集合
    private Map<String, List<TcpSession>> onLinesSpare = new ConcurrentHashMap<>();//已在线集合(备用)

    private BlockingQueue<StateChangeEvent> stateChangeEvent = new LinkedBlockingQueue<>(10000);//上下线事件
    private BlockingQueue<ConnectDetail> connectDetails = new LinkedBlockingQueue<>(10000);//连接明细

    private AtomicInteger totalConnectNum = new AtomicInteger(0);//总连接次数
    private AtomicInteger totalCloseNum = new AtomicInteger(0);//总关闭次数
    private AtomicLong totalSendPackets = new AtomicLong(0L);//总发送包数量
    private AtomicLong totalSendBytes = new AtomicLong(0L);//总发送字节数
    private AtomicLong totalReceivePackets = new AtomicLong(0L);//总接收包数量
    private AtomicLong totalReceiveBytes = new AtomicLong(0L);//总接收字节数

    @Override
    public void onConnect(TcpSession tcpSession) {
        String channelId = tcpSession.getChannelId();

        if (this.connected.containsKey(channelId)) {
            this.connected.get(channelId).close("会话重复连接：" + tcpSession);
        }
        totalConnectNum.getAndIncrement();
        connected.putIfAbsent(channelId, tcpSession);

        tcpSession.getChannel().eventLoop().scheduleAtFixedRate(() -> {
            saveSingleChannel(tcpSession);
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {
        totalReceiveBytes.getAndIncrement();
        totalReceivePackets.getAndAdd(data.length);
    }


    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {
        tcpSession.getChannel().eventLoop().execute(() -> {
            //写入到kafka
        });
    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        totalSendPackets.getAndIncrement();
        totalSendBytes.getAndAdd(data.length);
        tcpSession.getChannel().eventLoop().execute(() -> {
            //写入到kafka
        });
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {
        totalCloseNum.getAndIncrement();

        String packetId = tcpSession.getPacketId();

        //未登录连接
        if (StringUtils.isEmpty(packetId)) {
            TcpSession removed = connected.remove(tcpSession.getLocalAddress());
            if (removed != null) {
                Offline(tcpSession, reason);
            }
        }
        //已登录连接
        else {
            TcpSession removed = onLines.remove(packetId);
            if (removed != null) {
                Offline(tcpSession, reason);
            }
        }
    }

    @Override
    public void online(TcpSession tcpSession) {
        if (!this.connected.remove(tcpSession.getChannelId(), tcpSession)) {
            log.warn("移除连接会话失败");
        }
        tcpSession.getChannel().eventLoop().execute(() -> {
            String onlineKey = "iot:machine:" + tcpSession.getLocalAddress() + ":connect:" + tcpSession.getRemoteAddress();
            redisUtils.delete(onlineKey);
        });

        String packetId = tcpSession.getPacketId();
        TcpSession clientOld = this.onLines.putIfAbsent(packetId, tcpSession);
        if (clientOld != null && allowMultiSocketPerDevice) {
            if (this.onLinesSpare.containsKey(packetId) == false) {
                this.onLinesSpare.put(packetId, new ArrayList<>());
            }
            this.onLinesSpare.get(packetId).add(tcpSession);
        }
        stateChangeEvent.offer(new StateChangeEvent(tcpSession, 1));
        tcpSession.getChannel().eventLoop().execute(() -> {
            //写入到kafka
        });
    }

    @Override
    public void Offline(TcpSession tcpSession, String message) {
        stateChangeEvent.offer(new StateChangeEvent(tcpSession, 0));
        tcpSession.getChannel().eventLoop().execute(() -> {
            //写入到kafka
        });
    }

    /**
     * 保存单个连接信息
     *
     * @param channel
     */
    private void saveSingleChannel(TcpSession channel) {
        if (channel == null) {
            log.warn("连接已关闭");
            channel.close("检测到连接关闭");
            return;
        }
        if (channel.getSessionStatus() == SessionStatus.CONNECTED) {
            String onlineKey = "iot:machine:" + channel.getLocalAddress() + ":connect:" + channel.getRemoteAddress();

            Map<String, String> hashValue = new HashMap<>();
            hashValue.put(Constant.ONLINE_INFO_CREATE_TIME, DateTimeUtils2.outString(channel.getCreateTime()));
            hashValue.put(Constant.ONLINE_INFO_LAST_REFRESH_TIME, DateTimeUtils2.outNow());

            redisUtils.putAll(onlineKey, hashValue);
        } else if (channel.getSessionStatus() == SessionStatus.LOGIN) {
            String onlineKey = "iot:machine:" + channel.getLocalAddress() + ":online:" + channel.getPacketId();
            String onlineLocationKey = Constant.ONLINE_MAPPING + channel.getPacketId();

            Map<String, String> hashValue = new HashMap<>();
            hashValue.put(Constant.ONLINE_INFO_REMOTE_ADDRESS, channel.getRemoteAddress());

            hashValue.put(Constant.ONLINE_INFO_PACKET_ID, channel.getPacketId());
            hashValue.put(Constant.ONLINE_INFO_ID, channel.getId());
            hashValue.put(Constant.ONLINE_INFO_CREATE_TIME, DateTimeUtils2.outString(channel.getCreateTime()));

            hashValue.put(Constant.ONLINE_INFO_RECEIVE_PACKETS, String.valueOf(channel.getReceivedPackets()));
            hashValue.put(Constant.ONLINE_INFO_RECEIVE_BYTES, String.valueOf(channel.getReceivedBytes()));
            hashValue.put(Constant.ONLINE_INFO_LAST_RECEIVE_TIME, DateTimeUtils2.outString(channel.getLastReadTime()));

            hashValue.put(Constant.ONLINE_INFO_SEND_PACKETS, String.valueOf(channel.getSendPackets()));
            hashValue.put(Constant.ONLINE_INFO_SEND_BYTES, String.valueOf(channel.getSendBytes()));
            hashValue.put(Constant.ONLINE_INFO_LAST_SEND_TIME, DateTimeUtils2.outString(channel.getLastWriteTime()));

            hashValue.put(Constant.ONLINE_INFO_LAST_REFRESH_TIME, DateTimeUtils2.outNow());

            redisUtils.putAll(onlineKey, hashValue);
            redisUtils.expire(onlineKey, 5, TimeUnit.MINUTES);
            //3. 维护设备和所属机器的关系
            Map<String, String> mapping = new HashMap<>();
            mapping.put(Constant.MACHINE, channel.getLocalAddress());
            mapping.put(Constant.LAST_REFRESH_TIME, DateTimeUtils2.outNow());
            redisUtils.putAll(onlineLocationKey, mapping);
        }
    }

    /**
     * 连接状态切换通知
     */
    class StateChangeEvent {
        /**
         * 0:下线 1：上线
         */
        private int state;
        TcpSession session;

        public StateChangeEvent(TcpSession session, int state) {
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
