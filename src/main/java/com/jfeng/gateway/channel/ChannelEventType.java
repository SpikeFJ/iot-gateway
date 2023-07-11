package com.jfeng.gateway.channel;

/**
 * 通道事件类型
 */
public enum ChannelEventType {
    /**
     * 建立连接
     */
    CONNECT,
    /**
     * 登陆
     */
    LOGIN,
    /**
     * 登出
     */
    LOGOUT,
    /**
     * 接收数据
     */
    RECEIVE,
    /**
     * 发送数据
     */
    SEND,
    /**
     * 所有信息
     */
    ALL
}
