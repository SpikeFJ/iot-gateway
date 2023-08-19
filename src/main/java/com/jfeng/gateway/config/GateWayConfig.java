package com.jfeng.gateway.config;

import com.jfeng.gateway.session.OnlineStateChangeListener;
import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.listener.KafkaOnlineStateChangeListener;
import com.jfeng.gateway.session.listener.KafkaSessionListener;
import com.jfeng.gateway.session.listener.RedisOnlineStateChangeListener;
import com.jfeng.gateway.session.listener.RedisSessionListener;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
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

    private List<EventDataConfig> sourceData;
    private List<EventDataConfig> onlineData;

    private Map<String, String> parameter;
    private ApplicationContext applicationContext;

    @Bean
    public List<SessionListener> sessionListener() {
        List<SessionListener> listeners = new ArrayList<>();
        if (sourceData != null) {
            for (EventDataConfig config : sourceData) {
                if ("KAFKA".equalsIgnoreCase(config.getType())) {
                    listeners.add(applicationContext.getBean(KafkaSessionListener.class));
                } else if ("REDIS".equalsIgnoreCase(config.getType())) {
                    listeners.add(applicationContext.getBean(RedisSessionListener.class));
                }
            }
        }
        return listeners;
    }
    @Bean
    public List<OnlineStateChangeListener> onlineListener() {
        List<OnlineStateChangeListener> listeners = new ArrayList<>();
        if (sourceData != null) {
            for (EventDataConfig config : sourceData) {
                if ("KAFKA".equalsIgnoreCase(config.getType())) {
                    listeners.add(applicationContext.getBean(KafkaOnlineStateChangeListener.class));
                } else if ("REDIS".equalsIgnoreCase(config.getType())) {
                    listeners.add(applicationContext.getBean(RedisOnlineStateChangeListener.class));
                }
            }
        }
        return listeners;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

@Data
class EventDataConfig {
    private String type;
    private Map<String, String> topic;
}

