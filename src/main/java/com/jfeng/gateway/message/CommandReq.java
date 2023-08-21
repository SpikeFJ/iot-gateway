package com.jfeng.gateway.message;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 下行命令
 */
@Slf4j
@Data
public class CommandReq {
    String id;//设备
    String key;//标识唯一请求
    String data;
    String sendTime;
    int timeout; // 单位秒
    int tryTimes;//重试次数
}
