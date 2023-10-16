package com.jfeng.gateway.store;

/**
 * 存储连接历史（完整连接生命周期）
 */
public interface ConnectHistory {
    /**
     * 存储历史连接
     *
     * @param deviceId
     * @param connectLifeCycle
     */
    void save(String deviceId, ConnectLifeCycle connectLifeCycle);
}
