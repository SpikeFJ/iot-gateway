package com.jfeng.gateway;

import com.jfeng.gateway.config.GateWayConfig;
import com.jfeng.gateway.server.TcpServer;
import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;
import java.util.List;

@SpringBootApplication
@Slf4j
public class GatewayApplication implements CommandLineRunner, ApplicationContextAware {

    @Resource
    public GateWayConfig config;
    @Resource
    public List<SessionListener> sessionListener;
    @Resource
    public RedisUtils redisUtils;


    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        if ("TCP".equalsIgnoreCase(config.getAccessType()) && config.isEnable()) {
            TcpServer tcpServer = new TcpServer();
            tcpServer.setSessionListeners(sessionListener);
            tcpServer.setRedisUtils(redisUtils);

            tcpServer.init(null);
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
