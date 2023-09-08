package com.jfeng.gateway;

import com.jfeng.gateway.config.GateWayConfig;
import com.jfeng.gateway.server.TcpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

@SpringBootApplication
@Slf4j
public class GatewayApplication implements CommandLineRunner {
    @Resource
    public GateWayConfig config;

    @Resource
    public TcpServer tcpServer;

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        if ("TCP".equalsIgnoreCase(config.getAccessType()) && config.isEnable()) {
            tcpServer.setProtocol(config.getProtocol());
            tcpServer.init(null);
            tcpServer.start();
        }
    }
}
