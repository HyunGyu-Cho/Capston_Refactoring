package com.example.smart_healthcare.auth.session;

import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "app.auth.session-store", havingValue = "memory")
public class InMemoryRefreshSessionRepository implements RefreshSessionRepository {

    private final Map<String, RefreshSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> memberJtiIndex = new ConcurrentHashMap<>();

    @Override
    public void save(String jti, RefreshSession session) {
        sessions.put(jti, session);
        memberJtiIndex.computeIfAbsent(session.getMemberId(), ignored -> ConcurrentHashMap.newKeySet()).add(jti);
    }

    @Override
    public Optional<RefreshSession> findByJti(String jti) {
        RefreshSession session = sessions.get(jti);
        if (session == null) {
            return Optional.empty();
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            delete(jti);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    @Override
    public void revoke(String jti, String reason, String replacedByJti) {
        findByJti(jti).ifPresent(it -> it.revoke(reason, replacedByJti));
    }

    @Override
    public List<String> findJtiByMemberId(Long memberId) {
        return new ArrayList<>(memberJtiIndex.getOrDefault(memberId, Collections.emptySet()));
    }

    @Override
    public void delete(String jti) {
        RefreshSession removed = sessions.remove(jti);
        if (removed == null) {
            return;
        }
        Set<String> jtIs = memberJtiIndex.get(removed.getMemberId());
        if (jtIs != null) {
            jtIs.remove(jti);
        }
    }
}
