package com.jfeng.gateway.server;

import com.jfeng.gateway.config.GateWayConfig;
import com.jfeng.gateway.handler.none4.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.jfeng.gateway.comm.Constant.*;

/**
 * TCP接入服务
 */
@Component
@Slf4j
public class TcpServer implements Server {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Resource
    GateWayConfig gateWayConfig;

    @PostConstruct
    @Override
    public void start() throws Exception {
        DefaultEventExecutorGroup executors = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);
        GateWayConfig.ConfigItem tcpItem = gateWayConfig.tcp;

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, SO_BACKLOG).childOption(ChannelOption.SO_RCVBUF, SO_RCVBUF).childOption(ChannelOption.SO_SNDBUF, SO_SNDBUF).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new IpFilterHandler(tcpItem.getBlackIpList(), tcpItem.getWhiteIpList()));
                pipeline.addLast(new StatisticsHandler());
                pipeline.addLast(new StandardExtend4Decoder());
                pipeline.addLast(new StandardProtocol4Encoder());
                pipeline.addLast(new LoginHandler());
            }
        });

        int listenPort = tcpItem.getPort();
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
        ChannelFuture future = bootstrap.bind(listenPort).sync();
        log.info("TCP服务启动成功,端口: {}", listenPort);
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
}
