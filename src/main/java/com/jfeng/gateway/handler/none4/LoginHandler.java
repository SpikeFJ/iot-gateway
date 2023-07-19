package com.jfeng.gateway.handler.none4;


import com.jfeng.gateway.channel.ChannelStatus;
import com.jfeng.gateway.channel.ClientChannel;
import com.jfeng.gateway.protocol.StandardProtocol4;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

import static com.jfeng.gateway.handler.none4.StandardExtend4Decoder.CLIENT_CHANNEL_ATTRIBUTE_KEY;

/**
 * 登录处理
 */
@Slf4j
public class LoginHandler extends MessageToMessageDecoder<StandardProtocol4> {

    @Override
    protected void decode(ChannelHandlerContext ctx, StandardProtocol4 protocol4, List<Object> list) throws Exception {
        if (protocol4 != null && protocol4.getCmd() == 0x01) {
            log.info("登陆处理开始");
            ClientChannel clientChannel = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
            if (clientChannel == null || clientChannel.getChannelStatus() == ChannelStatus.CLOSED) {
                clientChannel.close("客户端已关闭，来自登录处理。");
                return;
            }

            //1.重复登录
            String terminalNo = protocol4.getTerminalNo();
            clientChannel.checkDupdicate(terminalNo);
            //3.响应
            ctx.writeAndFlush(buildLoginResp(protocol4, true));
            //4.维护终端唯一标识
            clientChannel.setPacketId(terminalNo);
            clientChannel.setId(terminalNo);
            //5.更新在线终端列表
            clientChannel.login();
        } else {
            ctx.fireChannelRead(protocol4);
        }
    }

    /**
     * 组织校时响应帧
     *
     * @param req
     * @return
     * @throws Exception
     */
    public static StandardProtocol4 buildLoginResp(StandardProtocol4 req, boolean success) throws Exception {
        req.setCmd(0xB4);
        byte[] resp = new byte[1];
        resp[0] = (byte) (success ? 0 : 1);
        req.setBody(resp);
        return req;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ClientChannel clientChannel = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        if (clientChannel != null) {
            clientChannel.close("登录异常断开：" + cause.getMessage());
        }
    }
}
