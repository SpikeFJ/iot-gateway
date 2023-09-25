package com.jfeng.gateway.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils2 {
    private DateTimeUtils2() {
    }

    public static Date toDate(LocalDateTime value) {
        final ZonedDateTime zonedDateTime = value.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        final Instant instant = date.toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String outNow() {
        return outString(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
    }

    public static String outNowWithMill() {
        return outString(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss SSS");
    }

    public static String outString(LocalDateTime value) {
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String outString(LocalDateTime value, String format) {
        return value.format(DateTimeFormatter.ofPattern(format));
    }

    public static String outString(Date value, String format) {
        return outString(value.getTime(), format);
    }

    public static String outString(long value) {
        return outString(value, "yyyy-MM-dd HH:mm:ss");
    }

    public static String outString(long value, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(value);
    }

    public static LocalDateTime parse(String value, String format) {
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(format));
    }
}
