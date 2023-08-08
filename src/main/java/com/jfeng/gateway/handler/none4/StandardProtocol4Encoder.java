package com.jfeng.gateway.handler.none4;

import com.jfeng.gateway.protocol.StandardProtocol4;
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
        if (protocol4 == null) throw new Exception("缺少协议对象");

        protocol4.encode(byteBuf);
    }
}