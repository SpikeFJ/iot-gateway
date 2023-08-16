package com.jfeng.gateway.session;

/**
 * 会话状态
 */
public enum SessionStatus {
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
