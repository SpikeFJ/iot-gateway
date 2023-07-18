package com.jfeng.gateway.channel;

import com.jfeng.gateway.util.DateTimeUtils;
import com.jfeng.gateway.util.Utils;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.*;

import static com.jfeng.gateway.handler.none4.StandardExtend4Decoder.CLIENT_CHANNEL_ATTRIBUTE_KEY;

/**
 * 通道连接
 */
@Getter
@Setter
@Slf4j
public class ClientChannel {
    private String channelId;//物理通道链路唯一标识
    private String packetId;//协议层面（也就是可以从报文中提取的）唯一标识
    private String id;// 业务层面唯一标识:deviceId

    private long createTime;
    private long lastReadTime;
    private long lastWriteTime;
    private String closeReason;

    private long receivedBytes;
    private long receivedPackets;
    private long sendBytes;
    private long sendPackets;

    private Channel channel;
    private ChannelStatus channelStatus;
    private String remoteAddress;
    private String localAddress;


    private Map<String, Object> tag = new HashMap<>();
    private List<ChannelEventListener> listeners = new ArrayList<>();

    public ClientChannel(Channel channel) {
        this.channel = channel;
        this.channelStatus = ChannelStatus.INITIAL;
        this.channelId = this.channel.id().asLongText();
        this.remoteAddress = Utils.getAddressInfo(channel.remoteAddress());
        this.localAddress = Utils.getAddressInfo(channel.localAddress());
        this.createTime = ZonedDateTime.now().toInstant().toEpochMilli();

        log.info("建立连接：" + this);
    }

    public void connect() {
        this.channelStatus = ChannelStatus.CONNECTED;
        listeners.stream().forEach(x -> x.onConnect(this));
    }

    public void login() {
        this.channelStatus = ChannelStatus.LOGIN;
        listeners.stream().forEach(x -> x.online(this));
    }


    public void receive(byte[] receive) {
        this.receivedBytes += receive.length;
        receivedPackets += 1;
        this.lastReadTime = ZonedDateTime.now().toInstant().toEpochMilli();

        listeners.stream().forEach(x -> x.onReceive(this, receive));
    }

    public void send(byte[] send) {
        this.sendBytes += send.length;
        sendPackets += 1;
        this.lastWriteTime = ZonedDateTime.now().toInstant().toEpochMilli();

        listeners.stream().forEach(x -> x.onSend(this, send));
    }

    public void close(String closeReason) {
        this.channelStatus = ChannelStatus.CLOSED;
        this.closeReason = closeReason;
        this.channel.attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).getAndSet(null);
        if (this.channel != null) {
            channel.close();
        }
        listeners.stream().forEach(x -> x.onDisConnect(this, closeReason));
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(",").append(remoteAddress);
        s.append(",创建时间:" + DateTimeUtils.outEpochMilli(createTime));

        if (lastReadTime != 0) {
            s.append(",最后接收时间：" + DateTimeUtils.outEpochMilli(lastReadTime));
        }
        if (lastWriteTime != 0) {
            s.append(",最后发送时间：" + DateTimeUtils.outEpochMilli(lastWriteTime));
        }

        return s.substring(1);
    }
}
