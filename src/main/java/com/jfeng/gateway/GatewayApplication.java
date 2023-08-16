package com.jfeng.gateway;

import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.config.GateWayConfig;
import com.jfeng.gateway.server.TcpServer;
import jdk.internal.joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@Slf4j
public class GatewayApplication implements CommandLineRunner, ApplicationContextAware {

    @Resource
    public GateWayConfig config;

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class);
    }


    @Override
    public void run(String... args) throws Exception {
        if (config.tcp != null && config.tcp.isEnable()) {
            TcpServer tcpServer = new TcpServer();
            tcpServer.start(parseTcpParameter(config.getTcp()));
        }
        if (config.udp != null && config.udp.isEnable()) {

        }
    }

    private Map<String, String> parseTcpParameter(GateWayConfig.ConfigItem configItem) {
        Map<String, String> tcpParameters = new HashMap<>();
        tcpParameters.put(Constant.PORT, String.valueOf(configItem.getPort()));
        tcpParameters.put(Constant.BLACK_IP_LIST, Strings.join(configItem.getBlackIpList(), ","));
        tcpParameters.put(Constant.WHITE_IP_LIST, Strings.join(configItem.getBlackIpList(), ","));
        tcpParameters.putAll(configItem.getParameter());
        return tcpParameters;
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
