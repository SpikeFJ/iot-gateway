package com.jfeng.gateway.protocol;

import com.jfeng.gateway.protocol.none4.StandardExtend4Decoder;
import com.jfeng.gateway.session.TcpSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Ip筛选
 */
@Slf4j
public class IpFilterHandler extends ChannelInboundHandlerAdapter {

    public IpFilterHandler(List<String> blackIpList, List<String> whiteIpList) {
        this.blackIpList = blackIpList;
        this.whiteIpList = whiteIpList;
    }

    private List<String> blackIpList;
    private List<String> whiteIpList;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (blackIpList == null || blackIpList.size() == 0) {
            ctx.fireChannelActive();
            return;
        }

        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = inetSocketAddress.getAddress().getHostAddress();

        if (blackIpList != null && blackIpList.contains(ip)) {
            log.warn("非法连接。ip：" + ip + ",端口：" + inetSocketAddress.getPort());
            ctx.channel().close();
        } else {
            ctx.fireChannelActive();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        TcpSession client = ctx.channel().attr(StandardExtend4Decoder.SESSION_KEY).get();
        if (client != null) {
            client.close("异常断开：" + cause.getMessage());
        }
    }
}
