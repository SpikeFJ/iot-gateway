package com.jfeng.gateway.comm;

import com.jfeng.gateway.util.Crc16Utils;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
public class Modbus {
    private int address;
    private int functionCode;
    private int registerAddress;
    private int length;

    private String expression;
    private int dataType;//0:int 1:double 2:
    private int decimalLength;
    private String unit;

    private volatile int rspCode = -1;//默认-1 0:失败 1：成功
    private String result;
    private String exception;
    private String code;

    public void send(ByteBuf out) {
        out.clear();
        out.writeByte(address);
        out.writeByte(3);

        out.writeShort(registerAddress);
        out.writeShort(length);

        byte[] crc3 = Crc16Utils.getCRC3(out, 0, 6);
        out.writeByte(crc3[1]);
        out.writeByte(crc3[0]);
    }

    public ModbusResp receive(ByteBuf in) {
        if (in.readByte() != address) {
            return ModbusResp.fail(5, "不一致的从站地址：" + address);
        }
        byte functionCode = in.readByte();
        if (functionCode == 0x83) {
            return ModbusResp.fail(in.readByte(), "异常码：" + in.readByte());
        }
        int byteNum = in.readByte();
        short i = in.readShort();

        ExpressionParser parser = new SpelExpressionParser();

        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("x", i);

        if (dataType == 0) {
            Integer value = parser.parseExpression(expression.replace("x", "#x")).getValue(context, Integer.class);
            rspCode = 1;
            result = value.toString();
        } else if (dataType == 1 || dataType == 2) {
            Double value = parser.parseExpression(expression.replace("x", "#x")).getValue(context, Double.class);

            BigDecimal valueWrap = BigDecimal.valueOf(value);
            valueWrap.setScale(decimalLength, RoundingMode.HALF_DOWN);
            rspCode = 1;
            result = valueWrap.toPlainString();
        } else if (dataType == 3) {

        }

        return ModbusResp.success(code + ":" + result + " " + unit);
    }
}
