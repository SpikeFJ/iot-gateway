package com.jfeng.gateway.server;

import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.comm.ThreadFactoryImpl;
import com.jfeng.gateway.dispatch.Dispatcher;
import com.jfeng.gateway.message.CommandReq;
import com.jfeng.gateway.message.DispatchMessage;
import com.jfeng.gateway.protocol.none4.*;
import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.SessionStatus;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TCP服务
 */
@Slf4j
@Getter
@Setter
@Component
public class TcpServer implements Server, SessionListener {
    ServerBootstrap bootstrap = new ServerBootstrap();
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Resource
    List<SessionListener> sessionListeners;
    @Resource
    Dispatcher dispatcher;
    @Resource
    RedisUtils redisUtils;

    String localAddress;
    //TODO 协议code和handler处理器之间的联系及约束
    String protocol;

    private static final String WAIT_TO_SEND = "IOT:WAIT_TO_SEND";
    private static final String WAIT_TO_ACK = "IOT:WAIT_TO_ACK";
    private static final String SENT = "IOT:SENT";

    private static final String WAIT_TO_SEND_KAFKA = "IOT:WAIT_TO_SEND_KAFKA";

    @Override
    public void init(Map<String, String> parameters) {
        try {
            localAddress = Utils.getLocalIp();
        } catch (UnknownHostException e) {

        }
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

                    String onlineKey = "iot:" + tcpSession.getLocalAddress() + ":online:" + tcpSession.getPacketId();
                    String mappingKey = Constant.ONLINE_MAPPING + tcpSession.getPacketId();

                    Map<String, String> oldOnlineInfo;
                    //1. 维护上一次因为异常未保存的历史连接信息
                    if (redisUtils.hasKey(onlineKey) && ((oldOnlineInfo = redisUtils.entries(onlineKey)) != null)) {
                        LocalDateTime createTime = DateTimeUtils2.parse(oldOnlineInfo.get(Constant.ONLINE_INFO_CREATE_TIME), "yyyy-MM-dd HH:mm:ss");
                        LocalDateTime endTime = event.state == 1 ? DateTimeUtils2.parse(oldOnlineInfo.get(Constant.ONLINE_INFO_LAST_REFRESH_TIME), "yyyy-MM-dd HH:mm:ss") : LocalDateTime.now();
                        long totalMill = Duration.between(endTime, createTime).getSeconds();

                        oldOnlineInfo.put(Constant.ONLINE_INFO_TOTAL_MILL, String.valueOf(totalMill));
                        connectDetails.offer(new ConnectDetail(oldOnlineInfo));
                    }
                    //2. 维护映射关系
                    if (event.state == 1) {
                        String connectKey = Constant.SYSTEM_PREFIX + tcpSession.getTcpServer().getLocalAddress() + ":connect:" + tcpSession.getRemoteAddress();
                        redisUtils.delete(connectKey);

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
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("心跳超时检测")).submit(() -> {
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
                        if (session == null) {
                            iterLogined.remove();
                        } else if (session.getSessionStatus() == SessionStatus.CLOSED || session.getSessionStatus() == SessionStatus.LOGIN) {
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
                    hashValue.put(Constant.ONLINE, String.valueOf(onLines.size()));
                    hashValue.put(Constant.CONNECTED, String.valueOf(connected.size()));
                    hashValue.put(Constant.TOTAL_CONNECT_NUM, String.valueOf(totalConnectNum));
                    hashValue.put(Constant.TOTAL_CLOSE_NUM, String.valueOf(totalCloseNum));
                    hashValue.put(Constant.TOTAL_SEND_PACKETS, String.valueOf(totalSendPackets));
                    hashValue.put(Constant.TOTAL_SEND_BYTES, String.valueOf(totalSendBytes));
                    hashValue.put(Constant.TOTAL_RECEIVE_PACKETS, String.valueOf(totalReceivePackets));
                    hashValue.put(Constant.TOTAL_RECEIVE_BYTES, String.valueOf(totalReceiveBytes));
                    hashValue.put(Constant.LAST_REFRESH_TIME, DateTimeUtils2.outNow());

                    redisUtils.putAll(Constant.SYSTEM_PREFIX + localAddress + ":summary", hashValue);
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
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("同步下发")).submit(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    String deviceId = haveWaitToSend.take();
                    if (this.contains(deviceId)) {
                        TcpSession session = this.get(deviceId);
                        session.send();
                    }
                } catch (Exception e) {
                    log.warn("下行检测", e);
                } finally {
                    try {
                        Thread.sleep(checkPeriod);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("异步下发")).submit(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    CommandReq req = this.waitToSendFromKafka.take();

                    TcpSession session = this.get(req.getDeviceId());
                    session.send(ByteBufUtil.decodeHexDump(req.getData()), true);
                } catch (Exception e) {
                    log.warn("下行检测", e);
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

    public void sendWaitToSend(TcpSession session) throws Exception {
        String deviceId = session.getPacketId();
        //1. 待确认队列
        while (session.getSessionStatus() != SessionStatus.CLOSED && redisUtils.rightPopAndLeftPush(WAIT_TO_ACK + deviceId, WAIT_TO_SEND + deviceId) != null) {
            Thread.sleep(1);
        }

        //2.1 读取待发送最新元素
        String toBeSend;
        while (session.getSessionStatus() != SessionStatus.CLOSED && (toBeSend = redisUtils.rightPopAndLeftPush(WAIT_TO_SEND + deviceId, WAIT_TO_ACK + deviceId)) != null) {
            CommandReq req = JsonUtils.deserialize(toBeSend, CommandReq.class);
            req.setTryTimes(req.getTryTimes() + 1);

            //2.2 发送
            session.send(ByteBufUtil.decodeHexDump(req.getData()), true);

            //2.3 移除确认
            redisUtils.rightPop(WAIT_TO_ACK + deviceId);

            //2.4 保持至已发送
            redisUtils.put(SENT, req.getSendNo(), JsonUtils.serialize(req));

            Thread.sleep(1);
        }
    }

    @Override
    public void start() throws Exception {
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_RCVBUF, 1024 * 8).childOption(ChannelOption.SO_SNDBUF, 1024 * 8).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new IpFilterHandler(null, null));
                pipeline.addLast(new StatisticsHandler(TcpServer.this));
                pipeline.addLast(new StandardExtend4Decoder());
                pipeline.addLast(new StandardProtocol4Encoder());
                pipeline.addLast(new LoginHandler());

            }
        });

        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
        ChannelFuture future = bootstrap.bind(30000).sync();
        log.info("TCP服务启动成功,端口: {}", 30000);
        future.channel().closeFuture().addListener(x -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info(future.channel().toString() + " TCP服务关闭");
        });
    }

    @Override
    public void stop() {
        log.info("TCP服务关闭");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    private boolean timeOut(long time, int threshold) {
        return (System.currentTimeMillis() - time) > threshold;
    }

    public boolean contains(String packetId) {
        return onLines.containsKey(packetId) || onLinesSpare.containsKey(packetId);
    }

    public TcpSession get(String packetId) {
        if (onLines.containsKey(packetId)) {
            return onLines.get(packetId);
        }
        if (onLinesSpare.containsKey(packetId)) {
            return onLinesSpare.get(packetId).get(0);
        }
        return null;
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
    private BlockingQueue<String> haveWaitToSend = new LinkedBlockingQueue<>(100);//有要发送的数据的设备Id

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
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {
        totalReceiveBytes.getAndIncrement();
        totalReceivePackets.getAndAdd(data.length);
    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        totalSendPackets.getAndIncrement();
        totalSendBytes.getAndAdd(data.length);
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {
        totalCloseNum.getAndIncrement();

        String packetId = tcpSession.getPacketId();
        //未登录连接
        if (StringUtils.isEmpty(packetId)) {
            TcpSession removed = connected.remove(tcpSession.getLocalAddress());
            if (removed != null) {
                Offline(removed, reason);
            }
        }
        //已登录连接
        else {
            TcpSession removed = onLines.remove(packetId);
            if (removed != null) {
                Offline(removed, reason);
            }else{

            }
        }
    }

    @Override
    public void online(TcpSession tcpSession) {
        if (!this.connected.remove(tcpSession.getChannelId(), tcpSession)) {
            log.warn("移除连接会话失败");
        }

        String packetId = tcpSession.getPacketId();

        if (this.onLines.containsKey(packetId) == false) {
            this.onLines.put(packetId, tcpSession);
        } else {
            if (allowMultiSocketPerDevice) {
                if (this.onLinesSpare.containsKey(packetId) == false) {
                    this.onLinesSpare.put(packetId, new ArrayList<>());
                }
                this.onLinesSpare.get(packetId).add(tcpSession);
            } else {
                this.onLines.get(packetId).close("重复登陆");
            }
        }
        stateChangeEvent.offer(new StateChangeEvent(tcpSession, 1));
    }

    @Override
    public void Offline(TcpSession tcpSession, String message) {
        stateChangeEvent.offer(new StateChangeEvent(tcpSession, 0));
    }

    /**
     * 数据分发
     *
     * @param packetId
     * @param hexData
     */
    public void dispatch(String packetId, String hexData) {
        dispatcher.sendNext(packetId, new DispatchMessage(protocol, hexData, localAddress));
    }

    private BlockingQueue<CommandReq> waitToSendFromKafka = new LinkedBlockingQueue<>(10000);


    public void loadKafkaData(TcpSession session) {
        String element = null;
        while (session.getSessionStatus() != SessionStatus.CLOSED && (element = redisUtils.rightPop(WAIT_TO_ACK + session.getPacketId())) != null) {
            this.fromKafka(element);
        }
    }

    public void fromKafka(String value) {
        CommandReq req = JsonUtils.deserialize(value, CommandReq.class);

        String deviceId = req.getDeviceId();

        if (contains(deviceId)) {
            waitToSendFromKafka.offer(req);
        } else if (req.isSendOnOffline()) {
            redisUtils.leftPush(WAIT_TO_SEND_KAFKA + deviceId, value);
        }
    }

    /**
     * 来自http的下行发送
     */
    public void fromHttp(CommandReq request) throws Exception {
        String deviceId = request.getDeviceId();

        if (this.contains(deviceId)) {
            saveWaitToSend(deviceId, request);
            notify(deviceId);

        } else if (request.isSendOnOffline()) {
            saveWaitToSend(deviceId, request);
        } else {
            log.warn("设备[" + request.getDeviceId() + "]不在线。");
        }
    }

    public void saveWaitToSend(String deviceId, CommandReq commandReq) {
        redisUtils.leftPush(WAIT_TO_SEND + deviceId, JsonUtils.serialize(commandReq));
    }

    public void notify(String deviceId) {
        haveWaitToSend.offer(deviceId);
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
