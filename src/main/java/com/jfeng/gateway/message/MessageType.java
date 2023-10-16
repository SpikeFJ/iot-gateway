package com.jfeng.gateway.message;

import lombok.Getter;

/**
 * 事件消息
 */
@Getter
public enum MessageType {
    /**
     * 设备连接
     */
    EVENT_CONNECT(100),
    /**
     * 设备接收到数据
     */
    EVENT_RECEIVE(110),
    /**
     * 设备接收到数据
     */
    EVENT_RECEIVE_COMPLETE(120),
    /**
     * 设备发送了数据
     */
    EVENT_SEND(130),
    /**
     * 断开连接
     */
    EVENT_DISCONNECT(140),

    /**
     * 设备上线
     */
    EVENT_ONLINE(200),
    /**
     * 设备下线
     */
    EVENT_OFFLINE(210),


    DISPATCH_DATA(300),
    DISPATCH_CMD_RESP(310),

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
