package com.jfeng.gateway.message;


import com.jfeng.gateway.util.DateTimeUtils2;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 事件消息(包括连接,登陆,发送,接收,断开,上线和下线)
 *
 * @param <T>
 */
@Slf4j
@Getter
@Setter
public class EventMessage<T> {
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

    public static EventMessage createConnect(String id, String from) {
        return create(MessageType.EVENT_CONNECT, id, from);
    }

    public static EventMessage createDisConnect(String id, String from) {
        return create(MessageType.EVENT_DISCONNECT, id, from);
    }

    public static EventMessage createReceiveComplete(String id, String data, String from) {
        return create(MessageType.EVENT_RECEIVE_COMPLETE, id, from, data);
    }

    public static EventMessage createSend(String id, String data, String from) {
        return create(MessageType.EVENT_SEND, id, from, data);
    }

    public static <T> EventMessage<T> createOnline(String id, String from) {
        return create(MessageType.EVENT_ONLINE, id, from);
    }

    public static <T> EventMessage<T> createOffline(String id, String from) {
        return create(MessageType.EVENT_OFFLINE, id, from);
    }

    private static EventMessage create(MessageType messageType, String id, String from) {
        return create(messageType, id, from, null);
    }

    private static <T> EventMessage<T> create(MessageType messageType, String id, String from, T data) {
        EventMessage<T> eventMessage = new EventMessage<>();
        eventMessage.type = messageType.getValue();
        eventMessage.id = id;
        eventMessage.from = from;
        eventMessage.time = DateTimeUtils2.outNow();
        if (data != null) {
            eventMessage.data = data;
        }
        return eventMessage;
    }
}
