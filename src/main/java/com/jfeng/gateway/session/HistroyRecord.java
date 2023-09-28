package com.jfeng.gateway.session;

import lombok.Getter;
import lombok.Setter;

/**
 * 历史发送记录
 */
@Setter
@Getter
public class HistroyRecord {
    /**
     * 数据类型。0：建立连接 1:接收 2：发送
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