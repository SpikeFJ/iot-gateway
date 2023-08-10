package com.jfeng.gateway.handler.none4;

import com.jfeng.gateway.channel.TcpSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

import static com.jfeng.gateway.handler.none4.StandardExtend4Decoder.CLIENT_CHANNEL_ATTRIBUTE_KEY;

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
        TcpSession client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        if (client != null) {
            client.close("异常断开：" + cause.getMessage());
        }
    }
}
