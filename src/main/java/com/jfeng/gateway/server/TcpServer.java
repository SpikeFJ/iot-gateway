package com.jfeng.gateway.server;

import com.jfeng.gateway.comm.ThreadFactoryImpl;
import com.jfeng.gateway.down.CommandReq;
import com.jfeng.gateway.down.CommandResp;
import com.jfeng.gateway.down.DownInfoSaveStrategy;
import com.jfeng.gateway.message.DispatchMessage;
import com.jfeng.gateway.protocol.IpFilterHandler;
import com.jfeng.gateway.protocol.none4.LoginHandler;
import com.jfeng.gateway.protocol.none4.StandardExtend4Decoder;
import com.jfeng.gateway.protocol.none4.StandardProtocol4Encoder;
import com.jfeng.gateway.protocol.none4.StatisticsHandler;
import com.jfeng.gateway.session.ProxySessionListener;
import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.SessionStatus;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.up.dispatch.DataDispatcher;
import com.jfeng.gateway.util.DateTimeUtils;
import com.jfeng.gateway.util.Utils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
public class TcpServer extends ProxySessionListener implements Server {
    @Resource
    DataDispatcher dataDispatcher;

    @Autowired(required = false)
    PeriodServerWorker periodServerWorker;

    @Autowired(required = false)
    DownInfoSaveStrategy downInfoSave;

    String localAddress;
    int port;
    String protocol;

    private volatile boolean isRunning = true;
    private int loginTimeout;//登陆超时
    private int heartTimeout;//心跳超时
    private int timeoutCheckInterval = 1_000;//检测周期
    private int periodDelay = 30_000;//周期性任务延迟间隔

    private boolean allowMultiSocketPerDevice = false;//是否需要单设备多连接，如双卡设备
    private Map<String, TcpSession> connected = new ConcurrentHashMap<>();//已连接集合
    private Map<String, TcpSession> onLines = new ConcurrentHashMap<>();//已在线集合
    private Map<String, List<TcpSession>> onLinesSpare = new ConcurrentHashMap<>();//已在线集合(备用)

    private LocalDateTime createTime;
    private AtomicInteger totalConnectNum = new AtomicInteger(0);//总连接次数
    private AtomicInteger totalCloseNum = new AtomicInteger(0);//总关闭次数
    private AtomicLong totalSendPackets = new AtomicLong(0L);//总发送包数量
    private AtomicLong totalSendBytes = new AtomicLong(0L);//总发送字节数
    private AtomicLong totalReceivePackets = new AtomicLong(0L);//总接收包数量
    private AtomicLong totalReceiveBytes = new AtomicLong(0L);//总接收字节数

    private BlockingQueue<CommandReq> syncWaitToSend = new LinkedBlockingQueue<>(100);
    private Map<String, Map<String, CommandReq>> synSent = new ConcurrentHashMap<>();

    public void addListener(List<SessionListener> listeners) {
        for (SessionListener listener : listeners) {
            if (listener.equals(this) == false) {
                this.getChildListeners().add(listener);
            }
        }
    }

    @Override
    public void init(Map<String, String> parameters) {
        localAddress = Utils.getIpAddress();
        createTime = LocalDateTime.now();
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("超时检测")).submit(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    //1.移除所有已关闭连接、登录超时连接
                    Iterator<Map.Entry<String, TcpSession>> iterConnected = connected.entrySet().iterator();
                    while (iterConnected.hasNext()) {
                        TcpSession value = iterConnected.next().getValue();
                        if (value == null) {
                            iterConnected.remove();
                        } else if (value.getSessionStatus() == SessionStatus.CONNECTED) {
                            if (loginTimeout > 0 && timeOut(value.getCreateTime(), loginTimeout)) {
                                value.close("登陆超时,超时间隔：" + loginTimeout / 1000 + "s. 连接创建时间:" + DateTimeUtils.outEpochMilli(value.getCreateTime()));
                            }
                        }
                        if (loginTimeout > 0) {
                            if (value.getSessionStatus() == SessionStatus.CONNECTED && timeOut(value.getCreateTime(), loginTimeout)) {
                                if (loginTimeout > 0 && timeOut(value.getCreateTime(), loginTimeout)) {
                                    value.close("登陆超时,超时间隔：" + loginTimeout / 1000 + "s. 连接创建时间:" + DateTimeUtils.outEpochMilli(value.getCreateTime()));
                                }
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
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("下发发送")).submit(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    CommandReq req = this.syncWaitToSend.take();

                    TcpSession session = this.get(req.getDeviceId());
                    session.send(ByteBufUtil.decodeHexDump(req.getData()), true);

                    if (downInfoSave != null) {
                        downInfoSave.storeSending(req);
                    }
                } catch (Exception e) {
                    log.warn("下发发送", e);
                } finally {
                    try {
                        Thread.sleep(periodDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        if (periodServerWorker != null) {
            Executors.newSingleThreadExecutor(new ThreadFactoryImpl("机器流量统计")).submit(() -> {
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    try {
                        periodServerWorker.run(this);
                    } catch (Exception e) {
                        log.warn("机器流量统计", e);
                    } finally {
                        try {
                            Thread.sleep(getPeriodDelay());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    ServerBootstrap bootstrap = new ServerBootstrap();
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

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
        ChannelFuture future = bootstrap.bind(port).sync();
        log.info("TCP服务启动成功,端口: {}", port);
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

    public boolean contains(String deviceId) {
        return onLines.containsKey(deviceId) || onLinesSpare.containsKey(deviceId);
    }

    public TcpSession get(String deviceId) {
        if (onLines.containsKey(deviceId)) {
            return onLines.get(deviceId);
        }
        if (allowMultiSocketPerDevice && onLinesSpare.containsKey(deviceId)) {
            return onLinesSpare.get(deviceId).get(0);
        }
        return null;
    }

    @Override
    public void onConnect(TcpSession tcpSession) {
        String channelId = tcpSession.getChannelId();

        if (this.connected.containsKey(channelId)) {
            this.connected.get(channelId).close("会话重复连接：" + tcpSession);
        }
        totalConnectNum.getAndIncrement();
        connected.putIfAbsent(channelId, tcpSession);

        super.onConnect(tcpSession);
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {
        totalReceiveBytes.getAndIncrement();
        totalReceivePackets.getAndAdd(data.length);

        super.onReceive(tcpSession, data);
    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        totalSendPackets.getAndIncrement();
        totalSendBytes.getAndAdd(data.length);

        super.onSend(tcpSession, data);
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {
        totalCloseNum.getAndIncrement();

        if (tcpSession.getSessionStatus() == SessionStatus.CONNECTED) {
            connected.remove(tcpSession.getChannelId());
        } else if (tcpSession.getSessionStatus() == SessionStatus.LOGIN) {
            TcpSession removed = onLines.remove(tcpSession.getDeviceId());
            if (removed != null) {
                offline(removed, reason);
            }
        }
        super.onDisConnect(tcpSession, reason);
    }

    @Override
    public void online(TcpSession tcpSession) {
        if (!this.connected.remove(tcpSession.getChannelId(), tcpSession)) {
            log.warn("移除连接会话失败");
        }

        String deviceId = tcpSession.getDeviceId();

        if (this.onLines.containsKey(deviceId) == false) {
            this.onLines.put(deviceId, tcpSession);
        } else {
            if (allowMultiSocketPerDevice) {
                if (this.onLinesSpare.containsKey(deviceId) == false) {
                    this.onLinesSpare.put(deviceId, new ArrayList<>());
                }
                this.onLinesSpare.get(deviceId).add(tcpSession);
            } else {
                this.onLines.get(deviceId).close("重复登陆");
            }
        }

        super.online(tcpSession);
    }

    /**
     * 数据分发
     *
     * @param packetId
     * @param hexData
     */
    public void dispatch(String packetId, String hexData) {
        dataDispatcher.sendNext(packetId, new DispatchMessage(protocol, hexData, localAddress));
    }


    /**
     * 来自http的下行发送
     */
    public CommandResp fromHttp(CommandReq request) {
        String deviceId = request.getDeviceId();

        if (this.contains(deviceId)) {
            if (request.checkTryTimes()) {
                this.syncWaitToSend.offer(request);
                return null;
            } else {
                return null;
            }
        } else {
            if (request.isSendOnOffline()) {
                if (downInfoSave != null) {
                    downInfoSave.storeWaitToSend(request);
                    return null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}
