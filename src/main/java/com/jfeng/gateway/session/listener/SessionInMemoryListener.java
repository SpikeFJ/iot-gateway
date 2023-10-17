package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.store.SessionHistory;
import com.jfeng.gateway.store.SessionRecord;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.FIFO;
import io.netty.buffer.ByteBufUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 会话内存监听器
 * <p>
 * 维护会话历史及明细
 */
@Component
@Setter
@Slf4j
@ConditionalOnProperty(name = "gateway.store.type", havingValue = "memory")
public class SessionInMemoryListener implements SessionListener {

    @Value("${gateway.store.maxRecordsForSingleSession:1000}")
    private int maxHistory;

    @Resource
    private SessionHistory history;

    @Override
    public void onConnect(TcpSession tcpSession) {
        tcpSession.setHistroyRecordFIFO(new FIFO<>(maxHistory));

        SessionRecord sessionRecord = new SessionRecord();
        sessionRecord.setDataType(0);
        sessionRecord.setTime(DateTimeUtils2.outNow());
        sessionRecord.setData("");
        tcpSession.getHistroyRecordFIFO().add(sessionRecord);
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {
        SessionRecord sessionRecord = new SessionRecord();
        sessionRecord.setDataType(1);
        sessionRecord.setTime(DateTimeUtils2.outNow());
        sessionRecord.setData(ByteBufUtil.hexDump(data));
        tcpSession.getHistroyRecordFIFO().add(sessionRecord);
    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        SessionRecord sessionRecord = new SessionRecord();
        sessionRecord.setDataType(2);
        sessionRecord.setTime(DateTimeUtils2.outNow());
        sessionRecord.setData(ByteBufUtil.hexDump(data));
        tcpSession.getHistroyRecordFIFO().add(sessionRecord);
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {
        SessionRecord sessionRecord = new SessionRecord();
        sessionRecord.setDataType(3);
        sessionRecord.setTime(DateTimeUtils2.outNow());
        sessionRecord.setData(reason);
        tcpSession.getHistroyRecordFIFO().add(sessionRecord);

        history.save(tcpSession.getDeviceId(), tcpSession.createConnectLifeCycle());
    }

    @Override
    public void online(TcpSession tcpSession) {

    }

    @Override
    public void offline(TcpSession tcpSession, String message) {

    }
}
