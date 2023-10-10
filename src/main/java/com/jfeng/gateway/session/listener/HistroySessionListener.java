package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.session.ConnectDataRecord;
import com.jfeng.gateway.session.SessionListener;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.FIFO;
import io.netty.buffer.ByteBufUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Setter
@Slf4j
@ConditionalOnProperty(name = "gateway.maxHistory")
public class HistroySessionListener implements SessionListener {

    @Value("${gateway.maxHistory:1000}")
    private int maxHistory;

    @Override
    public void onConnect(TcpSession tcpSession) {
        tcpSession.setHistroyRecordFIFO(new FIFO<>(maxHistory));

        ConnectDataRecord connectDataRecord = new ConnectDataRecord();
        connectDataRecord.setDataType(0);
        connectDataRecord.setTime(DateTimeUtils2.outNow());
        connectDataRecord.setData("");
        tcpSession.getHistroyRecordFIFO().add(connectDataRecord);
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {
        ConnectDataRecord connectDataRecord = new ConnectDataRecord();
        connectDataRecord.setDataType(1);
        connectDataRecord.setTime(DateTimeUtils2.outNow());
        connectDataRecord.setData(ByteBufUtil.hexDump(data));
        tcpSession.getHistroyRecordFIFO().add(connectDataRecord);
    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        ConnectDataRecord connectDataRecord = new ConnectDataRecord();
        connectDataRecord.setDataType(2);
        connectDataRecord.setTime(DateTimeUtils2.outNow());
        connectDataRecord.setData(ByteBufUtil.hexDump(data));
        tcpSession.getHistroyRecordFIFO().add(connectDataRecord);
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {
        tcpSession.getHistroyRecordFIFO().clear();
    }

    @Override
    public void online(TcpSession tcpSession) {

    }

    @Override
    public void offline(TcpSession tcpSession, String message) {

    }
}
