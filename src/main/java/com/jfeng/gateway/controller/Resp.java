package com.jfeng.gateway.controller;


/**
 * http响应内容
 *
 * @param <T>
 */

public class Resp<T> {
    //0：失败 1：成功
    public int code;
    //响应数据
    public T data;
    //响应描述
    public String message;

    private Resp() {
    }

    public static Resp success() {
        return success(null);
    }

    public static <T> Resp success(T data) {
        Resp resp = new Resp();
        resp.code = 1;
        resp.data = data;
        return resp;
    }

    public static Resp fail(String message) {
        return fail(0, message);
    }

    public static Resp fail(int code, String message) {
        Resp resp = new Resp();
        resp.code = code;
        resp.message = message;
        return resp;
    }
}
