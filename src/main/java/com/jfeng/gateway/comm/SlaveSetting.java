package com.jfeng.gateway.comm;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
    private List<RegisterSetting> registerSettings = new ArrayList<>();


    public List<Modbus> getModbusList() {
        List<Modbus> modbusList = new ArrayList<>();

        for (RegisterSetting registerSetting : registerSettings) {
            Modbus modbus = new Modbus();
            modbus.setAddress(address);
            modbus.setFunctionCode(0x03);
            modbus.setRegisterAddress(registerSetting.getAddress());
            modbus.setLength(registerSetting.getLength());
            modbus.setExpression(registerSetting.getExpression());
            modbus.setDataType(registerSetting.getDataType());
            modbus.setDecimalLength(registerSetting.getDecimalLength());
            modbus.setCode(registerSetting.getCode());
            modbus.setUnit(registerSetting.getUnit());
            modbusList.add(modbus);
        }
        return modbusList;
    }
}
