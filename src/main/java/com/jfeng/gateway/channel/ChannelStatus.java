package com.jfeng.gateway.channel;

/**
 * 通道状态
 */
public enum ChannelStatus {
    /**
     * 默认状态
     */
    INITIAL,
    /**
     * 已连接
     */
    CONNECTED,
    /**
     * 已登陆
     */
    LOGIN,
    /**
     * 已关闭
     */
    CLOSED
}
