package com.jfeng.gateway.message;

import com.jfeng.gateway.util.DateTimeUtils2;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 分发数据
 */
@Slf4j
@Data
public class DispatchMessage {
    String protocol;
    String data;
    String time;
    String from;

    public DispatchMessage(String protocol, String data, String from) {
        this.protocol = protocol;
        this.data = data;
        this.time = DateTimeUtils2.outNow();
        this.from = from;
    }
}
