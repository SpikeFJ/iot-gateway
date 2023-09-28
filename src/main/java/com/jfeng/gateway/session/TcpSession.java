package com.jfeng.gateway.session;

import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.server.TcpServer;
import com.jfeng.gateway.util.DateTimeUtils;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.FIFO;
import com.jfeng.gateway.util.Utils;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jfeng.gateway.protocol.none4.StandardExtend4Decoder.SESSION_KEY;

/**
 * Tcp会话
 */
@Getter
@Setter
@Slf4j
public class TcpSession {
    private String channelId;//物理通道链路唯一标识
    private String shortChannelId;//物理通道链路唯一标识(简写用于日志)
    private String deviceId;//设备Id，表明唯一设备，需要再协议有所体现
    private String bId;// 业务层面唯一标识

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
    private TcpServer tcpServer;
    private AtomicBoolean isSending = new AtomicBoolean(false);

    private Map<String, Object> tag = new HashMap<>();
    private List<SessionListener> sessionListeners = new ArrayList<>();

    private FIFO<HistroyRecord> histroyRecordFIFO;

    public TcpSession(Channel channel, TcpServer tcpServer) {
        this.channel = channel;
        this.tcpServer = tcpServer;

        this.sessionStatus = SessionStatus.CONNECTED;
        this.channelId = this.channel.id().asLongText();
        this.shortChannelId = this.channel.id().asShortText();
        this.remoteAddress = Utils.getAddressInfo(channel.remoteAddress());
        this.localAddress = Utils.getAddressInfo(channel.localAddress());
        this.createTime = ZonedDateTime.now().toInstant().toEpochMilli();

        addListeners(this.tcpServer);
    }

    private void addListeners(TcpServer tcpServer) {
        this.sessionListeners.add(tcpServer);
    }


    public void connect() {
        log.info("连接建立,建立时间：" + DateTimeUtils.outEpochMilli(createTime));
        this.sessionStatus = SessionStatus.CONNECTED;

        sessionListeners.stream().forEach(x -> x.onConnect(this));
    }

    public void checkDuplicate(String packetId) {
        if (this.tcpServer.contains(packetId)) {
            TcpSession old = this.tcpServer.getOnLines().get(packetId);
            old.close("重复登录");
        }
    }

    public void login() {
        log.info("登陆");
        this.sessionStatus = SessionStatus.LOGIN;

        sessionListeners.stream().forEach(x -> x.online(this));
    }


    public void receive(byte[] receive) {
        this.receivedBytes += receive.length;
        receivedPackets += 1;
        this.lastReadTime = ZonedDateTime.now().toInstant().toEpochMilli();

        sessionListeners.stream().forEach(x -> x.onReceive(this, receive));
    }

    public void receiveComplete(byte[] receive) {
        sessionListeners.stream().forEach(x -> x.onReceiveComplete(this, receive));
    }

    public void send(byte[] send) {
        send(send, false);
    }

    public void send(byte[] send, boolean writeAndFlush) {
        this.sendBytes += send.length;
        sendPackets += 1;
        this.lastWriteTime = ZonedDateTime.now().toInstant().toEpochMilli();
        if (writeAndFlush) {
            channel.writeAndFlush(send);
        }
        sessionListeners.stream().forEach(x -> x.onSend(this, send));
    }

    public void close(String closeReason) {
        //由于关闭操作有可能是在其他线程中操作
        if (this.channel != null && (MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().size() == 0)) {
            MDC.put(Constant.LOG_ADDRESS, getChannel().toString());
        }

        log.warn("连接关闭，原因:" + closeReason + ".连接信息：" + this);
        MDC.remove(Constant.LOG_ADDRESS);
        MDC.remove(Constant.LOG_TRANSACTION_ID);

        if (this.histroyRecordFIFO != null) {
            this.histroyRecordFIFO.clear();
        }
        this.sessionStatus = SessionStatus.CLOSED;
        this.closeReason = closeReason;
        this.channel.attr(SESSION_KEY).getAndSet(null);
        if (this.channel != null) {
            channel.close();
        }
        sessionListeners.stream().forEach(x -> x.onDisConnect(this, closeReason));
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(",").append(remoteAddress);
        s.append(",").append("当前状态:").append(sessionStatus);
        s.append(",创建时间:").append(DateTimeUtils.outEpochMilli(createTime));
        if (lastReadTime != 0) {
            s.append(",最后接收时间:").append(DateTimeUtils.outEpochMilli(lastReadTime));
        }
        if (lastWriteTime != 0) {
            s.append(",最后发送时间:").append(DateTimeUtils.outEpochMilli(lastWriteTime));
        }

        return s.substring(1);
    }

    public Map<String, Object> toOnlineJson() {
        Map<String, Object> onlineJson = new HashMap<>();
        onlineJson.put("createTime", DateTimeUtils2.outString(createTime, "yyyy-MM-dd HH:mm:ss"));
        onlineJson.put("remoteAddress", remoteAddress);
        onlineJson.put("sendPackets", sendPackets);
        onlineJson.put("receivedPackets", receivedPackets);
        onlineJson.put("lastReadTime", DateTimeUtils2.outString(lastReadTime, "yyyy-MM-dd HH:mm:ss"));
        onlineJson.put("deviceId", deviceId);
        onlineJson.put("businessId", bId);
        return onlineJson;
    }

    public Map<String, Object> toConnectJson() {
        Map<String, Object> onlineJson = new HashMap<>();
        onlineJson.put("createTime", DateTimeUtils2.outString(createTime, "yyyy-MM-dd HH:mm:ss"));
        onlineJson.put("remoteAddress", remoteAddress);
        onlineJson.put("sendPackets", sendPackets);
        onlineJson.put("receivedPackets", receivedPackets);
        onlineJson.put("lastReadTime", lastReadTime == 0 ? "" : DateTimeUtils2.outString(lastReadTime, "yyyy-MM-dd HH:mm:ss"));
        return onlineJson;
    }
}
