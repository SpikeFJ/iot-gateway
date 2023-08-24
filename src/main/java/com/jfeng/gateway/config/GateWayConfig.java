package com.jfeng.gateway.config;

import com.jfeng.gateway.dispatch.*;
import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.listener.KafkaNotifySessionListener;
import com.jfeng.gateway.session.listener.RedisNotifySessionListener;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "gateway")
@Getter
@Setter
public class GateWayConfig implements ApplicationContextAware {
    private String accessType;//tcp udp mqtt
    private boolean enable = false;
    private int port;
    private String protocol;

    private List<NotifyConfig> notification;
    @Value("${dispatch.type:spring}")
    private String dispatchType;
    private Map<String, String> parameter;

    private ApplicationContext applicationContext;

    @Bean
    public List<SessionListener> sessionListener() {
        List<SessionListener> listeners = new ArrayList<>();
        if (notification != null) {
            for (NotifyConfig config : notification) {
                if ("KAFKA".equalsIgnoreCase(config.getType())) {
                    KafkaNotifySessionListener kafkaListener = applicationContext.getBean(KafkaNotifySessionListener.class);
                    kafkaListener.init(config.getTopic());

                    listeners.add(kafkaListener);
                } else if ("REDIS".equalsIgnoreCase(config.getType())) {
                    RedisNotifySessionListener redisListener = applicationContext.getBean(RedisNotifySessionListener.class);
                    redisListener.init(config.getTopic());

                    listeners.add(redisListener);
                }
            }
        }
        return listeners;
    }

    @Bean
    public Dispatcher dispatcher() {
        Dispatcher dispatcher = applicationContext.getBean(SpringbootDispatcher.class);
        if ("kafka".equalsIgnoreCase(dispatchType)) {
            dispatcher = applicationContext.getBean(KafkaDispatcher.class);
        } else if ("http".equalsIgnoreCase(dispatchType)) {
            dispatcher = applicationContext.getBean(HttpDispatcher.class);
        } else if ("webhook".equalsIgnoreCase(dispatchType)) {
            dispatcher = applicationContext.getBean(WebhookDispatcher.class);
        }
        return dispatcher;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

@Data
class NotifyConfig {
    private String type;
    private Map<String, String> topic;
}

