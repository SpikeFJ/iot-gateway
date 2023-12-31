package com.jfeng.gateway.protocol.none4;


import com.jfeng.gateway.session.SessionStatus;
import com.jfeng.gateway.session.TcpSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

import static com.jfeng.gateway.protocol.none4.StandardExtend4Decoder.SESSION_KEY;

/**
 * 登录处理
 */
@Slf4j
public class LoginHandler extends MessageToMessageDecoder<StandardProtocol4> {
    @Override
    protected void decode(ChannelHandlerContext ctx, StandardProtocol4 protocol4, List<Object> list) throws Exception {
        if (protocol4 != null && protocol4.getCmd() == 0x01) {
            TcpSession tcpSession = ctx.channel().attr(SESSION_KEY).get();
            if (tcpSession == null || tcpSession.getSessionStatus() == SessionStatus.CLOSED) {
                tcpSession.close("客户端已关闭，来自登录处理。");
                return;
            }
            //1.重复登录
            String terminalNo = protocol4.getTerminalNo();
//            tcpSession.checkDuplicate(terminalNo);
            //3.响应
            //tcpSession.send(buildLoginResp(protocol4, true).encode(););
            ctx.writeAndFlush(buildLoginResp(protocol4, true));

            //4.维护终端唯一标识
            tcpSession.setDeviceId(terminalNo);
            tcpSession.setBId(terminalNo);
            //5.更新在线终端列表
            tcpSession.login();
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
        TcpSession tcpSession = ctx.channel().attr(SESSION_KEY).get();
        if (tcpSession != null) {
            tcpSession.close("异常断开：" + cause.getMessage());
        }
    }
}
