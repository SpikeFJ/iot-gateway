package com.jfeng.gateway.handler.none4;

import com.jfeng.gateway.channel.TcpChannel;
import com.jfeng.gateway.channel.TcpManage;
import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.util.TransactionIdUtils;
import com.jfeng.gateway.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * 事件统计handler（包括收单个连接的收发数据、连接、断开次数)
 */
@Slf4j
public class EventStatisticsHandler extends ChannelDuplexHandler {

    public static final AttributeKey<TcpChannel> CLIENT_CHANNEL_ATTRIBUTE_KEY = AttributeKey.valueOf("ClientChannel");

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
        client.receive(ByteBufUtil.getBytes(byteBuf));
        log.debug("接收数据(原始)：" + ByteBufUtil.hexDump(byteBuf));
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        TcpChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        if (client != null) {
            ByteBuf byteBuf = (ByteBuf) msg;
            client.send(ByteBufUtil.getBytes(byteBuf));

            MDC.put(Constant.LOG_TRANSACTION_ID, TransactionIdUtils.get(client.getShortChannelId()));
            log.info("发送<<:" + ByteBufUtil.hexDump(byteBuf));
            MDC.remove(Constant.LOG_TRANSACTION_ID);
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
        TcpChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        if (client != null) {
            client.close("异常断开：" + cause.getMessage());
        }
        super.exceptionCaught(ctx, cause);
    }
}
