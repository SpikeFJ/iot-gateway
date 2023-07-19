package com.jfeng.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaConfig {

    @Bean
    public KafkaTemplate kafkaTemplate() {
        DefaultKafkaProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerConfigs());
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean("kafkaListenerKafkaProperties")
    @Primary
    @ConfigurationProperties(prefix = "spring.kafka")
    public KafkaProperties properties() {
        return new KafkaProperties();
    }

    private Map<String, Object> producerConfigs() {
        return properties().buildProducerProperties();
    }
}
