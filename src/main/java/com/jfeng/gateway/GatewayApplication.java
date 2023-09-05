package com.jfeng.gateway;

import com.jfeng.gateway.config.GateWayConfig;
import com.jfeng.gateway.server.TcpServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.kafka.annotation.KafkaListener;

import javax.annotation.Resource;

@SpringBootApplication
@Slf4j
public class GatewayApplication implements CommandLineRunner, ApplicationContextAware {
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

    /**
     * 处理下发命令（Kafka）
     *
     * @param records
     */
    @KafkaListener(topics = {"${gateway.send.topic}"})
    public void receiveMessage(ConsumerRecord<String, String> records) {
        try {
            final String value = records.value();
            log.debug("接收消息: " + value + ",当前队列长度：");
            tcpServer.fromKafka(value);
        } catch (Exception e) {
            log.warn("一般上行数据处理异常：", e);
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
