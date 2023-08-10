package com.jfeng.gateway.channel;

/**
 * 会话监听器
 */
public interface SessionListener {

    /**
     * 连接事件
     *
     * @param tcpSession 通道连接
     */
    void onConnect(TcpSession tcpSession);

    /**
     * 接收事件（未分包数据)
     *
     * @param tcpSession 通道连接
     * @param data       接收数据
     */
    void onReceive(TcpSession tcpSession, byte[] data);

    /**
     * 接收事件
     *
     * @param tcpSession 通道连接
     * @param data       接收数据
     */
    void onReceiveComplete(TcpSession tcpSession, byte[] data);

    /**
     * 发送事件
     *
     * @param tcpSession 通道连接
     * @param data       发送数据
     */
    void onSend(TcpSession tcpSession, byte[] data);

    /**
     * 断开事件
     *
     * @param tcpSession 通道连接
     * @param reason     断开原因
     */
    void onDisConnect(TcpSession tcpSession, String reason);
}
