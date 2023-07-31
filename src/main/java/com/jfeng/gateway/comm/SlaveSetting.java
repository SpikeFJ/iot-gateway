package com.jfeng.gateway.comm;

import com.jfeng.gateway.util.Crc16Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 从站配置
 */
@Getter
@Setter
public class SlaveSetting {

    /**
     * 从站名称
     */
    private String name;

    /**
     * 从站地址
     */
    private int address;

    /**
     * 寄存器配置
     */
    private List<RegisterSetting> registerSettings = new ArrayList<>();


    public List<String> initModbusCode() {
        List<String> modbus = new ArrayList<>();

        ByteBuf byteBuf = Unpooled.buffer(8);

        for (RegisterSetting registerSetting : registerSettings) {
            byteBuf.clear();

            byteBuf.writeByte(address);
            byteBuf.writeByte(3);

            byteBuf.writeShort(registerSetting.getAddress());
            byteBuf.writeShort(registerSetting.getLength());

            byte[] crc3 = Crc16Utils.getCRC3(byteBuf, 0, 6);
            byteBuf.writeByte(crc3[1]);
            byteBuf.writeByte(crc3[0]);

            modbus.add(ByteBufUtil.hexDump(byteBuf));
        }

        return modbus;
    }
}
