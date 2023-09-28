package com.jfeng.gateway.protocol.none4;

import com.jfeng.gateway.session.SessionStatus;
import com.jfeng.gateway.session.TcpSession;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

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
    public static final AttributeKey<TcpSession> SESSION_KEY = AttributeKey.valueOf("sessionKey");

    public StandardExtend4Decoder() {
        super(ByteOrder.BIG_ENDIAN, MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUST, BYTES_STRIP, FAIL_FAST);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object obj = super.decode(ctx, in);
        if (obj == null) {
            return null;
        }

        TcpSession session = ctx.channel().attr(SESSION_KEY).get();
        ByteBuf byteBuf = null;
        try {
            byteBuf = (ByteBuf) obj;

            session.receiveComplete(ByteBufUtil.getBytes(byteBuf));

//             MDC.put(Constant.LOG_TRANSACTION_ID, TransactionIdUtils.get(session.getShortChannelId()));
//            log.info("接收>>:" + ByteBufUtil.hexDump(byteBuf));

            StandardProtocol4 protocol4 = new StandardProtocol4();
            protocol4.decode(byteBuf);

            if (canHandle(session.getSessionStatus(), protocol4.getCmd())) {
                return protocol4;
            }

            session.getTcpServer().dispatch(session.getDeviceId(), ByteBufUtil.hexDump(byteBuf));

        } catch (Exception e) {
            log.warn("decode", e);
            session.close("decode处理异常:" + e.getMessage());
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
     * @param sessionStatus 客户端状态
     * @param cmd           命令字
     * @return true:接入层处理 false:交由解析层处理
     * @throws Exception
     */
    private boolean canHandle(SessionStatus sessionStatus, int cmd) throws Exception {
        return true;
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
