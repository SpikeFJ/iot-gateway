package com.jfeng.gateway.config;

import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.listener.KafkaSessionListener;
import com.jfeng.gateway.session.listener.RedisSessionListener;
import com.jfeng.gateway.util.RedisUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
                    KafkaSessionListener kafkaListener = applicationContext.getBean(KafkaSessionListener.class);
                    kafkaListener.init(config.getTopic());

                    listeners.add(kafkaListener);
                } else if ("REDIS".equalsIgnoreCase(config.getType())) {
                    RedisSessionListener redisListener = applicationContext.getBean(RedisSessionListener.class);
                    redisListener.init(config.getTopic());

                    listeners.add(redisListener);
                }
            }
        }
        return listeners;
    }


    @Resource
    private RedisUtils redisUtils;

    @PostConstruct
    private void loadFromDb() {
        if (redisUtils.get("") != null) {
            //replace sourceData
            //
        }
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

