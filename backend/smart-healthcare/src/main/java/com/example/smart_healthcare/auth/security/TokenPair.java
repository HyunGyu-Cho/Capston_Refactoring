package com.example.smart_healthcare.auth.security;

// 발급된 access/refresh 토큰 쌍을 함께 전달하는 DTO.
public record TokenPair(
        String accessToken,
        String refreshToken
) {
}

