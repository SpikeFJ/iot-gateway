package com.jfeng.gateway.protocol;

import io.netty.buffer.ByteBuf;

/**
 * 通讯协议
 */
public interface Protocol {
    /**
     * 协议名
     */
    String name();

    /**
     * 解帧
     *
     * @param in
     */
    void decode(ByteBuf in) throws Exception;

    /**
     * 组帧
     *
     * @return
     */
    void encode(ByteBuf out) throws Exception;
}
