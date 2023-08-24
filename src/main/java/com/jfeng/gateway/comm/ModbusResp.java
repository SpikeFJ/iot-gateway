package com.jfeng.gateway.comm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModbusResp<T> {
    private int respCode;//0：正常
    private String msg;
    private T data;

    public static <T> ModbusResp<T> success(T data) {
        ModbusResp resp = new ModbusResp();
        resp.respCode = 0;
        resp.data = data;
        return resp;
    }

    public static <T> ModbusResp<T> fail(int exceptionCode, String msg, T data) {
        ModbusResp resp = new ModbusResp();
        resp.respCode = exceptionCode;
        resp.msg = msg;
        resp.data = data;
        return resp;
    }

    public static <T> ModbusResp<T> timeout(int exceptionCode, String msg, T data) {
        ModbusResp resp = new ModbusResp();
        resp.respCode = exceptionCode;
        resp.msg = msg;
        resp.data = data;
        return resp;
    }
}
