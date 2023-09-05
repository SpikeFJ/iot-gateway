package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.SessionStatus;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.RedisUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 单个会话监听器
 */
@Component
@Setter
@Slf4j
public class SingleSessionRedisListener implements SessionListener {
    @Resource
    private RedisUtils redisUtils;

    @Override
    public void onConnect(TcpSession tcpSession) {
        saveSingleChannel(tcpSession);
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {
        saveSingleChannel(tcpSession);
    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        saveSingleChannel(tcpSession);
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {

    }

    @Override
    public void online(TcpSession tcpSession) {
        saveSingleChannel(tcpSession);
        tcpSession.getTcpServer().notify(tcpSession.getPacketId());
        tcpSession.getTcpServer().loadKafkaData(tcpSession);
    }


    @Override
    public void Offline(TcpSession tcpSession, String message) {

    }


    /**
     * 保存单个连接信息
     *
     * @param tcpSession
     */
    private void saveSingleChannel(TcpSession tcpSession) {
        if (tcpSession == null) {
            log.warn("连接已关闭");
            return;
        }
        if (tcpSession.getSessionStatus() == SessionStatus.CONNECTED) {
            String onlineKey = Constant.SYSTEM_PREFIX + tcpSession.getTcpServer().getLocalAddress() + ":connect:" + tcpSession.getRemoteAddress();

            Map<String, String> hashValue = new HashMap<>();
            hashValue.put(Constant.ONLINE_INFO_CREATE_TIME, DateTimeUtils2.outString(tcpSession.getCreateTime()));
            hashValue.put(Constant.ONLINE_INFO_LAST_REFRESH_TIME, DateTimeUtils2.outNow());

            redisUtils.putAll(onlineKey, hashValue);
        } else if (tcpSession.getSessionStatus() == SessionStatus.LOGIN) {
            String onlineKey = Constant.SYSTEM_PREFIX + tcpSession.getTcpServer().getLocalAddress() + ":online:" + tcpSession.getPacketId();
            String onlineLocationKey = Constant.ONLINE_MAPPING + tcpSession.getPacketId();

            Map<String, String> hashValue = new HashMap<>();
            hashValue.put(Constant.ONLINE_INFO_REMOTE_ADDRESS, tcpSession.getRemoteAddress());

            hashValue.put(Constant.ONLINE_INFO_PACKET_ID, tcpSession.getPacketId());
            hashValue.put(Constant.ONLINE_INFO_ID, tcpSession.getId());
            hashValue.put(Constant.ONLINE_INFO_CREATE_TIME, DateTimeUtils2.outString(tcpSession.getCreateTime()));

            hashValue.put(Constant.ONLINE_INFO_RECEIVE_PACKETS, String.valueOf(tcpSession.getReceivedPackets()));
            hashValue.put(Constant.ONLINE_INFO_RECEIVE_BYTES, String.valueOf(tcpSession.getReceivedBytes()));
            hashValue.put(Constant.ONLINE_INFO_LAST_RECEIVE_TIME, DateTimeUtils2.outString(tcpSession.getLastReadTime()));

            hashValue.put(Constant.ONLINE_INFO_SEND_PACKETS, String.valueOf(tcpSession.getSendPackets()));
            hashValue.put(Constant.ONLINE_INFO_SEND_BYTES, String.valueOf(tcpSession.getSendBytes()));
            hashValue.put(Constant.ONLINE_INFO_LAST_SEND_TIME, DateTimeUtils2.outString(tcpSession.getLastWriteTime()));

            hashValue.put(Constant.ONLINE_INFO_LAST_REFRESH_TIME, DateTimeUtils2.outNow());

            redisUtils.putAll(onlineKey, hashValue);
            redisUtils.expire(onlineKey, 5, TimeUnit.MINUTES);
            //3. 维护设备和所属机器的关系
            Map<String, String> mapping = new HashMap<>();
            mapping.put(Constant.MACHINE, tcpSession.getLocalAddress());
            mapping.put(Constant.LAST_REFRESH_TIME, DateTimeUtils2.outNow());
            redisUtils.putAll(onlineLocationKey, mapping);
        }
    }
}
