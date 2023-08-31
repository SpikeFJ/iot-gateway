package com.jfeng.gateway;

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
import java.io.UnsupportedEncodingException;

@SpringBootApplication
@Slf4j
public class GatewayApplication implements CommandLineRunner, ApplicationContextAware {

    @Resource
    public GateWayConfig config;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    KafkaTemplate kafkaTemplate;

    public static void main(String[] args) throws UnsupportedEncodingException {
//        byte[] a= new byte[]{0x36, 0x39, 0x36, 0x66, 0x37, 0x34, 0x32, 0x65, 0x36, 0x38, 0x36, 0x31, 0x36, 0x66, 0x36, 0x62, 0x37, 0x35, 0x36, 0x31, 0x36, 0x39, 0x32, 0x65, 0x36, 0x33, 0x36, 0x65};
//        System.out.println(Arrays.toString(a));
//        String ascii = new String(a,"ASCII");
//        System.out.println(ascii);
//
//        System.out.println(ByteBufUtil.decodeHexDump(ascii));
//        String s1 = new String(ByteBufUtil.decodeHexDump(ascii),"ASCII");
//        System.out.println(s1);
        SpringApplication.run(GatewayApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        startTcp(config);
    }

    private void startTcp(GateWayConfig config) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_RCVBUF, 1024 * 8).childOption(ChannelOption.SO_SNDBUF, 1024 * 8).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new IpFilterHandler(config.getBlackIpList(), config.getWhiteIpList()));
                    pipeline.addLast(new ModbusHandler(redisUtils, kafkaTemplate));
                }
            });

            int listenPort = config.getPort();
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
