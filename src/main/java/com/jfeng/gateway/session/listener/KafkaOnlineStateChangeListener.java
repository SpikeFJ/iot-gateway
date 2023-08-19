package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.session.OnlineStateChangeListener;
import com.jfeng.gateway.session.TcpSession;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * kafka上下线监听器
 */
@Component
public class KafkaOnlineStateChangeListener implements OnlineStateChangeListener {
    @Resource
    private KafkaTemplate kafkaTemplate;

    @Override
    public void online(TcpSession tcpSession) {

    }

    @Override
    public void Offline(TcpSession tcpSession, String message) {

    }
}
