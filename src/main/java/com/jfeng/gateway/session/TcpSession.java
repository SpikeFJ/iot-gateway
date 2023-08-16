package com.jfeng.gateway.session;

import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.util.DateTimeUtils;
import com.jfeng.gateway.util.Utils;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.time.ZonedDateTime;
import java.util.*;

import static com.jfeng.gateway.handler.none4.StandardExtend4Decoder.CLIENT_CHANNEL_ATTRIBUTE_KEY;

/**
 * Tcp会话
 */
@Getter
@Setter
@Slf4j
public class TcpSession {
    private String channelId;//物理通道链路唯一标识
    private String shortChannelId;//物理通道链路唯一标识(简写用于日志)
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
    private SessionStatus sessionStatus;
    private String remoteAddress;
    private String localAddress;
    private TcpSessionManage<TcpSession> tcpSessionManage;

    private Map<String, Object> tag = new HashMap<>();
    private List<SessionListener> channelListeners = new ArrayList<>();
    private List<OnlineStateChangeListener> onlineStateListeners = new ArrayList<>();

    public TcpSession(Channel channel, TcpSessionManage<TcpSession> tcpSessionManage) {
        this.channel = channel;
        this.tcpSessionManage = tcpSessionManage;
        this.channelListeners.add(tcpSessionManage);
        this.onlineStateListeners.add(tcpSessionManage);

        this.sessionStatus = SessionStatus.INITIAL;
        this.channelId = this.channel.id().asLongText();
        this.shortChannelId = this.channel.id().asShortText();
        this.remoteAddress = Utils.getAddressInfo(channel.remoteAddress());
        this.localAddress = Utils.getAddressInfo(channel.localAddress());
        this.createTime = ZonedDateTime.now().toInstant().toEpochMilli();
    }


    public void connect() {
        log.info("连接建立,建立时间：" + DateTimeUtils.outEpochMilli(createTime));
        this.sessionStatus = SessionStatus.CONNECTED;
        channelListeners.stream().forEach(x -> x.onConnect(this));
    }

    public void checkDupdicate(String packetId) {
        if (this.tcpSessionManage.contains(packetId)) {
            TcpSession old = this.tcpSessionManage.getOnLines().get(packetId);
            old.close("重复登录");
        }
    }

    public void login() {
        log.info("登陆");
        this.sessionStatus = SessionStatus.LOGIN;
        onlineStateListeners.stream().forEach(x -> x.online(this));
    }


    public void receive(byte[] receive) {
        this.receivedBytes += receive.length;
        receivedPackets += 1;
        this.lastReadTime = ZonedDateTime.now().toInstant().toEpochMilli();

        channelListeners.stream().forEach(x -> x.onReceive(this, receive));
    }

    public void receiveComplete(byte[] receive) {
        channelListeners.stream().forEach(x -> x.onReceiveComplete(this, receive));
    }

    public void send(byte[] send) {
        this.sendBytes += send.length;
        sendPackets += 1;
        this.lastWriteTime = ZonedDateTime.now().toInstant().toEpochMilli();

        channelListeners.stream().forEach(x -> x.onSend(this, send));
    }

    public void close(String closeReason) {
        //由于关闭操作有可能是在其他线程中操作
        if (this.channel != null && (MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().size() == 0)) {
            MDC.put(Constant.LOG_ADDRESS, getChannel().toString());
        }

        log.warn("连接关闭，原因:" + closeReason + ".连接信息：" + this);
        MDC.remove(Constant.LOG_ADDRESS);
        MDC.remove(Constant.LOG_TRANSACTION_ID);

        this.sessionStatus = SessionStatus.CLOSED;
        this.closeReason = closeReason;
        this.channel.attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).getAndSet(null);
        if (this.channel != null) {
            channel.close();
        }
        channelListeners.stream().forEach(x -> x.onDisConnect(this, closeReason));
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