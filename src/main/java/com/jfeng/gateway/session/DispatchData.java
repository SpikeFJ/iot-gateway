package com.jfeng.gateway.session;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 解析后的分发数据
 */
@Slf4j
@Data
public class DispatchData {
    String protocol;
    String data;

    public DispatchData(String protocol, String data) {
        this.protocol = protocol;
        this.data =data;
    }
}
