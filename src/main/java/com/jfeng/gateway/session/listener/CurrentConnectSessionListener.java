package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.store.ConnectHistory;
import com.jfeng.gateway.store.ConnectRecord;
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
 * 当前连接信息
 */
@Component
@Setter
@Slf4j
@ConditionalOnProperty(name = "gateway.maxCollectRecord")
public class CurrentConnectSessionListener implements SessionListener {

    @Value("${maxCollectRecord:1000}")
    private int maxHistory;

    @Resource
    private ConnectHistory history;

    @Override
    public void onConnect(TcpSession tcpSession) {
        tcpSession.setHistroyRecordFIFO(new FIFO<>(maxHistory));

        ConnectRecord connectRecord = new ConnectRecord();
        connectRecord.setDataType(0);
        connectRecord.setTime(DateTimeUtils2.outNow());
        connectRecord.setData("");
        tcpSession.getHistroyRecordFIFO().add(connectRecord);
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {
        ConnectRecord connectRecord = new ConnectRecord();
        connectRecord.setDataType(1);
        connectRecord.setTime(DateTimeUtils2.outNow());
        connectRecord.setData(ByteBufUtil.hexDump(data));
        tcpSession.getHistroyRecordFIFO().add(connectRecord);
    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        ConnectRecord connectRecord = new ConnectRecord();
        connectRecord.setDataType(2);
        connectRecord.setTime(DateTimeUtils2.outNow());
        connectRecord.setData(ByteBufUtil.hexDump(data));
        tcpSession.getHistroyRecordFIFO().add(connectRecord);
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {
        ConnectRecord connectRecord = new ConnectRecord();
        connectRecord.setDataType(4);
        connectRecord.setTime(DateTimeUtils2.outNow());
        connectRecord.setData(reason);
        tcpSession.getHistroyRecordFIFO().add(connectRecord);

        history.save(tcpSession.getDeviceId(), tcpSession.createConnectLifeCycle());
    }

    @Override
    public void online(TcpSession tcpSession) {

    }

    @Override
    public void offline(TcpSession tcpSession, String message) {

    }
}
