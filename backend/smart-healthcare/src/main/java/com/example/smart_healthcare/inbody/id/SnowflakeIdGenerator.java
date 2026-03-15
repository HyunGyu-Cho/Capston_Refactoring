package com.example.smart_healthcare.inbody.id;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SnowflakeIdGenerator {

    // 커스텀 에폭: 2024-01-01 UTC (서비스 기준점)
    private static final long CUSTOM_EPOCH = 1704067200000L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private final long workerId;
    private final long datacenterId;

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeIdGenerator(
            @Value("${app.id.snowflake.worker-id:1}") long workerId,
            @Value("${app.id.snowflake.datacenter-id:1}") long datacenterId
    ) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("worker-id must be between 0 and " + MAX_WORKER_ID);
        }
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException("datacenter-id must be between 0 and " + MAX_DATACENTER_ID);
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        // 동일 밀리초 내 다건 생성은 sequence로 구분, 시간 역행은 예외 처리
        long currentTimestamp = currentTimestamp();
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards. Refusing to generate id.");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                currentTimestamp = waitUntilNextMillis(currentTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - CUSTOM_EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitUntilNextMillis(long timestamp) {
        long now = currentTimestamp();
        while (now <= timestamp) {
            now = currentTimestamp();
        }
        return now;
    }

    private long currentTimestamp() {
        return Instant.now().toEpochMilli();
    }
}
