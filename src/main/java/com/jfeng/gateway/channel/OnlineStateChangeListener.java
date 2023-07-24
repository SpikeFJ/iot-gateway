package com.jfeng.gateway.channel;

/**
 * 在线状态变更监听器
 */
public interface OnlineStateChangeListener {

    /**
     * 上线通知
     *
     * @param tcpChannel 客户端channel
     */
    void online(TcpChannel tcpChannel);

    /**
     * 下线通知
     *
     * @param tcpChannel 客户端channel
     * @param message       下线原因
     */
    void Offline(TcpChannel tcpChannel, String message);
}
