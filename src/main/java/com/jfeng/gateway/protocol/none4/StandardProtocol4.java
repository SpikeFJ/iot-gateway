package com.jfeng.gateway.protocol.none4;

import com.jfeng.gateway.protocol.Protocol;
import com.jfeng.gateway.protocol.ValidException;
import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Getter
@Setter
public class StandardProtocol4 implements Protocol {
    public static final byte HEADER = 0x23; //包头
    public static final Integer HEADER_LENGTH = 2; //包头长度
    public static final Integer COMMAND_LENGTH = 1;  //命令字长度
    public static final Integer TERMINAL_LENGTH = 17;  //通信唯一标识长度
    public static final Integer VERSION_LENGTH = 1; // 版本长度
    public static final Integer ENCRYPT_LENGTH = 1; //加密长度
    private static CheckCodeProcessor checkCodeProcessor;//校验码计算器

    static {
        checkCodeProcessor = new CheckCodeProcessor();
    }

    public StandardProtocol4() {

    }

    protected String header = "##";//起始符，固定为 ASCII 字符“##”，用“0x23, 0x23”表示
    protected int cmd;//命令单元
    protected String terminalNo = "00000000000000000"; //机械环保代码，机械环保代码是识别的唯一标识，由 17 位字码构成
    protected int softVersion; //终端软件版本。车载终端软件版本号有效值范围 0~255
    protected int encrypt = 1;//加密方式。0x01：不加密；0x02：RSA算法加密；0x03：国密 SM2 算法0xFE  表示异常，0xFF表示无效，其他预留
    protected byte[] body; //数据单元
    protected int checkCode;//校验码


    @Override
    public String name() {
        return "非四协议";
    }

    @Override
    public void decode(ByteBuf in) throws Exception {
        int availableBytes = in.readableBytes();

        int minLength = HEADER_LENGTH + COMMAND_LENGTH + TERMINAL_LENGTH + VERSION_LENGTH + ENCRYPT_LENGTH + HEADER_LENGTH + ENCRYPT_LENGTH;
        if (availableBytes < minLength) {
            throw new Exception("协议解析失败：缺少解析数据。");
        }

        if (in.readByte() != HEADER) {
            throw new Exception("协议解析失败：起始符错误");
        }
        if (in.readByte() != HEADER) {
            throw new Exception("协议解析失败：起始符错误");
        }

        int from = in.readerIndex();

        this.cmd = in.readByte();
        this.terminalNo = in.readCharSequence(17, Charset.forName("ASCII")).toString();

        this.softVersion = in.readUnsignedByte();
        this.encrypt = in.readUnsignedByte();

        int dataLength = in.readUnsignedShort();

        checkCodeProcessor.reset();
        in.forEachByte(from, 22 + dataLength, checkCodeProcessor);

        int expected = checkCodeProcessor.getCheckCode();
        this.checkCode = in.getByte(availableBytes - 1);

        if (!Objects.equals(expected, this.checkCode)) {
            throw new ValidException(this.checkCode, expected);
        }

        this.body = new byte[dataLength];
        in.readBytes(this.body, 0, dataLength);
    }

    /**
     * 返回协议对应的二进制数据报文
     *
     * @return
     */
    @Override
    public void encode(ByteBuf out) throws Exception {

        out.writeBytes(header.getBytes(StandardCharsets.US_ASCII));

        int from = out.writerIndex();
        out.writeByte(this.cmd);

        out.writeBytes(this.terminalNo.getBytes(StandardCharsets.US_ASCII));
        out.writeByte(softVersion);
        out.writeByte(encrypt);

        out.writeShort(body.length);

        out.writeBytes(body);

        out.forEachByte(from, body.length + 20, checkCodeProcessor);
        out.writeByte(checkCodeProcessor.getCheckCode());
    }

    /**
     * 校验码计算
     */
    public static final class CheckCodeProcessor implements ByteProcessor {
        @Getter
        private byte checkCode = (byte) 0x00;

        @Override
        public boolean process(byte value) throws Exception {
            checkCode ^= value;
            return true;
        }

        private void reset() {
            this.checkCode = (byte) 0x00;
        }
    }
}
