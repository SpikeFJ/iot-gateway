package com.jfeng.gateway.store;

import com.jfeng.gateway.comm.ThreadFactoryImpl;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.FIFO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 设备连接
 */
@Component
@ConditionalOnProperty(name = "gateway.store.type",havingValue = "memory")
@Slf4j
public class MemoryCollectHistory implements SessionHistory {
    /**
     * 最大连接条数。默认最多50条
     */
    @Value("${gateway.store.maxHistorySessions:50}")
    private int maxCollectRecord;

    /**
     * 最大保留完整历史连接时长，单位秒。默认保存8小时内历史数据
     */
    @Value("${gateway.store.maxHistoryTime:28800}")
    private int maxHistoryTime;

    /**
     * 历史信息
     */
    public Map<String, FIFO<SessionLifeCycle>> history = new HashMap<>();

    @PostConstruct
    private void init() {
        Executors.newSingleThreadExecutor(new ThreadFactoryImpl("超时检测")).submit(() -> {

            try {
                Iterator<Map.Entry<String, FIFO<SessionLifeCycle>>> iterator = history.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, FIFO<SessionLifeCycle>> next = iterator.next();
                    SessionLifeCycle peek = next.getValue().peek();

                    LocalDateTime start = DateTimeUtils2.toLocalDateTime(peek.getCreateTime());
                    Duration duration = Duration.between(start, LocalDateTime.now());
                    if (duration.toMillis() > maxHistoryTime) {
                        iterator.remove();
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void save(String deviceId, SessionLifeCycle sessionLifeCycle) {
        if (history.containsKey(deviceId) == false) {
            history.put(deviceId, new FIFO<>(maxCollectRecord));
        }
        history.get(deviceId).add(sessionLifeCycle);
    }
}
