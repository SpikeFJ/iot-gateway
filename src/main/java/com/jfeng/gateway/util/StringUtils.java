package com.jfeng.gateway.util;

/**
 * 字符串工具类
 */
public class StringUtils {
    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }


    public static String fill(String value, char replace, int fillLength) {
        return fill(value, replace, fillLength, true);
    }

    public static String fill(String value, char replace, int fillLength, boolean isLeft) {
        int currentLength = value.length();
        if (currentLength >= fillLength)
            return value;

        StringBuilder sb = new StringBuilder();
        int diff = fillLength - currentLength;
        int index = 0;
        if (isLeft) {
            while (index++ < diff) {
                sb.append(replace);
            }
            sb.append(value);
        } else {
            sb.append(value);
            while (index++ < diff) {
                sb.append(replace);
            }
        }
        return sb.toString();
    }
}
