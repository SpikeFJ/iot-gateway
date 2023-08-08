package com.jfeng.gateway.handler.none4;

import com.jfeng.gateway.channel.ChannelStatus;
import com.jfeng.gateway.channel.TcpChannel;
import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.protocol.StandardProtocol4;
import com.jfeng.gateway.util.TransactionIdUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.nio.ByteOrder;

/**
 * 车型协议处理器(国四扩展)
 */
@Slf4j
public class StandardExtend4Decoder extends LengthFieldBasedFrameDecoder {
    public static final Integer MAX_FRAME_LENGTH = 1024 * 1024;  //整帧最大允许长度
    public static final Integer LENGTH_FIELD_OFFSET = StandardProtocol4.HEADER_LENGTH + StandardProtocol4.COMMAND_LENGTH + StandardProtocol4.TERMINAL_LENGTH + StandardProtocol4.VERSION_LENGTH + StandardProtocol4.ENCRYPT_LENGTH;
    public static final Integer LENGTH_FIELD_LENGTH = 2;
    public static final Integer LENGTH_ADJUST = 1;
    public static final Integer BYTES_STRIP = 0;
    public static final boolean FAIL_FAST = true;
    public static final AttributeKey<TcpChannel> CLIENT_CHANNEL_ATTRIBUTE_KEY = AttributeKey.valueOf("ClientChannel");

    public StandardExtend4Decoder() {
        super(ByteOrder.BIG_ENDIAN, MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUST, BYTES_STRIP, FAIL_FAST);
    }


    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object obj = super.decode(ctx, in);
        if (obj == null) {
            return null;
        }

        TcpChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        ByteBuf byteBuf = null;
        try {
            byteBuf = (ByteBuf) obj;

            client.receiveComplete(ByteBufUtil.getBytes(byteBuf));

            MDC.put(Constant.LOG_TRANSACTION_ID, TransactionIdUtils.get(client.getShortChannelId()));
            log.info("接收>>:" + ByteBufUtil.hexDump(byteBuf));

            StandardProtocol4 protocol4 = new StandardProtocol4();
            protocol4.decode(byteBuf);

            return protocol4;
        } catch (Exception e) {
            client.close("decode处理异常:" + e.getMessage());
        } finally {
            release(byteBuf);
        }
        return null;
    }

    private void release(ByteBuf byteBuf) {
        ReferenceCountUtil.release(byteBuf);
    }

    /**
     * 判断接入层是否能够处理
     *
     * @param channelStatus 客户端状态
     * @param cmd           命令字
     * @return true:接入层处理 false:交由解析层处理
     * @throws Exception
     */
    private boolean canHandle(ChannelStatus channelStatus, int cmd) throws Exception {
        return false;
    }
}