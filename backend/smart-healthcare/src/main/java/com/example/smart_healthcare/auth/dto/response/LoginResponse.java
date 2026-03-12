package com.example.smart_healthcare.auth.dto.response;

public record LoginResponse(
        String accessToken,
        long accessTokenExpiresInSec,
        String tokenType
) {
}
