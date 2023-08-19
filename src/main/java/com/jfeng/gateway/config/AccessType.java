package com.jfeng.gateway.config;

/**
 * 接入方式
 */
public enum AccessType {
    TCP(0), UDP(1), MQTT(2);

    private final int value;

    AccessType(int i) {
        this.value = i;
    }

    public int getValue() {
        return this.value;
    }
}
