package com.jfeng.gateway.protocol.none4;

import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.server.TcpServer;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.TransactionIdUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import static com.jfeng.gateway.protocol.none4.StandardExtend4Decoder.SESSION_KEY;

/**
 * 统计处理器（包括收单个连接的收发数据、连接、断开次数)
 */
@Slf4j
public class StatisticsHandler extends ChannelDuplexHandler {
    private final TcpServer tcpServer;

    public StatisticsHandler(TcpServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("建立连接", ctx.channel().remoteAddress());

        Channel channel = ctx.channel();
        TcpSession tcpSession = new TcpSession(channel, tcpServer);
        channel.attr(SESSION_KEY).set(tcpSession);
        MDC.put(Constant.LOG_ADDRESS, channel.toString());

        tcpSession.connect();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        TcpSession client = ctx.channel().attr(SESSION_KEY).get();

        ByteBuf byteBuf = (ByteBuf) msg;
        client.receive(ByteBufUtil.getBytes(byteBuf));

        MDC.put(Constant.LOG_TRANSACTION_ID, TransactionIdUtils.get(client.getShortChannelId()));
        log.info("接收>>:" + ByteBufUtil.hexDump(byteBuf));
        MDC.remove(Constant.LOG_TRANSACTION_ID);

        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        TcpSession client = ctx.channel().attr(SESSION_KEY).get();
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
        TcpSession client = ctx.channel().attr(SESSION_KEY).get();
        if (client != null) {
            client.close("主动断开");
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常断开", cause);
        TcpSession client = ctx.channel().attr(SESSION_KEY).get();
        if (client != null) {
            client.close(cause.getMessage());
        }
        super.exceptionCaught(ctx, cause);
    }
}
