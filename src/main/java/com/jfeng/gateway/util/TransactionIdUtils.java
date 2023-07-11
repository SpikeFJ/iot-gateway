package com.jfeng.gateway.util;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生成唯一标识
 */
public class TransactionIdUtils {
    public static final char ZERO = '0';
    public static final int TWO = 2;
    public static final int THREE = 3;
    public static final int ONE_MILLION = 1000000;
    public static final int ONE_THOUSAND = 1000;
    public static final String INITIAL_DID = "000000";

    static AtomicInteger seq = new AtomicInteger(0);

    public static String get(String clientId) {
        LocalDateTime now = LocalDateTime.now();

        StringBuilder sb = new StringBuilder();
        sb.append(clientId == null ? INITIAL_DID : clientId);
        sb.append(now.getYear());
        sb.append(StringUtils.fill(String.valueOf(now.getMonthValue()), ZERO, TWO));
        sb.append(StringUtils.fill(String.valueOf(now.getDayOfMonth()), ZERO, TWO));
        sb.append(StringUtils.fill(String.valueOf(now.getHour()), ZERO, TWO));
        sb.append(StringUtils.fill(String.valueOf(now.getMinute()), ZERO, TWO));
        sb.append(StringUtils.fill(String.valueOf(now.getSecond()), ZERO, TWO));
        sb.append(StringUtils.fill(String.valueOf(now.getNano() / ONE_MILLION), ZERO, THREE));
        sb.append(StringUtils.fill(String.valueOf(seq.getAndIncrement() % ONE_THOUSAND), ZERO, THREE));

        return sb.toString();
    }
}
