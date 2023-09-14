package com.jfeng.gateway.up.dispatch;

import com.jfeng.gateway.comm.Constant;
import com.jfeng.gateway.server.TcpServer;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.RedisUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.redis")
@Primary
public class RedisServerInfoDispatcher implements ServerInfoDispatcher {
    @Resource
    private RedisUtils redisUtils;

    @Override
    public void dispatch(TcpServer tcpserver) {
        Map<String, String> serverInfos = new HashMap<>();
        serverInfos.put(Constant.ONLINE, String.valueOf(tcpserver.getOnLines().size()));
        serverInfos.put(Constant.CONNECTED, String.valueOf(tcpserver.getConnected().size()));

        serverInfos.put(Constant.SERVER_CONNECT_NUM, String.valueOf(tcpserver.getTotalConnectNum()));
        serverInfos.put(Constant.SERVER_CLOSE_NUM, String.valueOf(tcpserver.getTotalCloseNum()));
        serverInfos.put(Constant.SERVER_SEND_PACKETS, String.valueOf(tcpserver.getTotalSendPackets()));
        serverInfos.put(Constant.SERVER_SEND_BYTES, String.valueOf(tcpserver.getTotalSendBytes()));
        serverInfos.put(Constant.SERVER_RECEIVE_PACKETS, String.valueOf(tcpserver.getTotalReceivePackets()));
        serverInfos.put(Constant.SERVER_RECEIVE_BYTES, String.valueOf(tcpserver.getTotalReceiveBytes()));

        serverInfos.put(Constant.SERVER_WAIT_TO_SEND, String.valueOf(tcpserver.getSyncWaitToSend().size()));
        serverInfos.put(Constant.SERVER_SENDING, String.valueOf(tcpserver.getSynSent().size()));
        serverInfos.put(Constant.SERVER_LAST_REFRESH_TIME, DateTimeUtils2.outNow());

        redisUtils.putAll(Constant.SYSTEM_PREFIX + tcpserver.getLocalAddress() + ":summary", serverInfos);
    }
}
