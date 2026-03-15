package com.example.smart_healthcare.auth.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
// refresh 토큰 세션 메타데이터 도메인 모델.
// 토큰 해시와 생명주기 상태를 보관해 로테이션/폐기 검증에 사용한다.
public class RefreshSession {
    // 세션 소유 회원 ID.
    private final Long memberId;
    // refresh 토큰 원문의 SHA-256 해시값(원문은 저장하지 않음).
    private final String tokenHash;
    // 토큰 발급 시각.
    private final Instant issuedAt;
    // 토큰 만료 시각.
    private final Instant expiresAt;
    // 폐기 여부.
    private boolean revoked;
    // 폐기 사유(LOGOUT, ROTATED, REUSE_DETECTED 등).
    private String revokeReason;
    // 로테이션 시 새 토큰의 jti.
    private String replacedByJti;

    // 세션 상태를 폐기(revoked)로 전환한다.
    public void revoke(String reason, String replacedByJti) {
        this.revoked = true;
        this.revokeReason = reason;
        this.replacedByJti = replacedByJti;
    }
}


