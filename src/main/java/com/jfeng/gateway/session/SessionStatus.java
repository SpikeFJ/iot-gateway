package com.jfeng.gateway.session;

/**
 * 会话状态
 */
public enum SessionStatus {
    /**
     * 已连接
     */
    CONNECTED("已连接"),
    /**
     * 已登陆
     */
    LOGIN("已登陆"),
    /**
     * 已关闭
     */
    CLOSED("已关闭");

    private final String name;

    SessionStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
