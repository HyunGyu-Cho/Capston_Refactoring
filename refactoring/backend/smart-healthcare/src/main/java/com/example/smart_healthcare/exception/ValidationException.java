package com.example.smart_healthcare.exception;

import org.springframework.http.HttpStatus;

/**
 * 데이터 유효성 검사 실패 시 발생하는 예외
 * 클라이언트 입력 데이터의 문제를 나타냄
 */
public class ValidationException extends BaseException {
    
    private final String field;
    
    public ValidationException(String message, String field, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
        this.field = field;
    }
    
    public ValidationException(String message, String field, String errorCode, Throwable cause) {
        super(message, errorCode, HttpStatus.BAD_REQUEST, cause);
        this.field = field;
    }
    
    public String getField() {
        return field;
    }
    
    @Override
    public String toLogString() {
        return String.format("ValidationException[%s]: %s (Field: %s, Code: %s)", 
            getClass().getSimpleName(), 
            getMessage(), 
            field, 
            getErrorCode());
    }
    
    @Override
    public boolean isRecoverable() {
        return true; // 유효성 검사 오류는 사용자가 수정할 수 있음
    }
    
    @Override
    public boolean isValidationError() {
        return true; // 항상 유효성 검사 오류
    }
}
