package com.jfeng.gateway.handler.none4;

import com.jfeng.gateway.channel.ChannelStatus;
import com.jfeng.gateway.channel.TcpChannel;
import com.jfeng.gateway.channel.TcpManage;
import com.jfeng.gateway.comm.*;
import com.jfeng.gateway.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 事件统计handler（包括收单个连接的收发数据、连接、断开次数)
 */
@Slf4j
public class EventStatisticsHandler extends ChannelDuplexHandler {

    public static final AttributeKey<TcpChannel> CLIENT_CHANNEL_ATTRIBUTE_KEY = AttributeKey.valueOf("ClientChannel");
    Sending[] toSend;
    int sendingIndex;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //log.debug("建立连接", ctx.channel().remoteAddress());
        Channel channel = ctx.channel();
        TcpChannel tcpChannel = new TcpChannel(channel, TcpManage.getInstance(Utils.getAddressInfo(channel.localAddress())));
        channel.attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).set(tcpChannel);

        MDC.put(Constant.LOG_ADDRESS, channel.toString());
        tcpChannel.connect();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        TcpChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();

        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        client.receive(bytes);

        log.debug("接收数据(原始)：" + ByteBufUtil.hexDump(byteBuf));

        if (client.getChannelStatus() == ChannelStatus.CONNECTED) {
            checkDtuCode(client, bytes);
        } else if (client.getChannelStatus() == ChannelStatus.LOGIN) {
            if (toSend != null) {
                if (sendingIndex < toSend.length) {
                    if (System.currentTimeMillis() - toSend[sendingIndex].sendTime > 3000) {
                        //超时
                    } else {
                        ModbusResp receive = toSend[sendingIndex].receive(byteBuf);
                        log.info("解析数据" + receive.getData());

                        if (sendingIndex + 1 != toSend.length) {
                            sendingIndex++;
                            sendNext(ctx.channel(), Unpooled.buffer(8));
                        }
                    }
                } else if (sendingIndex == toSend.length) {
                    log.info("本次发送结束");
                    sendingIndex = 0;
                }
            } else {
                log.info("未找到需要采集的数据");
            }

        }
//        ReferenceCountUtil.release(byteBuf);
        super.channelRead(ctx, msg);
    }

    private void checkDtuCode(TcpChannel tcpChannel, byte[] bytes) {
        String dtuCode = new String(bytes, Charset.forName("ASCII"));
        tcpChannel.setPacketId(dtuCode);
        tcpChannel.login();
        log.info("dtu编号:" + dtuCode);

        Map<String, CollectionSetting> settings = Instance.settings;
        CollectionSetting collectionSetting = settings.get(dtuCode);

        if (collectionSetting != null) {
            toSend = new Sending[collectionSetting.getModbusList().size()];
            for (int i = 0; i < collectionSetting.getModbusList().size(); i++) {
                toSend[i] = new Sending(collectionSetting.getModbusList().get(i));
            }

            Channel channel = tcpChannel.getChannel();
            channel.eventLoop().scheduleAtFixedRate(() -> {
                sendingIndex = 0;
                sendNext(channel, Unpooled.buffer(8));
            }, 1, collectionSetting.getConnectPeriod(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        TcpChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        if (client != null) {
            ByteBuf byteBuf = (ByteBuf) msg;
            client.send(ByteBufUtil.getBytes(byteBuf));

            //MDC.put(Constant.LOG_TRANSACTION_ID, TransactionIdUtils.get(client.getShortChannelId()));
            log.info("发送<<:" + ByteBufUtil.hexDump(byteBuf));
            //MDC.remove(Constant.LOG_TRANSACTION_ID);
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        TcpChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        if (client != null) {
            client.close("主动断开");
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常断开", cause.getMessage());
        TcpChannel client = ctx.channel().attr(CLIENT_CHANNEL_ATTRIBUTE_KEY).get();
        if (client != null) {
            client.close(cause.getMessage());
        }
        super.exceptionCaught(ctx, cause);
    }


    private void sendNext(Channel channel, ByteBuf buffer) {
        buffer = Unpooled.buffer(8);
        toSend[sendingIndex].send(buffer);
        channel.writeAndFlush(buffer);
    }

    class Sending {
        Sending(Modbus modbus) {
            this.modbus = modbus;
            sendTime = -1;
        }


        public void send(ByteBuf out) {
            this.modbus.send(out);
            this.sendTime = System.currentTimeMillis();
        }

        public ModbusResp receive(ByteBuf in) {
            this.sendTime = -1;
            return this.modbus.receive(in);
        }

        Modbus modbus;
        long sendTime;
    }
}
