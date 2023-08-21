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
 * 刷新单个设备流量监听器。
 */
@Component
@Setter
@Slf4j
public class RedisRefreshSingleDeviceListener implements SessionListener {
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

    }

    @Override
    public void Offline(TcpSession tcpSession, String message) {

    }


    /**
     * 保存单个连接信息
     *
     * @param channel
     */
    private void saveSingleChannel(TcpSession channel) {
        if (channel == null) {
            log.warn("连接已关闭");
            channel.close("检测到连接关闭");
            return;
        }
        if (channel.getSessionStatus() == SessionStatus.CONNECTED) {
            String onlineKey = "iot:machine:" + channel.getLocalAddress() + ":connect:" + channel.getRemoteAddress();

            Map<String, String> hashValue = new HashMap<>();
            hashValue.put(Constant.ONLINE_INFO_CREATE_TIME, DateTimeUtils2.outString(channel.getCreateTime()));
            hashValue.put(Constant.ONLINE_INFO_LAST_REFRESH_TIME, DateTimeUtils2.outNow());

            redisUtils.putAll(onlineKey, hashValue);
        } else if (channel.getSessionStatus() == SessionStatus.LOGIN) {
            String onlineKey = "iot:machine:" + channel.getLocalAddress() + ":online:" + channel.getPacketId();
            String onlineLocationKey = Constant.ONLINE_MAPPING + channel.getPacketId();

            Map<String, String> hashValue = new HashMap<>();
            hashValue.put(Constant.ONLINE_INFO_REMOTE_ADDRESS, channel.getRemoteAddress());

            hashValue.put(Constant.ONLINE_INFO_PACKET_ID, channel.getPacketId());
            hashValue.put(Constant.ONLINE_INFO_ID, channel.getId());
            hashValue.put(Constant.ONLINE_INFO_CREATE_TIME, DateTimeUtils2.outString(channel.getCreateTime()));

            hashValue.put(Constant.ONLINE_INFO_RECEIVE_PACKETS, String.valueOf(channel.getReceivedPackets()));
            hashValue.put(Constant.ONLINE_INFO_RECEIVE_BYTES, String.valueOf(channel.getReceivedBytes()));
            hashValue.put(Constant.ONLINE_INFO_LAST_RECEIVE_TIME, DateTimeUtils2.outString(channel.getLastReadTime()));

            hashValue.put(Constant.ONLINE_INFO_SEND_PACKETS, String.valueOf(channel.getSendPackets()));
            hashValue.put(Constant.ONLINE_INFO_SEND_BYTES, String.valueOf(channel.getSendBytes()));
            hashValue.put(Constant.ONLINE_INFO_LAST_SEND_TIME, DateTimeUtils2.outString(channel.getLastWriteTime()));

            hashValue.put(Constant.ONLINE_INFO_LAST_REFRESH_TIME, DateTimeUtils2.outNow());

            redisUtils.putAll(onlineKey, hashValue);
            redisUtils.expire(onlineKey, 5, TimeUnit.MINUTES);
            //3. 维护设备和所属机器的关系
            Map<String, String> mapping = new HashMap<>();
            mapping.put(Constant.MACHINE, channel.getLocalAddress());
            mapping.put(Constant.LAST_REFRESH_TIME, DateTimeUtils2.outNow());
            redisUtils.putAll(onlineLocationKey, mapping);
        }
    }
}
