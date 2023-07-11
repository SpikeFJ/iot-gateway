package com.jfeng.gateway.util;

public class ByteUtils {
    private ByteUtils() {
    }

    /**
     * 获取字节数组直至遇到某个指定值
     *
     * @param source
     * @param startIndex
     * @param target
     * @return
     */
    public static byte[] findBytes(byte[] source, int startIndex, byte target) throws Exception {
        int pointIndex = -1;
        for (int i = startIndex; i < source.length; i++) {
            if (source[i] == target) {
                pointIndex = i;
                break;
            }
        }

        if (pointIndex == -1) {
            throw new Exception("未找到" + target);
        }
        byte[] result = new byte[pointIndex - startIndex + 1];
        System.arraycopy(source, startIndex, result, 0, result.length);
        return result;
    }

    private static int[] AND = {0x1, 0x3, 0x7, 0xF, 0x1F, 0x3F, 0x7F, 0xFF};

    /**
     * 数据是否有效
     *
     * @param value
     * @param startIndex
     * @param length
     * @return
     */
    public static boolean isValid(byte[] value, int startIndex, int length) {
        for (int i = 0; i < length; i++) {
            if ((value[startIndex + i] & 0xFF) != 0xFF) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析数据，单位字节
     *
     * @param value
     * @param startIndex
     * @param length
     * @return
     */
    public static long parse(byte[] value, int startIndex, int length) {
        return parse(value, startIndex, length, true);
    }

    /**
     * 解析数据，单位字节
     *
     * @param value
     * @param startIndex
     * @param length
     * @param isEndian   是否大端
     * @return
     */
    public static long parse(byte[] value, int startIndex, int length, boolean isEndian) {
        if (!isValid(value, startIndex, length)) {
            return Long.MIN_VALUE;
        }

        if (length == 1)
            return value[startIndex] & 0xFF;

        long result = 0L;
        if (isEndian) {
            int shift = 8 * (length - 1);
            for (int i = startIndex; i < startIndex + length; i++) {
                result += ((long) value[i] & 0xFF) << shift;
                shift -= 8;
            }
        } else {
            int shift = 0;
            for (int i = startIndex; i < startIndex + length; i++) {
                result += ((long) value[i] & 0xFF) << shift;
                shift += 8;
            }
        }
        return result;
    }

    /**
     * 将二进制数字转为两位16进制字符串
     *
     * @param value
     * @return
     */
    public static String toHexString(Integer value) {
        return StringUtils.fill(Integer.toString(value, 16), '0', 2);
    }

    public static String toHexString(byte value) {
        return StringUtils.fill(Integer.toString(value, 16), '0', 2);
    }

    public static String toHexString(byte[] value, int startIndex, int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = startIndex; i < startIndex + length; i++) {
            sb.append(toHexString(value[startIndex]));
        }
        return sb.toString();
    }

    public static byte[] buildBitStore(byte[] value, boolean isBigEdian) {
        byte[] bitStore = new byte[value.length * 8];
        if (isBigEdian) {
            for (int i = 0; i < value.length; i++) {
                int start = i * 8;
                int tmp = value[i];
                bitStore[start] = (byte) (((tmp & 0xF0) >> 7) & 0x01);
                bitStore[start + 1] = (byte) (((tmp & 0xF0) >> 6) & 0x01);
                bitStore[start + 2] = (byte) (((tmp & 0xF0) >> 5) & 0x01);
                bitStore[start + 3] = (byte) (((tmp & 0xF0) >> 4) & 0x01);
                bitStore[start + 4] = (byte) ((tmp >> 3) & 0x01);
                bitStore[start + 5] = (byte) ((tmp >> 2) & 0x01);
                bitStore[start + 6] = (byte) ((tmp >> 1) & 0x01);
                bitStore[start + 7] = (byte) (tmp & 0x01);
            }
        } else {
            for (int i = value.length - 1; i >= 0; i--) {
                int start = i * 8;
                int tmp = value[i];
                bitStore[start] = (byte) (((tmp & 0xF0) >> 7) & 0x01);
                bitStore[start + 1] = (byte) (((tmp & 0xF0) >> 6) & 0x01);
                bitStore[start + 2] = (byte) (((tmp & 0xF0) >> 5) & 0x01);
                bitStore[start + 3] = (byte) (((tmp & 0xF0) >> 4) & 0x01);
                bitStore[start + 4] = (byte) ((tmp >> 3) & 0x01);
                bitStore[start + 5] = (byte) ((tmp >> 2) & 0x01);
                bitStore[start + 6] = (byte) ((tmp >> 1) & 0x01);
                bitStore[start + 7] = (byte) (tmp & 0x01);
            }
        }
        return bitStore;
    }

    /**
     * 基于位数组计算
     *
     * @param bitStore
     * @param startIndex
     * @param length
     * @return
     */
    public static long parseBit(byte[] bitStore, int startIndex, int length) {
        long result = 0;
        int bitShift = 0;

        int realStartIndex = bitStore.length - 1 - startIndex;
        for (int i = 0; i < length; i++) {
            if (realStartIndex > 0) {
                result |= (bitStore[realStartIndex--] << bitShift);
                bitShift++;
            }
        }
        return result;
    }


    public static byte[] encode(long value, int length) {
        return encode(value, length, true);
    }

    public static byte[] encode(long value, int length, boolean isBigEndian) {
        byte[] bytes = new byte[length];
        if (isBigEndian) {
            int shift = length == 1 ? 0 : (length - 1) * 8;
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) ((value >> shift) & 0xFF);
                shift -= 8;
            }
        } else {
            int shift = 0;
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (value >> shift);
                shift += 8;
            }
        }
        return bytes;
    }

    public static String parseString(byte[] source, int startIndex, java.nio.charset.Charset charset) {
        return parseString(source, startIndex, charset, source.length);
    }

    public static String parseString(byte[] source, int startIndex, java.nio.charset.Charset charset, int maxLength) {
        int realLength = 0;

        while (realLength < maxLength) {
            if (source[startIndex + realLength] == 0x00) {
                break;
            }
            realLength++;
        }
        return new String(source, startIndex, realLength, charset);
    }

    /**
     * 计算CRC16校验值
     *
     * @param arr_buff 数据
     * @param offset   起始位置
     * @param length   长度
     * @return
     */
    public static byte[] getCrc16(byte[] arr_buff, int offset, int length) {
        int len = offset + length;

        // 预置 1 个 16 位的寄存器为十六进制FFFF, 称此寄存器为 CRC寄存器。
        int crc = 0xABAB;//0xABAB初始值，和下位机对应
        int i, j;
        int data;
        for (i = offset; i < len; i++) {
            data = arr_buff[i];
            // 把第一个 8 位二进制数据 与 16 位的 CRC寄存器的低 8 位相异或, 把结果放于 CRC寄存器
            crc = ((crc & 0xFF00) | (crc & 0x00FF) ^ (data & 0xFF));
            for (j = 0; j < 8; j++) {
                // 把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位
                if ((crc & 0x0001) > 0) {
                    // 如果移出位为 1, CRC寄存器与多项式A001进行异或
                    crc = crc >> 1;
                    crc = crc ^ 0x8408;//0x8408为多项式0x1021的反转
                } else
                    // 如果移出位为 0,再次右移一位
                    crc = crc >> 1;
            }
        }
        crc = crc ^ 0xFFFF;//crc结果 异或 结果异或值

        byte[] tmp = new byte[2];
        tmp[0] = intToBytes(crc)[0];//低位在前
        tmp[1] = intToBytes(crc)[1];//高位在后
        return tmp;
    }

    /**
     * 将int转换成byte数组，低位在前，高位在后
     * 改变高低位顺序只需调换数组序号
     */
    private static byte[] intToBytes(int value) {
        byte[] src = new byte[2];
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    public static byte bcd2Dec(byte data) {
        byte value = (byte) ((data & 0x0F) + ((data & 0xF0) >> 4) * 10);
        return value;
    }

}
