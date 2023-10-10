package com.jfeng.gateway.session;

import com.jfeng.gateway.util.FIFO;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备历史数据
 */
public class DeviceHistoryData {
    private static Map<String,FIFO<LifeCycleRecord>> deviceHistory = new HashMap<>();
}
