package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.session.OnlineStateChangeListener;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.RedisUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Redis上下线监听器
 */
@Component
public class RedisOnlineStateChangeListener implements OnlineStateChangeListener {
    @Resource
    private RedisUtils redisUtils;

    @Override
    public void online(TcpSession tcpSession) {

    }

    @Override
    public void Offline(TcpSession tcpSession, String message) {

    }
}
