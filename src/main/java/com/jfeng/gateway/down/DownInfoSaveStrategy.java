package com.jfeng.gateway.down;

import com.jfeng.gateway.session.TcpSession;

/**
 * 下发信息存储策略
 */
public interface DownInfoSaveStrategy {
    /**
     * 加载待发送
     *
     * @param tcpSession
     */
    void loadWaitToSend(TcpSession tcpSession);

    /**
     * 存储待发送
     *
     * @param req
     */
    void storeWaitToSend(CommandReq req);

    /**
     * 加载发送中
     *
     * @param tcpSession
     */
    void loadSending(TcpSession tcpSession);

    /**
     * 存储发送中
     *
     * @param req
     */
    void storeSending(CommandReq req);
}
