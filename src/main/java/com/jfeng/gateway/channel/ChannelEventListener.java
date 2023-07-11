package com.jfeng.gateway.channel;

import io.netty.buffer.ByteBuf;

public interface ChannelEventListener extends OnlineStateChangeListener{

    /**
     * 连接事件
     *
     * @param clientChannel 通道连接
     */
    void onConnect(ClientChannel clientChannel);

    /**
     * 接收事件
     *
     * @param clientChannel 通道连接
     * @param data          接收数据
     */
    void onReceive(ClientChannel clientChannel, byte[] data);

    /**
     * 发送事件
     *
     * @param clientChannel 通道连接
     * @param data          发送数据
     */
    void onSend(ClientChannel clientChannel, byte[] data);

    /**
     * 断开事件
     *
     * @param clientChannel 通道连接
     * @param reason        断开原因
     */
    void onDisConnect(ClientChannel clientChannel, String reason);
}
