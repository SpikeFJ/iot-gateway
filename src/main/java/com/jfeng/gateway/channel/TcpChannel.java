package com.jfeng.gateway.channel;

import com.jfeng.gateway.comm.CollectionSetting;
import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.comm.Modbus;
import com.jfeng.gateway.comm.ModbusResp;
import com.jfeng.gateway.util.DateTimeUtils;
import com.jfeng.gateway.util.JsonUtils;
import com.jfeng.gateway.util.StringUtils;
import com.jfeng.gateway.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.time.ZonedDateTime;
import java.util.*;

import static com.jfeng.gateway.handler.none4.ModbusHandler.CLIENT_CHANNEL_ATTRIBUTE_KEY;

/**
 * Tcp通道连接
 */
@Getter
@Setter
@Slf4j
public class TcpChannel {
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
    private ChannelStatus channelStatus;
    private String remoteAddress;
    private String localAddress;
    private TcpManage<TcpChannel> tcpManage;

    private Map<String, Object> tag = new HashMap<>();
    private List<ChannelEventListener> channelListeners = new ArrayList<>();
    private List<OnlineStateChangeListener> onlineStateListeners = new ArrayList<>();

    public TcpChannel(Channel channel, TcpManage<TcpChannel> tcpManage) {
        this.channel = channel;
        this.tcpManage = tcpManage;
        this.channelListeners.add(tcpManage);
        this.onlineStateListeners.add(tcpManage);

        this.channelStatus = ChannelStatus.INITIAL;
        this.channelId = this.channel.id().asLongText();
        this.shortChannelId = this.channel.id().asShortText();
        this.remoteAddress = Utils.getAddressInfo(channel.remoteAddress());
        this.localAddress = Utils.getAddressInfo(channel.localAddress());
        this.createTime = ZonedDateTime.now().toInstant().toEpochMilli();
    }


    public void connect() {
        log.info("连接建立,建立时间：" + DateTimeUtils.outEpochMilli(createTime));
        this.channelStatus = ChannelStatus.CONNECTED;
        channelListeners.stream().forEach(x -> x.onConnect(this));
    }

    public void checkDuplicate(String packetId) {
        if (this.tcpManage.contains(packetId)) {
            TcpChannel old = this.tcpManage.getOnLines().get(packetId);
            old.close("重复登录");
        }
    }

    public void login() {
        log.info("登陆");
        this.channelStatus = ChannelStatus.LOGIN;
        onlineStateListeners.stream().forEach(x -> x.online(this));
    }

    Sending[] toSend;
    private String dtuCode;
    private byte[] heartCode;
    private String heartCodeStr;
    int sendingIndex;

    public void initSetting(CollectionSetting collectionSetting) throws Exception {
        if (collectionSetting != null) {
            this.dtuCode = collectionSetting.getDtuCode();
            this.heartCodeStr = collectionSetting.getHeartCode();
            this.heartCode = StringUtils.isNotEmpty(collectionSetting.getHeartCode()) ? collectionSetting.getHeartCode().getBytes("ASCII") : null;
            toSend = new Sending[collectionSetting.getModbusList().size()];
            for (int i = 0; i < collectionSetting.getModbusList().size(); i++) {
                toSend[i] = new Sending(collectionSetting.getModbusList().get(i));
            }
        }

        log.info("初始化配置：" + Arrays.toString(toSend));
    }

    public void receiveResp(ByteBuf byteBuf) {
        if (Arrays.equals(this.heartCode, ByteBufUtil.getBytes(byteBuf))) {
            log.info("心跳：" + heartCodeStr);
        } else if (toSend != null) {
            if (sendingIndex < toSend.length) {
                ModbusResp receive = toSend[sendingIndex].receive(byteBuf);
                log.info("当前帧:" + (sendingIndex + 1) + ",总帧数:" + toSend.length + ",解析数据" + JsonUtils.serialize(receive.getData()));

                tcpManage.getKafkaTemplate().send("parse_out_tahsensor", packetId, JsonUtils.serialize(receive.getData()));
            }

            if (sendingIndex + 1 < toSend.length) {
                sendingIndex++;
                //log.info("准备发送下一帧:" + sendingIndex + ",总数:" + toSend.length);
                sendNext();
            } else {
                log.info("本次发送结束");
                sendingIndex = 0;
            }
        } else {
            log.info("未知数据");
        }
    }

    public void sendNext() {
        ByteBuf buffer = Unpooled.buffer(8);
        toSend[sendingIndex].send(buffer);
        toSend[sendingIndex].sendTime = System.currentTimeMillis();
        channel.writeAndFlush(buffer);
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

        this.channelStatus = ChannelStatus.CLOSED;
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


    class Sending {
        public Sending(Modbus modbus) {
            this.modbus = modbus;
            sendTime = -1;
        }


        public void send(ByteBuf out) {
            this.modbus.send(out);
            this.sendTime = System.currentTimeMillis();
        }

        public ModbusResp receive(ByteBuf in) {
            if (System.currentTimeMillis() - toSend[sendingIndex].sendTime > 3000) {
                log.info("数据返回超时");
            }
            this.sendTime = -1;
            return this.modbus.receive(in);
        }

        Modbus modbus;
        long sendTime;

        @Override
        public String toString() {
            return "Sending{" + "modbus=" + modbus + ", sendTime=" + sendTime + '}';
        }
    }
}
