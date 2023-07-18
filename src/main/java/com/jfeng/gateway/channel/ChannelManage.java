package com.jfeng.gateway.channel;

import com.jfeng.gateway.GatewayApplication;
import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.comm.ThreadFactoryImpl;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.RedisUtils;
import com.jfeng.gateway.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 通道管理器
 *
 * @param <T>
 */
@Slf4j
public class ChannelManage<T extends ClientChannel> implements ChannelEventListener {
    private static Map<String, ChannelManage> instances = new ConcurrentHashMap<>();

    public static ChannelManage getInstance(String channelType) {
        String key = channelType.toUpperCase();
        ChannelManage channelManage = instances.get(key);
        if (channelManage == null) {
            channelManage = new ChannelManage();
            ChannelManage old = instances.putIfAbsent(key, channelManage);
            if (old != null) {
                channelManage = old;
            }
        }
        return channelManage;
    }

    private ChannelManage() {
        init();
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("超时检测")).submit(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    //1.移除所有已关闭连接、登录超时连接
                    Iterator<Map.Entry<String, ClientChannel>> iterConnected = connected.entrySet().iterator();
                    while (iterConnected.hasNext()) {
                        ClientChannel value = iterConnected.next().getValue();
                        if (value == null || value.getChannelStatus() == ChannelStatus.CLOSED) {
                            iterConnected.remove();
                        } else if (value.getChannelStatus() == ChannelStatus.CONNECTED) {
                            if (timeOut(value.getCreateTime(), loginTimeout)) {
                                value.close("登录超时,超时间隔：" + loginTimeout / 1000 + "s." + value);
                            }
                        }
                    }

                    //2. 心跳超时，已登录的终端指定时间没有发送发送
                    Iterator<Map.Entry<String, ClientChannel>> iterLogined = onLines.entrySet().iterator();
                    while (iterLogined.hasNext()) {
                        ClientChannel session = iterLogined.next().getValue();
                        if (session == null || session.getChannelStatus() == ChannelStatus.CLOSED) {
                            iterLogined.remove();
                        } else if (session.getChannelStatus() == ChannelStatus.LOGIN) {
                            if (timeOut(session.getLastReadTime(), heartTimeout)) {
                                session.close("心跳超时,超时间隔：" + heartTimeout / 1000 + "s." + session.getChannel().toString());
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
                    ClientChannel clientChannel = event.session;

                    String onlineKey = Constant.ONLINE_LOCATION + clientChannel.getPacketId();
                    String onlineLocationKey = Constant.ONLINE_LOCATION + clientChannel.getPacketId();
                    Map<String, String> oldOnlineInfo;

                    if (event.state == 1) {
                        //1.维护上一次因为异常未保存的历史连接信息
                        if (redisUtils.hasKey(onlineKey) && ((oldOnlineInfo = redisUtils.entries(onlineKey)) != null)) {
                            long totalMill = Long.parseLong(oldOnlineInfo.get(Constant.ONLINE_INFO_LAST_REFRESH_TIME)) - Long.parseLong(oldOnlineInfo.get(Constant.ONLINE_INFO_CREATE_TIME));
                            oldOnlineInfo.put(Constant.ONLINE_INFO_TOTAL_MILL, String.valueOf(totalMill));
                            connectDetails.offer(new ConnectDetail(oldOnlineInfo));
                        }
                        //2.在线列表
                        Map<String, String> hashValue = new HashMap<>();
                        hashValue.put(Constant.ONLINE_INFO_REMOTE_ADDRESS, clientChannel.getRemoteAddress());

                        hashValue.put(Constant.ONLINE_INFO_PACKET_ID, clientChannel.getPacketId());
                        hashValue.put(Constant.ONLINE_INFO_ID, clientChannel.getId());
                        hashValue.put(Constant.ONLINE_INFO_CREATE_TIME, String.valueOf(clientChannel.getCreateTime()));

                        hashValue.put(Constant.ONLINE_INFO_RECEIVE_PACKETS, String.valueOf(clientChannel.getReceivedPackets()));
                        hashValue.put(Constant.ONLINE_INFO_RECEIVE_BYTES, String.valueOf(clientChannel.getReceivedBytes()));
                        hashValue.put(Constant.ONLINE_INFO_LAST_RECEIVE_TIME, String.valueOf(clientChannel.getLastReadTime()));

                        hashValue.put(Constant.ONLINE_INFO_SEND_PACKETS, String.valueOf(clientChannel.getSendPackets()));
                        hashValue.put(Constant.ONLINE_INFO_SEND_BYTES, String.valueOf(clientChannel.getSendBytes()));
                        hashValue.put(Constant.ONLINE_INFO_LAST_SEND_TIME, String.valueOf(clientChannel.getLastWriteTime()));
                        hashValue.put(Constant.ONLINE_INFO_LAST_REFRESH_TIME, String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli()));

                        redisUtils.putAll(onlineKey, hashValue);
                        redisUtils.expire(onlineKey, 5, TimeUnit.MINUTES);
                        //3. 维护设备和所属机器的关系
                        Map<String, String> mapping = new HashMap<>();
                        mapping.put(Constant.ONLINE_MACHINE, clientChannel.getLocalAddress());
                        mapping.put(Constant.ONLINE_LAST_REFRESH, DateTimeUtils2.outNow());
                        redisUtils.putAll(onlineLocationKey, mapping);
                    } else {
                        //1.保存连接信息
                        if (redisUtils.hasKey(onlineKey) && ((oldOnlineInfo = redisUtils.entries(onlineKey)) != null)) {
                            long totalMill = System.currentTimeMillis() - Long.parseLong(oldOnlineInfo.get(Constant.ONLINE_INFO_CREATE_TIME));
                            oldOnlineInfo.put(Constant.ONLINE_INFO_TOTAL_MILL, String.valueOf(totalMill));

                            connectDetails.offer(new ConnectDetail(oldOnlineInfo));
                        }
                        //2.删除在线列表及机器映射关系
                        redisUtils.delete(onlineKey);
                        redisUtils.delete(onlineLocationKey);
                    }
                } catch (Exception e) {
                    log.warn("在线列表存储异常：", e);
                }
            }
        });
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("总流量统计")).submit(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    Map<String, String> hashValue = new HashMap<>();
                    hashValue.put("TOTAL_ONLINES", String.valueOf(onLines.size()));
                    hashValue.put("TOTAL_CONNECTED", String.valueOf(connected.size()));
                    hashValue.put("TOTAL_CONNECT_NUM", String.valueOf(totalConnectNum));
                    hashValue.put("TOTAL_CLOSE_NUM", String.valueOf(totalCloseNum));
                    hashValue.put("TOTAL_SEND_PACKETS", String.valueOf(totalSendPackets));
                    hashValue.put("TOTAL_SEND_BYTES", String.valueOf(totalSendBytes));
                    hashValue.put("TOTAL_RECEIVE_PACKETS", String.valueOf(totalReceivePackets));
                    hashValue.put("TOTAL_RECEIVE_BYTES", String.valueOf(totalReceiveBytes));
                    hashValue.put("LAST_REFRESH_TIME", DateTimeUtils2.outNow());

//                    String onlineKey = "STATISTIC" + localIpAndPort;
//
//                    redisUtils.putAll(onlineKey, hashValue);
//                    redisUtils.expire(onlineKey, heartTimeout, TimeUnit.MILLISECONDS);

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
    }

    private boolean timeOut(long time, int threshold) {
        return (System.currentTimeMillis() - time) > threshold;
    }

    public boolean contains(String identifyNo) {
        return onLines.containsKey(identifyNo);
    }


    private volatile boolean isRunning = true;
    private int loginTimeout = 1000 * 60;//登陆超时
    private int heartTimeout = 1000 * 60 * 3;//心跳超时
    private int timeoutCheckInterval = 1000 * 3;//检测周期
    private int checkPeriod = 1000;//检测周期

    private boolean allowMultiSocketPerDevice = false;//是否需要单设备多连接，如双卡设备
    private Map<String, ClientChannel> connected = new ConcurrentHashMap<>();//已连接集合
    private Map<String, ClientChannel> onLines = new ConcurrentHashMap<>();//已在线集合
    private Map<String, List<ClientChannel>> onLinesSpare = new ConcurrentHashMap<>();//已在线集合(备用)

    private BlockingQueue<StateChangeEvent> stateChangeEvent = new LinkedBlockingQueue<>(10000);//上下线事件
    private BlockingQueue<ConnectDetail> connectDetails = new LinkedBlockingQueue<>(10000);//连接明细

    private AtomicInteger totalConnectNum = new AtomicInteger(0);//总连接次数
    private AtomicInteger totalCloseNum = new AtomicInteger(0);//总关闭次数
    private AtomicLong totalSendPackets = new AtomicLong(0L);//总发送包数量
    private AtomicLong totalSendBytes = new AtomicLong(0L);//总发送字节数
    private AtomicLong totalReceivePackets = new AtomicLong(0L);//总接收包数量
    private AtomicLong totalReceiveBytes = new AtomicLong(0L);//总接收字节数


    @Override
    public void onConnect(ClientChannel clientChannel) {
        String channelId = clientChannel.getChannelId();
        //TODO 会话重复时可以采取哪些策略
        if (this.connected.containsKey(channelId)) {
            this.connected.get(channelId).close("会话重复连接：" + clientChannel);
        }
        totalConnectNum.getAndIncrement();
        connected.putIfAbsent(channelId, clientChannel);
    }

    @Override
    public void onReceive(ClientChannel clientChannel, byte[] data) {
        totalReceiveBytes.getAndIncrement();
        totalReceivePackets.getAndAdd(data.length);
    }

    @Override
    public void onSend(ClientChannel clientChannel, byte[] data) {
        totalSendPackets.getAndIncrement();
        totalSendBytes.getAndAdd(data.length);
    }

    @Override
    public void onDisConnect(ClientChannel clientChannel, String reason) {
        totalCloseNum.getAndIncrement();
        connected.remove(clientChannel.getPacketId(), clientChannel);
        if (StringUtils.isNotEmpty(clientChannel.getPacketId()) && onLines.remove(clientChannel.getPacketId(), clientChannel)) {
        }
        //断开不应该使用异步，因为session可以会销毁相关属性
        //stateChangeEvent.offer(new StateChangeEvent(session, 0));
//        redisUtils.delete(Constant.SYSTEM_PREFIX + "ONLINE:" + session.keyNo);
    }

    @Override
    public void online(ClientChannel clientChannel) {
        String packetId = clientChannel.getPacketId();
        if (!this.connected.remove(packetId, clientChannel)) {
            log.warn("移除连接会话失败");
        }

        if (allowMultiSocketPerDevice) {
            if (this.onLinesSpare.containsKey(packetId)) {
//                this.onLinesSpare.put(session.keyNo,)
            } else {
                this.onLines.put(packetId, clientChannel);
            }
        }
        stateChangeEvent.offer(new StateChangeEvent(clientChannel, 1));
    }

    @Override
    public void Offline(ClientChannel clientChannel, String message) {
//        this.onLines.remove(session.identifierNo);
//        stateChangeEvent.offer(new StateChangeEvent(session, 0));
    }

    class StateChangeEvent {
        /**
         * 0:下线 1：上线
         */
        private int state;
        ClientChannel session;

        public StateChangeEvent(ClientChannel session, int state) {
            this.session = session;
            this.state = state;
        }
    }

    class ConnectDetail {

        Map<String, String> connectInfo;

        public ConnectDetail(Map<String, String> connectInfo) {
            this.connectInfo = connectInfo;
        }
    }
}
