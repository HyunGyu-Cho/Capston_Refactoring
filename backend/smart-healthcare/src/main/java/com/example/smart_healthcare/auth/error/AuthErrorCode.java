package com.example.smart_healthcare.auth.error;

import com.example.smart_healthcare.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    AUTH_400_001("AUTH-400-001", "Invalid request format", HttpStatus.BAD_REQUEST),
    AUTH_400_VAL("AUTH-400-VAL", "Validation failed", HttpStatus.BAD_REQUEST),
    AUTH_401_001("AUTH-401-001", "Email or password does not match", HttpStatus.UNAUTHORIZED),
    AUTH_401_002("AUTH-401-002", "Access token is invalid", HttpStatus.UNAUTHORIZED),
    AUTH_401_003("AUTH-401-003", "Access token is expired", HttpStatus.UNAUTHORIZED),
    AUTH_401_004("AUTH-401-004", "Refresh token is missing", HttpStatus.UNAUTHORIZED),
    AUTH_401_005("AUTH-401-005", "Refresh token is invalid", HttpStatus.UNAUTHORIZED),
    AUTH_401_006("AUTH-401-006", "Refresh token is expired", HttpStatus.UNAUTHORIZED),
    AUTH_401_007("AUTH-401-007", "Refresh token is revoked", HttpStatus.UNAUTHORIZED),
    AUTH_401_008("AUTH-401-008", "Refresh token reuse detected", HttpStatus.UNAUTHORIZED),
    AUTH_403_001("AUTH-403-001", "Access denied", HttpStatus.FORBIDDEN),
    AUTH_409_001("AUTH-409-001", "Email already exists", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;

    AuthErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}

