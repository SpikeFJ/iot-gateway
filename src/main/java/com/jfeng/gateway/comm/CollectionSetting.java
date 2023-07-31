package com.jfeng.gateway.comm;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 采集配置
 */
@Getter
@Setter
public class CollectionSetting {
    /**
     * DTU设备编号
     */
    private String dtuCode;
    /**
     * 心跳编码
     */
    private String heartCode;
    /**
     * 采集间隔，单位：毫秒
     */
    private int connectPeriod;

    /**
     * 从站配置
     */
    private List<SlaveSetting> slaveSettings = new ArrayList<>();


    public void encode(ByteBuf out) {

    }
}
