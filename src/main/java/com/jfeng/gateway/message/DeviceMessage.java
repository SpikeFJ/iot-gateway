package com.jfeng.gateway.message;


import com.jfeng.gateway.util.DateTimeUtils2;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备事件
 *
 * @param <T>
 */
@Slf4j
@Getter
@Setter
public class DeviceMessage<T> {
    /**
     * 消息类型
     */
    int type;
    /**
     * 设备id
     */
    String id;
    /**
     * 传递数据
     */
    T data;
    /**
     * 创建时间
     */
    String time;
    /**
     * 数据来源
     */
    String from;

    public static DeviceMessage createConnect(String id, String from) {
        DeviceMessage deviceMessage = new DeviceMessage<>();
        deviceMessage.type = MessageType.DEVICE_CONNECT.getValue();
        deviceMessage.id = id;
        deviceMessage.time = DateTimeUtils2.outNow();
        deviceMessage.from = from;
        return deviceMessage;
    }

    public static DeviceMessage createDisConnect(String id, String from) {
        DeviceMessage deviceMessage = new DeviceMessage<>();
        deviceMessage.type = MessageType.DEVICE_DISCONNECT.getValue();
        deviceMessage.id = id;
        deviceMessage.time = DateTimeUtils2.outNow();
        deviceMessage.from = from;
        return deviceMessage;
    }

    public static DeviceMessage createReceiveComplete(String id, String data, String from) {
        DeviceMessage<String> deviceMessage = new DeviceMessage<>();
        deviceMessage.type = MessageType.DEVICE_RECEIVE_COMPLETE.getValue();
        deviceMessage.id = id;
        deviceMessage.time = DateTimeUtils2.outNow();
        deviceMessage.from = from;
        deviceMessage.data = data;
        return deviceMessage;
    }

    public static DeviceMessage createSend(String id, String data, String from) {
        DeviceMessage<String> deviceMessage = new DeviceMessage<>();
        deviceMessage.type = MessageType.DEVICE_SEND.getValue();
        deviceMessage.id = id;
        deviceMessage.time = DateTimeUtils2.outNow();
        deviceMessage.from = from;
        deviceMessage.data = data;
        return deviceMessage;
    }

    public static <T> DeviceMessage<T> createOnline(String id,String from) {
        DeviceMessage<T> deviceMessage = new DeviceMessage<>();
        deviceMessage.type = MessageType.DEVICE_ONLINE.getValue();
        deviceMessage.id = id;
        deviceMessage.time = DateTimeUtils2.outNow();
        deviceMessage.from = from;
        return deviceMessage;
    }

    public static <T> DeviceMessage<T> createOffline(String id,  String from) {
        DeviceMessage<T> deviceMessage = new DeviceMessage<>();
        deviceMessage.type = MessageType.DEVICE_OFFLINE.getValue();
        deviceMessage.id = id;
        deviceMessage.time = DateTimeUtils2.outNow();
        deviceMessage.from = from;
        return deviceMessage;
    }
}
