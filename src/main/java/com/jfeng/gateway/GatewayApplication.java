package com.jfeng.gateway;

import com.jfeng.gateway.config.GateWayConfig;
import com.jfeng.gateway.server.TcpServer;
import com.jfeng.gateway.session.SessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;
import java.util.List;

@SpringBootApplication
@Slf4j
public class GatewayApplication implements CommandLineRunner {
    @Resource
    public GateWayConfig config;

    @Autowired(required = false)
    public TcpServer tcpServer;

    @Resource
    private List<SessionListener> sessionListener;

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        if ("TCP".equalsIgnoreCase(config.getAccessType()) && config.isEnable()) {
            if (tcpServer == null) {
                log.warn("初始化TCP服务端失败");
                return;
            }
            tcpServer.setProtocol(config.getProtocol());
            tcpServer.setPort(config.getPort());
            tcpServer.addListener(sessionListener);
            tcpServer.init(null);
            tcpServer.start();
        } else if ("UDP".equalsIgnoreCase(config.getAccessType()) && config.isEnable()) {

        } else if ("MQTT".equalsIgnoreCase(config.getAccessType()) && config.isEnable()) {

        }
    }
}
