package com.jfeng.gateway.channel;

/**
 * 连接状态监听器
 */
public interface ChannelEventListener {

    /**
     * 连接事件
     *
     * @param tcpChannel 通道连接
     */
    void onConnect(TcpChannel tcpChannel);

    /**
     * 接收事件（未分包数据)
     *
     * @param tcpChannel 通道连接
     * @param data       接收数据
     */
    void onReceive(TcpChannel tcpChannel, byte[] data);

    /**
     * 接收事件
     *
     * @param tcpChannel 通道连接
     * @param data       接收数据
     */
    void onReceiveComplete(TcpChannel tcpChannel, byte[] data);

    /**
     * 发送事件
     *
     * @param tcpChannel 通道连接
     * @param data       发送数据
     */
    void onSend(TcpChannel tcpChannel, byte[] data);

    /**
     * 断开事件
     *
     * @param tcpChannel 通道连接
     * @param reason     断开原因
     */
    void onDisConnect(TcpChannel tcpChannel, String reason);
}
