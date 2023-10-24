package com.jfeng.gateway.down;

import com.jfeng.gateway.server.TcpServer;
import com.jfeng.gateway.session.SessionStatus;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.JsonUtils;
import com.jfeng.gateway.util.RedisUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Getter
@Setter
@Slf4j
@ConditionalOnProperty(name = "spring.redis")
public class RedisDownInfoSaveStrategy implements DownInfoSaveStrategy {
    private static final String WAIT_TO_SEND = "IOT:WAIT_TO_SEND:";
    private static final String WAIT_TO_ACK = "IOT:WAIT_TO_ACK:";
    private static final String SENT = "IOT:SENT";
    private static final String WAIT_TO_SEND_KAFKA = "IOT:WAIT_TO_SEND_KAFKA";

    @Autowired
    RedisUtils redisUtils;
    private Lock lock = new ReentrantLock();

    @Override
    public void loadWaitToSend(TcpSession session) {
        String deviceId = session.getDeviceId();
        //1 将待确认队列中所有元素回传到待发送
        while (session.getSessionStatus() != SessionStatus.CLOSED && redisUtils.rightPopAndLeftPush(WAIT_TO_ACK + deviceId, WAIT_TO_SEND + deviceId) != null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //2 将待发送队列 加载到内存队列
        String toBeSend;
        while (session.getSessionStatus() != SessionStatus.CLOSED && (toBeSend = redisUtils.rightPopAndLeftPush(WAIT_TO_SEND + deviceId, WAIT_TO_ACK + deviceId)) != null) {
            CommandReq req = JsonUtils.deserialize(toBeSend, CommandReq.class);
            session.getTcpServer().fromHttp(req);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                log.warn("发送异常", e);
            }
        }
    }

    @Override
    public void storeWaitToSend(CommandReq req) {
        redisUtils.leftPush(WAIT_TO_SEND + req.deviceId, JsonUtils.serialize(req));
    }

    @Override
    public void loadSending(TcpSession tcpSession) {
        String deviceId = tcpSession.getDeviceId();
        TcpServer tcpServer = tcpSession.getTcpServer();

        // 将已发送队列 加载到内存队列
        String toBeSend;
        while (tcpServer.contains(deviceId) && tcpSession.getSessionStatus() != SessionStatus.CLOSED && (toBeSend = redisUtils.rightPopAndLeftPush(WAIT_TO_SEND + deviceId, WAIT_TO_ACK + deviceId)) != null) {

            if (tcpServer.getSynSent().containsKey(deviceId) == false) {
                tcpServer.getSynSent().put(deviceId, new HashMap<>());
            }
            CommandReq req = JsonUtils.deserialize(toBeSend, CommandReq.class);
            tcpServer.getSynSent().get(deviceId).put(req.getSendNo(), req);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                log.warn("发送异常", e);
            }
        }
    }

    @Override
    public void storeSending(CommandReq req) {
        req.setTryTimes(req.getTryTimes() + 1);
        //移除确认并保持至已发送
        redisUtils.rightPopAndLeftPush(WAIT_TO_ACK + req.deviceId, SENT + req.deviceId);
    }
}
