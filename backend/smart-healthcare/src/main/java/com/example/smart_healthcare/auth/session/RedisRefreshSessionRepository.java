package com.example.smart_healthcare.auth.session;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "app.auth.session-store", havingValue = "redis", matchIfMissing = true)
public class RedisRefreshSessionRepository implements RefreshSessionRepository {

    private static final String PREFIX = "auth:refresh:";
    private static final String MEMBER_INDEX_PREFIX = "auth:refresh:member:";

    private final StringRedisTemplate redisTemplate;
    private final long refreshExpireSec;

    public RedisRefreshSessionRepository(
            StringRedisTemplate redisTemplate,
            @Value("${app.auth.jwt.refresh-expire-sec:604800}") long refreshExpireSec
    ) {
        this.redisTemplate = redisTemplate;
        this.refreshExpireSec = refreshExpireSec;
    }

    @Override
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
    public void revoke(String jti, String reason, String replacedByJti) {
        String key = key(jti);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForHash().put(key, "revoked", "true");
            redisTemplate.opsForHash().put(key, "revokeReason", reason == null ? "" : reason);
            redisTemplate.opsForHash().put(key, "replacedByJti", replacedByJti == null ? "" : replacedByJti);
        }
    }

    @Override
    public List<String> findJtiByMemberId(Long memberId) {
        Set<String> values = redisTemplate.opsForSet().members(memberIndexKey(memberId));
        if (values == null) {
            return List.of();
        }
        return new ArrayList<>(values);
    }

    @Override
    public void delete(String jti) {
        redisTemplate.delete(key(jti));
    }

    private String key(String jti) {
        return PREFIX + jti;
    }

    private String memberIndexKey(Long memberId) {
        return MEMBER_INDEX_PREFIX + memberId;
    }
}
