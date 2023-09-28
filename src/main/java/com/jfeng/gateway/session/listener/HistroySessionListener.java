package com.jfeng.gateway.session.listener;

import com.jfeng.gateway.session.HistroyRecord;
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
@ConditionalOnProperty(name = "gateway.maxHistroy")
public class HistroySessionListener implements SessionListener {

    @Value("${gateway.maxHistroy}")
    private int maxHistroy;

    @Override
    public void onConnect(TcpSession tcpSession) {
        HistroyRecord histroyRecord = new HistroyRecord();
        histroyRecord.setDataType(0);
        histroyRecord.setTime(DateTimeUtils2.outNow());
        histroyRecord.setData("");
        tcpSession.getHistroyRecordFIFO().add(histroyRecord);
    }

    @Override
    public void onReceive(TcpSession tcpSession, byte[] data) {
        HistroyRecord histroyRecord = new HistroyRecord();
        histroyRecord.setDataType(1);
        histroyRecord.setTime(DateTimeUtils2.outNow());
        histroyRecord.setData(ByteBufUtil.hexDump(data));
        tcpSession.getHistroyRecordFIFO().add(histroyRecord);
    }

    @Override
    public void onReceiveComplete(TcpSession tcpSession, byte[] data) {

    }

    @Override
    public void onSend(TcpSession tcpSession, byte[] data) {
        HistroyRecord histroyRecord = new HistroyRecord();
        histroyRecord.setDataType(2);
        histroyRecord.setTime(DateTimeUtils2.outNow());
        histroyRecord.setData(ByteBufUtil.hexDump(data));
        tcpSession.getHistroyRecordFIFO().add(histroyRecord);
    }

    @Override
    public void onDisConnect(TcpSession tcpSession, String reason) {

    }

    @Override
    public void online(TcpSession tcpSession) {
        tcpSession.setHistroyRecordFIFO(new FIFO<>(maxHistroy));
    }

    @Override
    public void offline(TcpSession tcpSession, String message) {

    }
}
