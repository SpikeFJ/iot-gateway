package com.jfeng.gateway.comm;

import lombok.Getter;
import lombok.Setter;

/**
 * 寄存器配置
 */
@Getter
@Setter
public class RegisterSetting {
    /**
     * 寄存器地址
     */
    private int address;
    /**
     * 长度
     */
    private int length;
    /**
     * 编码
     */
    private String code;
    /**
     * 单位
     */
    private String unit;

    /**
     * 换算
     */
    private String expression;
}
