package com.example.smart_healthcare.auth.security;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
