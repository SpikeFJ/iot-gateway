package com.jfeng.gateway.store;

import lombok.Getter;
import lombok.Setter;

/**
 * 会话记录
 */
@Setter
@Getter
public class SessionRecord {
    /**
     * 数据类型。0：建立连接 1:接收 2：发送 3:断开
     */
    int dataType;
    /**
     * 发生时间
     */
    String time;
    /**
     * 数据
     */
    String data;
}
