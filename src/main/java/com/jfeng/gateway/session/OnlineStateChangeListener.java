package com.jfeng.gateway.session;

import java.util.EventListener;

/**
 * 在线状态变更监听器
 */
public interface OnlineStateChangeListener extends EventListener {

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
