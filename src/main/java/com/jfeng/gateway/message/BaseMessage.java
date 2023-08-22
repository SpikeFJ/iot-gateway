package com.jfeng.gateway.message;

public abstract class BaseMessage<T> {

    public BaseMessage(MessageType type) {
        this.type = type.getValue();
    }

    /**
     * 消息类型
     */
    int type;

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

}
