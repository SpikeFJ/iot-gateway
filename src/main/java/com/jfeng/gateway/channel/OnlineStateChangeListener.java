package com.jfeng.gateway.channel;

/**
 * 在线状态变更监听器
 */
public interface OnlineStateChangeListener {

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
     * @param message       下线原因
     */
    void Offline(TcpSession tcpSession, String message);
}
