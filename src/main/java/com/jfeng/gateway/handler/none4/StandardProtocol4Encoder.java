package com.jfeng.gateway.handler.none4;

import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.channel.ClientChannel;
import com.jfeng.gateway.protocol.StandardProtocol4;
import com.jfeng.gateway.util.TransactionIdUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import static com.jfeng.gateway.handler.none4.StandardExtend4Decoder.CLIENT_CHANNEL_ATTRIBUTE_KEY;

/**
 * 国标消息编码
 */
@Slf4j
public class StandardProtocol4Encoder extends MessageToByteEncoder<StandardProtocol4> {
    @Override
    protected void encode(ChannelHandlerContext ctx, StandardProtocol4 protocol4, ByteBuf byteBuf) throws Exception {
        if (protocol4 == null)
            throw new Exception("缺少协议对象");

        protocol4.encode(byteBuf);
        ClientChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        if (client != null) {
            client.send(ByteBufUtil.getBytes(byteBuf));

            MDC.put(Constant.LOG_TRANSACTION_ID, TransactionIdUtils.get(client.getShortChannelId()));
            log.info("发送<<:" + ByteBufUtil.hexDump(byteBuf));
            MDC.remove(Constant.LOG_TRANSACTION_ID);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof StandardProtocol4) {
            super.write(ctx, msg, promise);
        } else {
            ByteBuf data = (ByteBuf) msg;
            ctx.writeAndFlush(data);
        }
    }
}
