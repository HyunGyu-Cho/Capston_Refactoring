package com.example.smart_healthcare.common.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String code();
    String message();
    HttpStatus status();
}
