package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.TcpSession;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * kafka会话监听器，接收到事件后写入kafka对应的主题中
 */
@Component
public class KafkaSessionListener  implements SessionListener {
    private final String ROOT_TOPIC = "abc";

    @Resource
    private KafkaTemplate kafkaTemplate;

    @Override
    public void onConnect(TcpSession tcpSession) {

    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {

    }
}
