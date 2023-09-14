package com.jfeng.gateway.up.dispatch;

import com.jfeng.gateway.message.DispatchMessage;
import com.jfeng.gateway.util.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * kafka数据分发器
 */
@Component
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
@Primary
public class KafkaDataDispatcher implements DataDispatcher {
    @Resource
    private KafkaTemplate kafkaTemplate;

    @Value("${dispatch.topic:dispatchDst}")
    private String dispatchTopic;

    @Override
    public void sendNext(String packageId, DispatchMessage data) {
        kafkaTemplate.send(dispatchTopic, packageId, JsonUtils.serialize(data));
    }
}
