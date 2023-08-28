package com.jfeng.gateway.protocol.none4;

import com.jfeng.gateway.session.TcpSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 国标消息编码
 */
@Slf4j
public class StandardProtocol4Encoder extends MessageToByteEncoder<StandardProtocol4> {
    @Override
    protected void encode(ChannelHandlerContext ctx, StandardProtocol4 protocol4, ByteBuf byteBuf) throws Exception {
        if (protocol4 != null) {
            protocol4.encode(byteBuf);
            TcpSession client = ctx.channel().attr(StandardExtend4Decoder.SESSION_KEY).get();
            if (client != null) {
                client.send(byteBuf.array());
            }
        }
    }
}
