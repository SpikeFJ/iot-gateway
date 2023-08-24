package com.jfeng.gateway;

import com.jfeng.gateway.comm.DeviceInfo;
import com.jfeng.gateway.config.GateWayConfig;
import com.jfeng.gateway.handler.none4.IpFilterHandler;
import com.jfeng.gateway.handler.none4.ModbusHandler;
import com.jfeng.gateway.util.RedisUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.kafka.core.KafkaTemplate;

import javax.annotation.Resource;

@SpringBootApplication
@Slf4j
public class GatewayApplication implements CommandLineRunner, ApplicationContextAware {

    @Resource
    public GateWayConfig config;
    @Resource
    private DeviceInfo deviceInfo;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    KafkaTemplate kafkaTemplate;

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        if (config.tcp != null && config.tcp.isEnable()) {
            startTcp(config.getTcp());
        }
    }

    private void startTcp(GateWayConfig.ConfigItem tcpItem) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_RCVBUF, 1024 * 8).childOption(ChannelOption.SO_SNDBUF, 1024 * 8).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new IpFilterHandler(tcpItem.getBlackIpList(), tcpItem.getWhiteIpList()));
                    pipeline.addLast(new ModbusHandler(redisUtils, kafkaTemplate));
                }
            });

            int listenPort = tcpItem.getPort();
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
            ChannelFuture future = serverBootstrap.bind(listenPort).sync();
            log.info("Tcp(Modbus)服务启动成功,端口: {}", listenPort);
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
