package com.jfeng.gateway.session;

import java.util.EventListener;

/**
 * 会话监听器,用于监听实时通知类数据
 */
public interface SessionListener extends EventListener {

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

    //------------------------online、offline是业务事件----------

    /**
     * 上线通知
     *
     * @param tcpSession 客户端channel
     */
    void online(TcpSession tcpSession);

    /**
     * 下线通知
     *
     * @param tcpSession 客户端channel
     * @param message    下线原因
     */
    void Offline(TcpSession tcpSession, String message);
}
