package com.example.smart_healthcare.inbody.cache;

import com.example.smart_healthcare.inbody.dto.response.InbodyListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class InbodyLatestCache {

    // 최신 5건은 조회 빈도가 높아 짧은 TTL로 캐시한다.
    private static final long TTL_SECONDS = 300L;

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    private final Counter redisHitCounter;
    private final Counter localHitCounter;
    private final Counter missCounter;
    private final Counter evictCounter;

    private final Map<Long, InbodyListResponse> latestFiveByMember = new ConcurrentHashMap<>();

    public InbodyLatestCache(ObjectMapper objectMapper, StringRedisTemplate redisTemplate, MeterRegistry meterRegistry) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.redisHitCounter = meterRegistry.counter("inbody.cache.latest5.hit", "store", "redis");
        this.localHitCounter = meterRegistry.counter("inbody.cache.latest5.hit", "store", "local");
        this.missCounter = meterRegistry.counter("inbody.cache.latest5.miss");
        this.evictCounter = meterRegistry.counter("inbody.cache.latest5.evict");
    }

    public Optional<InbodyListResponse> getLatestFive(Long memberId) {
        String key = latestFiveKey(memberId);
        try {
            // 1차: Redis 조회 (다중 인스턴스에서 공유 캐시)
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                InbodyListResponse value = objectMapper.readValue(cached, InbodyListResponse.class);
                // 2차: 로컬 캐시에 적재해 Redis 순간 장애/지연에 대비
                latestFiveByMember.put(memberId, value);
                redisHitCounter.increment();
                return Optional.of(value);
            }
        } catch (Exception ignored) {
            // Redis 장애/역직렬화 실패 시 로컬 캐시로 폴백
        }

        InbodyListResponse local = latestFiveByMember.get(memberId);
        if (local != null) {
            localHitCounter.increment();
            return Optional.of(local);
        }

        missCounter.increment();
        return Optional.empty();
    }

    public void putLatestFive(Long memberId, InbodyListResponse response) {
        latestFiveByMember.put(memberId, response);
        try {
            String payload = objectMapper.writeValueAsString(response);
            // TTL을 둬서 오래된 최신 데이터가 장시간 남지 않게 한다.
            redisTemplate.opsForValue().set(latestFiveKey(memberId), payload, TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // Redis 장애/직렬화 실패 시 로컬 캐시만 유지
        }
    }

    public void evict(Long memberId) {
        // 저장 직후 캐시를 제거해 정합성을 우선한다.
        latestFiveByMember.remove(memberId);
        evictCounter.increment();
        try {
            redisTemplate.delete(latestFiveKey(memberId));
        } catch (Exception ignored) {
            // Redis 장애는 서비스 오류로 전파하지 않는다.
        }
    }

    private String latestFiveKey(Long memberId) {
        return "inbody:latest5:" + memberId;
    }
}
