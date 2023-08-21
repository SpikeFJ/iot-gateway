package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.message.DeviceMessage;
import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.JsonUtils;
import io.netty.buffer.ByteBufUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * kafka会话监听器，接收到事件后写入kafka对应的主题中
 */
@Component
@Setter
@Slf4j
public class KafkaNotifySessionListener implements SessionListener {
    private final String ROOT_TOPIC = Constant.SYSTEM_PREFIX + "hex_data";
    private final String CONNECT = "connect";
    private final String SEND = "send";
    private final String RECEIVE = "receive";
    private final String RECEIVE_COMPLETE = "receive_complete";
    private final String CLOSE = "CLOSE";

    private final String ONLINE = "ONLINE";
    private final String OFFLINE = "OFFLINE";

    private Map<String, String> topics;

    public void init(Map<String, String> topics) {
        this.topics.put("ROOT_TOPIC", ROOT_TOPIC);
        this.topics = topics;
    }


    @Resource
    private KafkaTemplate kafkaTemplate;

    @Override
    public void onConnect(TcpSession tcpSession) {
        String topic = topics.getOrDefault(CONNECT, "ROOT_TOPIC");

        DeviceMessage deviceMessage = DeviceMessage.createConnect(tcpSession.getPacketId(), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getPacketId(), JsonUtils.serialize(deviceMessage));
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {
        String topic = topics.getOrDefault(RECEIVE_COMPLETE, "ROOT_TOPIC");
        DeviceMessage deviceMessage = DeviceMessage.createReceiveComplete(tcpSession.getPacketId(), ByteBufUtil.hexDump(data), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getPacketId(), JsonUtils.serialize(deviceMessage));
    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        String topic = topics.getOrDefault(SEND, "ROOT_TOPIC");
        DeviceMessage deviceMessage = DeviceMessage.createSend(tcpSession.getPacketId(), ByteBufUtil.hexDump(data), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getPacketId(), JsonUtils.serialize(deviceMessage));
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {
        String topic = topics.getOrDefault(CLOSE, "ROOT_TOPIC");
        DeviceMessage deviceMessage = DeviceMessage.createDisConnect(tcpSession.getPacketId(), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getPacketId(), JsonUtils.serialize(deviceMessage));
    }

    @Override
    public void online(TcpSession tcpSession) {
        String topic = topics.getOrDefault(ONLINE, "ROOT_TOPIC");
        DeviceMessage deviceMessage = DeviceMessage.createOnline(tcpSession.getPacketId(), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getPacketId(), JsonUtils.serialize(deviceMessage));
    }

    @Override
    public void Offline(TcpSession tcpSession, String reason) {
        String topic = topics.getOrDefault(OFFLINE, "ROOT_TOPIC");
        DeviceMessage deviceMessage = DeviceMessage.createOnline(tcpSession.getPacketId(), tcpSession.getLocalAddress());

        kafkaTemplate.send(topic, tcpSession.getPacketId(), JsonUtils.serialize(deviceMessage));
    }
}

