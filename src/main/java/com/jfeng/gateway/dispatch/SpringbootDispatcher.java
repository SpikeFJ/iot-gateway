package com.jfeng.gateway.dispatch;

import com.jfeng.gateway.message.DispatchMessage;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * springboot处理接收数据
 */
@Component()
@ConditionalOnMissingBean(KafkaDispatcher.class)
public class SpringbootDispatcher implements Dispatcher, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void sendNext(String packageId, DispatchMessage data) {
        applicationContext.publishEvent(data);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
