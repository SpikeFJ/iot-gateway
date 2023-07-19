package com.jfeng.gateway.message;

import lombok.Getter;

/**
 * 消息类型
 */
@Getter
public enum MessageType {
    /**
     * 设备上线
     */
    DEVICE_LOGIN(100),
    /**
     * 设备下线
     */
    DEVICE_LOGOUT(110),
    /**
     * 设备接收到数据
     */
    DEVICE_RECEIVE(120),
    /**
     * 设备发送了数据
     */
    DEVICE_SEND(130),

    /**
     * 设备连接历史明细
     */
    DEVICE_CONNECT_HISTORY(200),

    /**
     * 服务器信息
     */
    SERVER_SUMMARY(300);

    private final int value;

    MessageType(int i) {
        this.value = i;
    }
}
