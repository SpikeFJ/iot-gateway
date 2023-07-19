package com.jfeng.gateway.protocol;

public class ValidException extends Exception {
    private int expect;
    private int real;

    public ValidException(int expect, int real) {
        super();
        this.expect = expect;
        this.real = real;
    }

    @Override
    public String getMessage() {
        return "校验码校验失败,期望:" + this.expect + "，实际：" + real;
    }
}
