package com.jfeng.gateway.session;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 代理监听器
 */
@Slf4j
@Setter
@Getter
public abstract class ProxySessionListener implements SessionListener {
    private List<SessionListener> childListeners = new ArrayList<>();

    @Override
    public void onConnect(TcpSession tcpSession) {
        childListeners.parallelStream().forEach(x -> x.onConnect(tcpSession));
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {
        childListeners.parallelStream().forEach(x -> x.onReceive(tcpSession, data));
    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {
        childListeners.parallelStream().forEach(x -> x.onReceiveComplete(tcpSession, data));
    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        childListeners.parallelStream().forEach(x -> x.onSend(tcpSession, data));
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {
        childListeners.parallelStream().forEach(x -> x.onDisConnect(tcpSession, reason));
    }

    @Override
    public void online(TcpSession tcpSession) {
        childListeners.parallelStream().forEach(x -> x.online(tcpSession));
    }

    @Override
    public void offline(TcpSession tcpSession, String message) {
        childListeners.parallelStream().forEach(x -> x.offline(tcpSession, message));
    }
}
