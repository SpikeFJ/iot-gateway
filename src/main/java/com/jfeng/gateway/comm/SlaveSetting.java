package com.jfeng.gateway.comm;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 从站配置
 */
@Getter
@Setter
public class SlaveSetting {
    /**
     * 从站名称
     */
    private String name;

    /**
     * 从站地址
     */
    private int address;

    /**
     * 寄存器配置
     */
    private List<RegisterSetting> registerSettings;
}
