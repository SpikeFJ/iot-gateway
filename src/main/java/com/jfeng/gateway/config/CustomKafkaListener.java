package com.jfeng.gateway.config;


import com.jfeng.gateway.server.TcpServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

import javax.annotation.Resource;

/**
 * 自定义消费监听器
 */
@Slf4j
public class CustomKafkaListener {
    @Resource
    TcpServer tcpServer;

    /**
     * 处理下发命令（Kafka）
     *
     * @param records
     */
    @KafkaListener(topics = {"${gateway.down.topic}"})
    public void receiveMessage(ConsumerRecord<String, String> records) {
        try {
            final String value = records.value();
            log.debug("接收消息: " + value + ",当前队列长度：");
            tcpServer.fromKafka(value);
        } catch (Exception e) {
            log.warn("一般上行数据处理异常：", e);
        }
    }
}
