package com.jfeng.gateway.handler.none4;

import com.jfeng.gateway.channel.ChannelStatus;
import com.jfeng.gateway.channel.TcpChannel;
import com.jfeng.gateway.channel.TcpManage;
import com.jfeng.gateway.comm.CollectionSetting;
import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.util.JsonUtils;
import com.jfeng.gateway.util.RedisUtils;
import com.jfeng.gateway.util.StringUtils;
import com.jfeng.gateway.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * Modbus handler
 */
@Slf4j
@Data
public class ModbusHandler extends ChannelDuplexHandler {
    public static final AttributeKey<TcpChannel> CLIENT_CHANNEL_ATTRIBUTE_KEY = AttributeKey.valueOf("ClientChannel");
    RedisUtils redisUtils;
    KafkaTemplate kafkaTemplate;

    public ModbusHandler(RedisUtils redisUtils, KafkaTemplate kafkaTemplate) {
        this.redisUtils = redisUtils;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        TcpChannel tcpChannel = new TcpChannel(channel, TcpManage.getInstance(Utils.getAddressInfo(channel.localAddress())));
        channel.attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).set(tcpChannel);

        MDC.put(Constant.LOG_ADDRESS, channel.toString());
        tcpChannel.connect();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        TcpChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();

        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        client.receive(bytes);

        log.info("接收<<:" + ByteBufUtil.hexDump(byteBuf));

        if (client.getChannelStatus() == ChannelStatus.CONNECTED) {
            checkDtuCode(client, bytes);
        } else if (client.getChannelStatus() == ChannelStatus.LOGIN) {
            client.receiveResp(byteBuf);
        }
        super.channelRead(ctx, msg);
    }

    private void checkDtuCode(TcpChannel tcpChannel, byte[] bytes) throws Exception {
        String dtuCode = new String(bytes, Charset.forName("ASCII"));
        tcpChannel.setPacketId(dtuCode);
        tcpChannel.login();
        log.info("dtu编号:" + dtuCode);

        String strDeviceInfo = redisUtils.get("dtu:device:" + dtuCode);
        if (StringUtils.isEmpty(strDeviceInfo)) {
            log.warn("该设备未设置相关采集参数");
            return;
        }
        CollectionSetting collectionSetting = JsonUtils.deserialize(strDeviceInfo, CollectionSetting.class);
        if (collectionSetting == null) {
            log.warn("该设备采集参数配置错误");
            return;
        }

        tcpChannel.initSetting(collectionSetting);
        Channel channel = tcpChannel.getChannel();
        channel.eventLoop().scheduleAtFixedRate(() -> {
            log.info("定时发送");
            tcpChannel.sendNext();
        }, 10, collectionSetting.getConnectPeriod(), TimeUnit.SECONDS);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        TcpChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        if (client != null) {
            ByteBuf byteBuf = (ByteBuf) msg;
            client.send(ByteBufUtil.getBytes(byteBuf));

            log.info("发送<<:" + ByteBufUtil.hexDump(byteBuf));
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        TcpChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        if (client != null) {
            client.close("主动断开");
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常断开", cause.getMessage());
        TcpChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        if (client != null) {
            client.close(cause.getMessage());
        }
        super.exceptionCaught(ctx, cause);
    }
}
