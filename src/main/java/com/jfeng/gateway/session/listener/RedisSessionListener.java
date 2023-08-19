package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.RedisUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Redis会话监听器，接收到事件后通过Redis的PubSub机制通知订阅者
 */
@Component
public class RedisSessionListener implements SessionListener {
    @Resource
    private RedisUtils redisUtils;

    @Override
    public void onConnect(TcpSession tcpSession) {

    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {

    }
}
