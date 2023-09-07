package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.message.EventMessage;
import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.JsonUtils;
import io.netty.buffer.ByteBufUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * kafka会话监听器，接收到事件后写入kafka对应的主题中
 */
@Component
@Setter
@Slf4j
public class KafkaSessionListener implements SessionListener {
    private final String CONNECT = "connect";
    private final String SEND = "send";
    private final String RECEIVE = "receive";
    private final String CLOSE = "close";
    private final String ONLINE = "online";
    private final String OFFLINE = "offline";
    private final String DEFAULT = "default";

    private Map<String, String> topics;


    @PostConstruct
    public void init() {
        this.topics = new HashMap<>();
        this.topics.put("default","default");
    }


    @Resource
    private KafkaTemplate kafkaTemplate;

    @Override
    public void onConnect(TcpSession tcpSession) {
        String topic = topics.getOrDefault(CONNECT, DEFAULT);

        EventMessage eventMessage = EventMessage.createConnect(tcpSession.getDeviceId(), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getDeviceId(), JsonUtils.serialize(eventMessage));
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {
        String topic = topics.getOrDefault(RECEIVE, DEFAULT);
        EventMessage eventMessage = EventMessage.createReceiveComplete(tcpSession.getDeviceId(), ByteBufUtil.hexDump(data), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getDeviceId(), JsonUtils.serialize(eventMessage));
    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        String topic = topics.getOrDefault(SEND, DEFAULT);
        EventMessage eventMessage = EventMessage.createSend(tcpSession.getDeviceId(), ByteBufUtil.hexDump(data), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getDeviceId(), JsonUtils.serialize(eventMessage));
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {
        String topic = topics.getOrDefault(CLOSE, DEFAULT);
        EventMessage eventMessage = EventMessage.createDisConnect(tcpSession.getDeviceId(), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getDeviceId(), JsonUtils.serialize(eventMessage));
    }

    @Override
    public void online(TcpSession tcpSession) {
        String topic = topics.getOrDefault(ONLINE, DEFAULT);
        EventMessage eventMessage = EventMessage.createOnline(tcpSession.getDeviceId(), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getDeviceId(), JsonUtils.serialize(eventMessage));
    }

    @Override
    public void Offline(TcpSession tcpSession, String reason) {
        String topic = topics.getOrDefault(OFFLINE, DEFAULT);
        EventMessage eventMessage = EventMessage.createOnline(tcpSession.getDeviceId(), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getDeviceId(), JsonUtils.serialize(eventMessage));
    }
}

