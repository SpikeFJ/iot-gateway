package com.jfeng.gateway.down;

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
    String data; //发送数据：16进制字符串
    boolean sendOnOffline;//是否离线下发（即终端不在线时，等待终端上线后再次发送)
    int timeout; // 超时时长，单位秒
    int maxTryTimes;//最大重试次数
    int maxRespTimes;//最大响应次数

    long sendTime; //发送时间
    int tryTimes = 1;//当前重试次数，默认1
    int respTimes = 1;//当前需要响应次数

    public boolean checkTryTimes() {
        return tryTimes <= maxTryTimes;
    }
}
