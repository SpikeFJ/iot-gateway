package com.jfeng.gateway.message;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 下行命令
 */
@Slf4j
@Data
public class CommandReq {
    String deviceId;//设备
    String sendNo;//发送序号、标识唯一请求
    String data;
    String sendTime;
    boolean sendOnOffline;//是否离线下发（即终端不在线时，等待终端上线后再次发送)
    int timeout; // 单位秒
    int tryTimes;//重试次数
}
