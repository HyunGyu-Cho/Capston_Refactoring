package com.example.smart_healthcare.auth.session;

import java.time.Instant;

public class RefreshSession {
    private final Long memberId;
    private final String tokenHash;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private boolean revoked;
    private String revokeReason;
    private String replacedByJti;

    public RefreshSession(Long memberId, String tokenHash, Instant issuedAt, Instant expiresAt) {
        this.memberId = memberId;
        this.tokenHash = tokenHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public String getReplacedByJti() {
        return replacedByJti;
    }

    public String getRevokeReason() {
        return revokeReason;
    }

    public void revoke(String reason, String replacedByJti) {
        this.revoked = true;
        this.revokeReason = reason;
        this.replacedByJti = replacedByJti;
    }
}
