package com.example.smart_healthcare.auth.session;

import java.util.List;
import java.util.Optional;

// refresh 토큰 세션 상태를 다루는 저장소 계약.
// 구현체는 Redis, 메모리, 외부 저장소 등으로 확장할 수 있다.
public interface RefreshSessionRepository {
    // jti 식별자 기준으로 refresh 세션 상태를 저장한다.
    void save(String jti, RefreshSession session);

    // jti 기준으로 세션 상태(활성/폐기 포함)를 조회한다.
    Optional<RefreshSession> findByJti(String jti);

    // refresh 세션을 폐기(revoke) 상태로 변경한다.
    // replacedByJti는 토큰 로테이션으로 신규 토큰이 발급된 경우 사용한다.
    void revoke(String jti, String reason, String replacedByJti);

    // 회원에게 발급된 모든 jti 목록을 반환한다.
    // 재사용 감지 등 revoke-all 처리에 활용된다.
    List<String> findJtiByMemberId(Long memberId);

    // 단일 refresh 세션 엔트리를 삭제한다.
    void delete(String jti);
}

