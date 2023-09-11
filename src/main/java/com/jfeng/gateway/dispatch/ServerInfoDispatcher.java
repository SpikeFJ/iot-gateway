package com.jfeng.gateway.dispatch;

import com.jfeng.gateway.server.TcpServer;

/**
 * 服务自身信息分发器。
 */
public interface ServerInfoDispatcher {
    /**
     * 分发服务端信息
     *
     * @param tcpServer 服务端
     */
    void dispatch(TcpServer tcpServer);
}
