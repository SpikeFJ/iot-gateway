package com.jfeng.gateway.message;


import com.jfeng.gateway.util.DateTimeUtils2;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class Message<T> {
    /**
     * 消息类型
     */
    int messageType;
    /**
     * 传递数据
     */
    T data;

    /**
     * 传递时间
     */
    String time;


    public static <R> Message<R> login(R data) {
        Message<R> message = new Message<>();
        message.messageType = MessageType.DEVICE_LOGIN.getValue();
        message.time = DateTimeUtils2.outNow();
        message.data = data;
        return message;
    }
}
