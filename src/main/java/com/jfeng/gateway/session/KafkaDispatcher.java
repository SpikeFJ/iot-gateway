package com.jfeng.gateway.session;

import com.jfeng.gateway.util.JsonUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * springboot处理接收数据
 */
@Component
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaDispatcher implements Dispatcher {
    @Resource
    private KafkaTemplate kafkaTemplate;

    @Override
    public void sendNext(String packageId, DispatchData data) {
        kafkaTemplate.send("", packageId, JsonUtils.serialize(data));
    }
}
