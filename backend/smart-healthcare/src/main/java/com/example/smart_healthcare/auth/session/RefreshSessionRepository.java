package com.example.smart_healthcare.auth.session;

import java.util.List;
import java.util.Optional;

public interface RefreshSessionRepository {
    void save(String jti, RefreshSession session);
    Optional<RefreshSession> findByJti(String jti);
    void revoke(String jti, String reason, String replacedByJti);
    List<String> findJtiByMemberId(Long memberId);
    void delete(String jti);
}
