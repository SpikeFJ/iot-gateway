package com.jfeng.gateway.store;

/**
 * 会话历史（完整连接生命周期）
 */
public interface SessionHistory {
    /**
     * 存储历史连接
     *
     * @param deviceId
     * @param sessionLifeCycle
     */
    void save(String deviceId, SessionLifeCycle sessionLifeCycle);
}
