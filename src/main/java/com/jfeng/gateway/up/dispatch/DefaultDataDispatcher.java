package com.jfeng.gateway.up.dispatch;

import com.jfeng.gateway.message.DispatchMessage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 默认数据分发器(采用springboot内部广播方式)
 */
@Component
public class DefaultDataDispatcher implements DataDispatcher, ApplicationContextAware {
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
