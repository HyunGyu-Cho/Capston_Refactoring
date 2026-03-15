package com.example.smart_healthcare.auth.session;

import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "app.auth.session-store", havingValue = "memory")
// refresh 세션 저장소의 메모리 구현체.
// 프로세스 재시작 시 데이터 유실이 허용되는 로컬/개발 환경용이다.
public class InMemoryRefreshSessionRepository implements RefreshSessionRepository {

    // jti -> 세션 상태
    private final Map<String, RefreshSession> sessions = new ConcurrentHashMap<>();
    // memberId -> 발급된 jti 집합
    private final Map<Long, Set<String>> memberJtiIndex = new ConcurrentHashMap<>();

    @Override
    // 세션을 저장하고 회원별 인덱스를 함께 갱신한다.
    public void save(String jti, RefreshSession session) {
        sessions.put(jti, session);
        memberJtiIndex.computeIfAbsent(session.getMemberId(), ignored -> ConcurrentHashMap.newKeySet()).add(jti);
    }

    @Override
    // 세션이 없거나 만료된 경우 Optional.empty를 반환한다.
    // 만료 엔트리는 조회 시점에 지연 삭제한다.
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
    // 세션이 존재하면 해당 세션을 폐기 상태로 변경한다.
    public void revoke(String jti, String reason, String replacedByJti) {
        findByJti(jti).ifPresent(it -> it.revoke(reason, replacedByJti));
    }

    @Override
    // 회원 기준 현재 jti 목록을 반환한다(없으면 빈 목록).
    public List<String> findJtiByMemberId(Long memberId) {
        return new ArrayList<>(memberJtiIndex.getOrDefault(memberId, Collections.emptySet()));
    }

    @Override
    // 세션 데이터와 회원 인덱스에서 해당 jti를 함께 제거한다.
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

