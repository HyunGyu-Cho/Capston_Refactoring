package com.example.smart_healthcare.inbody.error;

import com.example.smart_healthcare.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum InbodyErrorCode implements ErrorCode {
    INBODY_404_001("INBODY-404-001", "Inbody data not found", HttpStatus.NOT_FOUND),
    INBODY_403_001("INBODY-403-001", "No permission to access this inbody data", HttpStatus.FORBIDDEN),
    INBODY_409_001("INBODY-409-001", "Duplicate measurement time for member", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;

    InbodyErrorCode(String code, String message, HttpStatus status) {
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
