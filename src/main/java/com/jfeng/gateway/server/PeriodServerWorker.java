package com.jfeng.gateway.server;

/**
 * 服务器周期性定时任务
 */
public interface PeriodServerWorker {
    /**
     * 处理Server信息
     *
     * @param tcpServer 服务端
     */
    void run(TcpServer tcpServer);
}
