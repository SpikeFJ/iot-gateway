package com.jfeng.gateway.store;

import com.jfeng.gateway.util.FIFO;
import lombok.Getter;
import lombok.Setter;

/**
 * 会话对象，记录完整单次连接生命周期
 */
@Setter
@Getter
public class SessionLifeCycle {
    private String channelId;
    private String remoteAddress;

    private long createTime;
    private long closeTime;
    private String closeReason;
    private long totalTime;

    private String deviceId;
    private String businessId;

    /**
     * 会话记录明细
     */
    private FIFO<SessionRecord> sessionRecords;
}
