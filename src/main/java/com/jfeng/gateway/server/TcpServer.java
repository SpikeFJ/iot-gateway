package com.jfeng.gateway.server;

import com.jfeng.gateway.handler.none4.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;

import static com.jfeng.gateway.comm.Constant.*;

/**
 * TCP服务
 */
@Slf4j
public class TcpServer implements Server {
    ServerBootstrap bootstrap = new ServerBootstrap();
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Override
    public void start(Map<String, String> parameters) throws Exception {
        DefaultEventExecutorGroup executors = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, Integer.parseInt(parameters.getOrDefault(TCP_SO_BACKLOG, "128")))
                .childOption(ChannelOption.SO_RCVBUF, Integer.parseInt(parameters.getOrDefault(TCP_SO_RCV_BUF, "8192")))
                .childOption(ChannelOption.SO_SNDBUF, Integer.parseInt(parameters.getOrDefault(TCP_SO_SND_BUF, "8192")))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IpFilterHandler(Arrays.asList(parameters.get(BLACK_IP_LIST).split(",")), Arrays.asList(parameters.get(WHITE_IP_LIST).split(","))));
                        pipeline.addLast(new StatisticsHandler(parameters));
                        pipeline.addLast(new StandardExtend4Decoder());
                        pipeline.addLast(new StandardProtocol4Encoder());
                        pipeline.addLast(new LoginHandler());
                    }
                });

        int listenPort = Integer.parseInt(parameters.get(PORT));
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
