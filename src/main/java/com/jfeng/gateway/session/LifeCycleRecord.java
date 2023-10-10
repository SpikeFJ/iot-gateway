package com.jfeng.gateway.session;

import com.jfeng.gateway.util.FIFO;

/**
 * 单个连接生命周期
 */
public class LifeCycleRecord {
    private String channelId;
    private String remoteAddress;

    private String createTime;
    private String closeTime;
    private String closeReason;
    private String totalTime;
    /**
     * 发送接收明细
     */
    private FIFO<ConnectDataRecord> historyRecords;
}
