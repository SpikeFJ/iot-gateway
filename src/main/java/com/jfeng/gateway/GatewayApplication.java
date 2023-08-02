package com.jfeng.gateway;

import com.jfeng.gateway.config.GateWayConfig;
import com.jfeng.gateway.handler.none4.EventStatisticsHandler;
import com.jfeng.gateway.handler.none4.IpFilterHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;

@SpringBootApplication
@Slf4j
public class GatewayApplication implements CommandLineRunner, ApplicationContextAware {

    @Resource
    public GateWayConfig config;

    public static void main(String[] args) {
//        Charset ascii = Charset.forName("ASCII");
//        byte[] bytes = ByteBufUtil.decodeHexDump("040300000001");
//        String s = new String(bytes, ascii);
//
//        byte[] crc3 = Crc16Utils.getCRC3(bytes, 0, bytes.length);
        SpringApplication.run(GatewayApplication.class);
    }

    private static final Integer SO_BACKLOG = 128;
    private static final Integer SO_RCVBUF = 1024 * 8;
    private static final Integer SO_SNDBUF = 1024 * 8;

    @Override
    public void run(String... args) throws Exception {
        if (config.tcp != null && config.tcp.isEnable()) {
            startTcp(config.getTcp());
        }
        if (config.udp != null && config.udp.isEnable()) {

        }
    }

    private void startTcp(GateWayConfig.ConfigItem tcpItem) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup executors = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, SO_BACKLOG).childOption(ChannelOption.SO_RCVBUF, SO_RCVBUF).childOption(ChannelOption.SO_SNDBUF, SO_SNDBUF).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new IpFilterHandler(tcpItem.getBlackIpList(), tcpItem.getWhiteIpList()));
                    pipeline.addLast(new EventStatisticsHandler());
//                    pipeline.addLast(new StandardExtend4Decoder());
//                    pipeline.addLast(new StandardProtocol4Encoder());
//                    pipeline.addLast(new LoginHandler());
                }
            });

            int listenPort = tcpItem.getPort();
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
            ChannelFuture future = serverBootstrap.bind(listenPort).sync();
            log.info("Tcp服务启动成功,端口: {}", listenPort);
            future.channel().closeFuture().addListener(x -> {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                log.info(future.channel().toString() + " Tcp服务链路关闭");
            });
        } catch (Exception e) {
            log.warn("Tcp服务启动异常", e);
        }
    }

    private static ApplicationContext application;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        application = applicationContext;
    }

    public static <T> T getObject(Class<T> clazz) {
        final T bean = application.getBean(clazz);
        return bean;
    }
}
