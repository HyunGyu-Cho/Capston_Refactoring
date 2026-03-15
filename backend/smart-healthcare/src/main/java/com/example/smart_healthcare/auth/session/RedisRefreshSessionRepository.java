package com.example.smart_healthcare.auth.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.auth.session-store", havingValue = "redis", matchIfMissing = true)
// refresh 세션 저장소의 Redis 구현체.
// 운영 환경에서 기본 구현체로 사용된다.
public class RedisRefreshSessionRepository implements RefreshSessionRepository {

    // 세션 해시 키 접두사: auth:refresh:{jti}
    private static final String PREFIX = "auth:refresh:";
    // 회원 인덱스 Set 키 접두사: auth:refresh:member:{memberId}
    private static final String MEMBER_INDEX_PREFIX = "auth:refresh:member:";

    private final StringRedisTemplate redisTemplate;
    @Value("${app.auth.jwt.refresh-expire-sec:604800}")
    private long refreshExpireSec;

    @Override
    // 세션 필드를 Redis hash에 저장하고 토큰 만료 시각에 맞춰 TTL을 설정한다.
    // revoke-all 처리를 위해 회원 인덱스 Set에도 jti를 기록한다.
    public void save(String jti, RefreshSession session) {
        String key = key(jti);
        Map<String, String> values = new HashMap<>();
        values.put("memberId", String.valueOf(session.getMemberId()));
        values.put("tokenHash", session.getTokenHash());
        values.put("issuedAt", String.valueOf(session.getIssuedAt().getEpochSecond()));
        values.put("expiresAt", String.valueOf(session.getExpiresAt().getEpochSecond()));
        values.put("revoked", String.valueOf(session.isRevoked()));
        values.put("revokeReason", "");
        values.put("replacedByJti", "");
        redisTemplate.opsForHash().putAll(key, values);

        long ttlSeconds = Duration.between(Instant.now(), session.getExpiresAt()).getSeconds();
        if (ttlSeconds > 0) {
            redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        }

        redisTemplate.opsForSet().add(memberIndexKey(session.getMemberId()), jti);
        redisTemplate.expire(memberIndexKey(session.getMemberId()), Duration.ofSeconds(refreshExpireSec));
    }

    @Override
    // Redis hash 필드를 읽어 RefreshSession 객체로 복원한다.
    public Optional<RefreshSession> findByJti(String jti) {
        String key = key(jti);
        Map<Object, Object> values = redisTemplate.opsForHash().entries(key);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        long memberId = Long.parseLong(String.valueOf(values.get("memberId")));
        String tokenHash = String.valueOf(values.get("tokenHash"));
        Instant expiresAt = Instant.ofEpochSecond(Long.parseLong(String.valueOf(values.get("expiresAt"))));
        Instant issuedAt = Instant.ofEpochSecond(Long.parseLong(String.valueOf(values.get("issuedAt"))));
        RefreshSession session = new RefreshSession(memberId, tokenHash, issuedAt, expiresAt);
        boolean revoked = Boolean.parseBoolean(String.valueOf(values.get("revoked")));
        if (revoked) {
            session.revoke(String.valueOf(values.get("revokeReason")), String.valueOf(values.get("replacedByJti")));
        }
        return Optional.of(session);
    }

    @Override
    // 기존 세션을 폐기 상태로 표시하고 사유/대체 jti를 기록한다.
    public void revoke(String jti, String reason, String replacedByJti) {
        String key = key(jti);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForHash().put(key, "revoked", "true");
            redisTemplate.opsForHash().put(key, "revokeReason", reason == null ? "" : reason);
            redisTemplate.opsForHash().put(key, "replacedByJti", replacedByJti == null ? "" : replacedByJti);
        }
    }

    @Override
    // 회원 인덱스에 연결된 모든 jti를 반환한다.
    public List<String> findJtiByMemberId(Long memberId) {
        Set<String> values = redisTemplate.opsForSet().members(memberIndexKey(memberId));
        if (values == null) {
            return List.of();
        }
        return new ArrayList<>(values);
    }

    @Override
    // 세션 hash 키만 삭제한다.
    // 회원 인덱스 정리는 TTL 및 revoke-all 반복 처리에서 자연 정리된다.
    public void delete(String jti) {
        redisTemplate.delete(key(jti));
    }

    // jti로 세션 hash 키를 생성한다.
    private String key(String jti) {
        return PREFIX + jti;
    }

    // memberId로 회원 인덱스 Set 키를 생성한다.
    private String memberIndexKey(Long memberId) {
        return MEMBER_INDEX_PREFIX + memberId;
    }
}


