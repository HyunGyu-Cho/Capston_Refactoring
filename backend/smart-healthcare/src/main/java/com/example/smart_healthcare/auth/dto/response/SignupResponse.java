package com.example.smart_healthcare.auth.dto.response;

public record SignupResponse(
        Long memberId,
        String email,
        String nickname,
        String status
) {
}

