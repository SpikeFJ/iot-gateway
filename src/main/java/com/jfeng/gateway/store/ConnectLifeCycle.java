package com.jfeng.gateway.store;

import com.jfeng.gateway.util.FIFO;
import lombok.Getter;
import lombok.Setter;

/**
 * 完整单次连接生命周期
 */
@Setter
@Getter
public class ConnectLifeCycle {
    private String channelId;
    private String remoteAddress;

    private long createTime;
    private long closeTime;
    private String closeReason;
    private long totalTime;

    private String deviceId;
    private String businessId;

    /**
     * 发送接收明细
     */
    private FIFO<ConnectRecord> connectRecords;
}
