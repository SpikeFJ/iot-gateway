package com.jfeng.gateway.dispatch;

import com.jfeng.gateway.message.DispatchMessage;
import com.jfeng.gateway.util.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * kafka处理分发数据
 */
@Component
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaDispatcher implements Dispatcher {
    @Resource
    private KafkaTemplate kafkaTemplate;

    @Value("${dispatch.topic:dispatchDst}")
    private String dispatchTopic;

    @Override
    public void sendNext(String packageId, DispatchMessage data) {
        kafkaTemplate.send(dispatchTopic, packageId, JsonUtils.serialize(data));
    }
}
