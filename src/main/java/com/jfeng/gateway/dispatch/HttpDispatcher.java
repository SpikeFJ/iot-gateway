package com.jfeng.gateway.dispatch;

import com.jfeng.gateway.message.DispatchMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * webhook处理分发数据,controler中需要提供回调入口方法
 */
@Component
public class HttpDispatcher implements Dispatcher {
    @Resource
    private RestTemplate restTemplate;

    @Value("${dispatch.topic:dispatchDst}")
    private String dispatchTopic;

    @Override
    public void sendNext(String packageId, DispatchMessage data) {
       // kafkaTemplate.send(dispatchTopic, packageId, JsonUtils.serialize(data));
    }
}