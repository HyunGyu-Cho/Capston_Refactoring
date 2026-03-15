package com.example.smart_healthcare.auth.security;

import org.springframework.security.core.AuthenticationException;

// 인증 에러 코드를 함께 전달하기 위한 AuthenticationException.
public class AuthFailureException extends AuthenticationException {
    private final String code;

    public AuthFailureException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
