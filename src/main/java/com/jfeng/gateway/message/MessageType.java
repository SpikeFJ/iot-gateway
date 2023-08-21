package com.jfeng.gateway.message;

import lombok.Getter;

/**
 * 消息类型
 */
@Getter
public enum MessageType {
    /**
     * 设备连接
     */
    DEVICE_CONNECT(100),
    /**
     * 设备接收到数据
     */
    DEVICE_RECEIVE(110),
    /**
     * 设备接收到数据
     */
    DEVICE_RECEIVE_COMPLETE(120),
    /**
     * 设备发送了数据
     */
    DEVICE_SEND(130),
    /**
     * 设备发送了数据
     */
    DEVICE_DISCONNECT(140),

    /**
     * 设备上线
     */
    DEVICE_ONLINE(200),
    /**
     * 设备下线
     */
    DEVICE_OFFLINE(210),


    /**
     * 设备连接历史明细
     */
    DEVICE_CONNECT_HISTORY(300),
    /**
     * 服务器信息
     */
    SERVER_SUMMARY(400);

    private final int value;

    MessageType(int i) {
        this.value = i;
    }
}
