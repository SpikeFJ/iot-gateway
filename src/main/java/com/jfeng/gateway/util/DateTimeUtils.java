package com.jfeng.gateway.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils {
    private DateTimeUtils() {
    }

    /**
     * 按照 32960协议6.4格式解析为：yyyyMMddHHmmss
     *
     * @param value
     * @param startIndex
     * @return
     * @throws Exception
     */
    public static String toDateTime(byte[] value, int startIndex) throws Exception {
        return toDateTime(value, startIndex, false);
    }

    /**
     * 按照 32960协议6.4格式解析为：yyyyMMddHHmmssSSS
     *
     * @param value
     * @param startIndex
     * @param containMill 是否包含三位毫秒
     * @return
     * @throws Exception
     */
    public static String toDateTime(byte[] value, int startIndex, boolean containMill) throws Exception {
        if (ByteUtils.isValid(value, startIndex, containMill ? 8 : 6)) {
            validate(value, startIndex, containMill);

            StringBuilder sb = new StringBuilder();
            sb.append((value[startIndex++] & 0xFF) + TransactionIdUtils.ONE_THOUSAND * TransactionIdUtils.TWO);
            sb.append(StringUtils.fill(String.valueOf((value[startIndex++]) & 0xFF), TransactionIdUtils.ZERO, TransactionIdUtils.TWO));
            sb.append(StringUtils.fill(String.valueOf((value[startIndex++]) & 0xFF), TransactionIdUtils.ZERO, TransactionIdUtils.TWO));
            sb.append(StringUtils.fill(String.valueOf((value[startIndex++]) & 0xFF), TransactionIdUtils.ZERO, TransactionIdUtils.TWO));
            sb.append(StringUtils.fill(String.valueOf((value[startIndex++]) & 0xFF), TransactionIdUtils.ZERO, TransactionIdUtils.TWO));
            sb.append(StringUtils.fill(String.valueOf((value[startIndex++]) & 0xFF), TransactionIdUtils.ZERO, TransactionIdUtils.TWO));
            if (containMill) {
                int mill = ((value[startIndex] & 0xFF) << 8) + (value[startIndex + 1] & 0xFF);
                sb.append(StringUtils.fill(String.valueOf(mill), TransactionIdUtils.ZERO, TransactionIdUtils.THREE));
            } else {
                sb.append("000");
            }
            return sb.toString();
        } else {
            return "";
        }
    }


    /**
     * 校验日期格式
     *
     * @param value
     * @param containMill 是否包含毫秒
     * @throws Exception
     */
    private static void validate(byte[] value, int startIndex, boolean containMill) throws Exception {
        if (value == null || value.length < (containMill ? 8 : 6)) {
            throw new Exception("数据采集时间长度不足.");
        }
        validate(value[startIndex + 0], 0, 99, "年份");
        validate(value[startIndex + 1], 0, 12, "月份");
        validate(value[startIndex + 2], 1, 31, "日");
        validate(value[startIndex + 3], 0, 23, "小时");
        validate(value[startIndex + 4], 0, 59, "分钟");
        validate(value[startIndex + 5], 0, 59, "秒");
    }

    /**
     * 校验数据范围
     *
     * @param value
     * @param min
     * @param max
     * @param msg
     * @throws Exception
     */
    private static void validate(byte value, int min, int max, String msg) throws Exception {
        if (value < min || value > max) {
            throw new Exception("数据采集时间错误," + msg + ":" + value + "。");
        }
    }

    /**
     * 获取年份
     *
     * @param dateTime 日期时间：yyyyMMddHHmmss
     * @return
     */
    public static int getYear(long dateTime) {
        return (int) (dateTime / 10000000000L);
    }

    /**
     * 获取月份
     *
     * @param dateTime 日期时间：yyyyMMddHHmmss
     * @return
     */
    public static byte getMonth(long dateTime) {
        return (byte) (dateTime / 100000000 % 100);
    }

    /**
     * 获取日
     *
     * @param dateTime 日期时间：yyyyMMddHHmmss
     * @return
     */
    public static byte getDay(long dateTime) {
        return (byte) (dateTime / 1000000 % 100);
    }

    /**
     * 获取小时
     *
     * @param dateTime 日期时间：yyyyMMddHHmmss
     * @return
     */
    public static byte getHour(long dateTime) {
        return (byte) ((dateTime % 1000000) / 10000);
    }

    /**
     * 获取分钟
     *
     * @param dateTime 日期时间：yyyyMMddHHmmss
     * @return
     */
    public static byte getMinute(long dateTime) {
        return (byte) ((dateTime % 10000) / 100);
    }

    /**
     * 获取秒
     *
     * @param dateTime 日期时间：yyyyMMddHHmmss
     * @return
     */
    public static byte getSecond(long dateTime) {
        return (byte) (dateTime % 100);
    }


    /**
     * 显示日期
     *
     * @param dateTime 格式yyyyMMddHHmmss
     * @return 日期字符显示：yyyy-MM-dd HH:mm:ss
     */
    public static String outDateTime(long dateTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(getYear(dateTime));
        sb.append("-");
        sb.append(getMonth(dateTime));
        sb.append("-");
        sb.append(getDay(dateTime));
        sb.append("-");

        sb.append(getHour(dateTime));
        sb.append(":");
        sb.append(getMinute(dateTime));
        sb.append(":");
        sb.append(getSecond(dateTime));

        return sb.toString();
    }

    public static String outCurrentTimeMillis(long currentTimeMillis) {
        Date date = new Date(currentTimeMillis);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        return dateFormat.format(date);
    }

    /**
     * 显示日期
     *
     * @return 日期字符显示：yyyy-MM-dd HH:mm:ss
     */
    public static String outDateTimeNow() {
        LocalDateTime now = LocalDateTime.now();
        StringBuilder sb = new StringBuilder();
        sb.append(now.getYear());
        sb.append("-");
        sb.append(StringUtils.fill(String.valueOf(now.getMonthValue()), '0', 2));
        sb.append("-");
        sb.append(StringUtils.fill(String.valueOf(now.getDayOfMonth()), '0', 2));
        sb.append(" ");
        sb.append(StringUtils.fill(String.valueOf(now.getHour()), '0', 2));
        sb.append(":");
        sb.append(StringUtils.fill(String.valueOf(now.getMinute()), '0', 2));
        sb.append(":");
        sb.append(StringUtils.fill(String.valueOf(now.getSecond()), '0', 2));
        return sb.toString();
    }

    /**
     * 显示日期
     *
     * @return 日期字符显示：yyyy-MM-dd HH:mm:ss SSS
     */
    public static String outLongDateTimeNow() {
        LocalDateTime now = LocalDateTime.now();
        StringBuilder sb = new StringBuilder();
        sb.append(now.getYear());
        sb.append(Integer.toString(now.getMonthValue() + 100).substring(1));
        sb.append(Integer.toString(now.getDayOfMonth() + 100).substring(1));
        sb.append(Integer.toString(now.getHour() + 100).substring(1));
        sb.append(Integer.toString(now.getMinute() + 100).substring(1));
        sb.append(Integer.toString(now.getSecond() + 100).substring(1));
        sb.append(Integer.toString((now.getNano() / 1000_000) + 1000).substring(1));
        return sb.toString();
    }

    public static byte[] toBytes(String dateTime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, df);
        byte[] bytTime = new byte[6];
        bytTime[0] = (byte) (localDateTime.getYear() - 2000);
        bytTime[1] = (byte) localDateTime.getMonthValue();
        bytTime[2] = (byte) localDateTime.getDayOfMonth();
        bytTime[3] = (byte) localDateTime.getHour();
        bytTime[4] = (byte) localDateTime.getMinute();
        bytTime[5] = (byte) localDateTime.getSecond();
        return bytTime;
    }

    public static byte[] now() {
        LocalDateTime localDateTime = LocalDateTime.now();
        byte[] bytTime = new byte[6];
        bytTime[0] = (byte) (localDateTime.getYear() - 2000);
        bytTime[1] = (byte) localDateTime.getMonthValue();
        bytTime[2] = (byte) localDateTime.getDayOfMonth();
        bytTime[3] = (byte) localDateTime.getHour();
        bytTime[4] = (byte) localDateTime.getMinute();
        bytTime[5] = (byte) localDateTime.getSecond();
        return bytTime;
    }



    public static String outEpochMilli(long currentMill) {
        Instant instant = Instant.ofEpochMilli(currentMill);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return outLocalDateTime(localDateTime);
    }

    public static String outLocalDateTime(LocalDateTime localDateTime) {
        return outLocalDateTime(localDateTime, "yyyy-MM-dd HH:mm:ss SSS");
    }

    public static String outLocalDateTime(LocalDateTime localDateTime, String formatPattern) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatPattern);
        return dtf.format(localDateTime);
    }
}
