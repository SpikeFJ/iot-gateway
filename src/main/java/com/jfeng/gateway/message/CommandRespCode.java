package com.jfeng.gateway.message;

import lombok.Getter;

/**
 * 下行命令响应code
 */
@Getter
public enum CommandRespCode {

    /**
     * 成功
     */
    SUCCESS(0),
    /**
     * 成功发送
     */
    SUCCESS_SEND(1),

    /**
     * 不在线
     */
    OFFLINE(4),

    /**
     * 超时
     */
    TIMEOUT(3),

    /**
     * 失败
     */
    FAIL(5);

    private final int value;

    CommandRespCode(int i) {
        this.value = i;
    }
}
