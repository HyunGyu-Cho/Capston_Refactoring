package com.example.smart_healthcare.auth.dto.response;

import java.util.List;

public record MeResponse(
        Long memberId,
        String email,
        List<String> roles
) {
}
