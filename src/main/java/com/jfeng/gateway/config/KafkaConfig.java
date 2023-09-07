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


    @Bean
    CustomKafkaListener myKafkaListener() {
        return new CustomKafkaListener();
    }
//
//    @Bean
//    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, String>
//                factory = new ConcurrentKafkaListenerContainerFactory<>();
//        // 设置消费者工厂
//        factory.setConsumerFactory(consumerFactory());
//        // 消费者组中线程数量
//        factory.setConcurrency(3);
//        // 拉取超时时间
//        factory.getContainerProperties().setPollTimeout(3000);
//
//        // 当使用批量监听器时需要设置为true
//        factory.setBatchListener(true);
//
//        return factory;
//    }
//
//    @Bean
//    public ConsumerFactory<String, String> consumerFactory() {
//        return new DefaultKafkaConsumerFactory<>(producerConfigs());
//    }
//
//    //    @Bean
//    public Map<String, Object> consumerConfigs() {
//        Map<String, Object> propsMap = new HashMap<>();
//        // Kafka地址
//        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "112.125.26.68:9092");
//        //配置默认分组，这里没有配置+在监听的地方没有设置groupId，多个服务会出现收到相同消息情况
//        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, "defaultGroup");
//        // 是否自动提交offset偏移量(默认true)
//        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
//        // 自动提交的频率(ms)
//        propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
//        // Session超时设置
//        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
//        // 键的反序列化方式
//        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        // 值的反序列化方式
//        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        // offset偏移量规则设置：
//        // (1)、earliest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费
//        // (2)、latest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据
//        // (3)、none：topic各分区都存在已提交的offset时，从offset后开始消费；只要有一个分区不存在已提交的offset，则抛出异常
//        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        return propsMap;
//    }
}
