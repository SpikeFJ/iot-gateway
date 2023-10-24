package com.jfeng.gateway.config;


import com.jfeng.gateway.server.TcpServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

/**
 * 自定义消费监听器
 */
@Slf4j
public class CustomKafkaListener {
    @Autowired
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
        } catch (Exception e) {
            log.warn("一般上行数据处理异常：", e);
        }
    }
}
