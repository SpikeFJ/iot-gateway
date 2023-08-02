package com.jfeng.gateway.comm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModbusResp {
    private int respCode;//0：正常
    private String msg;
    private String data;

    public static ModbusResp success(String data) {
        ModbusResp resp = new ModbusResp();
        resp.respCode = 0;
        resp.data = data;
        return resp;
    }

    public static ModbusResp fail(int exceptionCode, String msg) {
        ModbusResp resp = new ModbusResp();
        resp.respCode = exceptionCode;
        resp.msg = msg;
        return resp;
    }
}
