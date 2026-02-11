package com.example.smart_healthcare.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 모든 커스텀 예외의 기본 클래스
 * 공통적인 예외 처리 로직과 메타데이터를 제공
 */
@Getter
public abstract class BaseException extends RuntimeException {
    
    // ===== 기본 필드 =====
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final LocalDateTime timestamp;
    private final Map<String, Object> details;
    
    // ===== 생성자 =====
    
    protected BaseException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }
    
    protected BaseException(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }
    
    // ===== 상세 정보 관리 =====
    
    /**
     * 상세 정보 추가
     */
    public BaseException addDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
    
    /**
     * 여러 상세 정보 한번에 추가
     */
    public BaseException addDetails(Map<String, Object> details) {
        this.details.putAll(details);
        return this;
    }
    
    /**
     * 상세 정보 제거
     */
    public BaseException removeDetail(String key) {
        this.details.remove(key);
        return this;
    }
    
    /**
     * 모든 상세 정보 제거
     */
    public BaseException clearDetails() {
        this.details.clear();
        return this;
    }
    
    // ===== 유틸리티 메서드 =====
    
    /**
     * 예외가 복구 가능한지 확인
     */
    public boolean isRecoverable() {
        return httpStatus.is4xxClientError();
    }
    
    /**
     * 예외가 심각한 오류인지 확인
     */
    public boolean isCritical() {
        return httpStatus.is5xxServerError();
    }
    
    /**
     * 예외가 인증/권한 관련인지 확인
     */
    public boolean isAuthenticationError() {
        return httpStatus == HttpStatus.UNAUTHORIZED || httpStatus == HttpStatus.FORBIDDEN;
    }
    
    /**
     * 예외가 유효성 검사 관련인지 확인
     */
    public boolean isValidationError() {
        return httpStatus == HttpStatus.BAD_REQUEST;
    }
    
    /**
     * 예외가 리소스를 찾을 수 없는지 확인
     */
    public boolean isNotFoundError() {
        return httpStatus == HttpStatus.NOT_FOUND;
    }
    
    // ===== 로깅을 위한 메서드 =====
    
    /**
     * 예외 정보를 로깅용 문자열로 변환
     */
    public String toLogString() {
        return String.format("Exception[%s]: %s (HTTP: %s, Code: %s, Details: %s)", 
            getClass().getSimpleName(), 
            getMessage(), 
            httpStatus.value(), 
            errorCode, 
            details);
    }
    
    /**
     * 예외 정보를 Map으로 변환 (JSON 직렬화용)
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("errorCode", errorCode);
        map.put("message", getMessage());
        map.put("httpStatus", httpStatus.value());
        map.put("timestamp", timestamp);
        map.put("details", details);
        map.put("exceptionType", getClass().getSimpleName());
        
        if (getCause() != null) {
            map.put("cause", getCause().getMessage());
        }
        
        return map;
    }
    
    // ===== 정적 팩토리 메서드 =====
    
    /**
     * 간단한 예외 생성
     */
    public static BaseException of(String message, String errorCode, HttpStatus httpStatus) {
        return new BaseException(message, errorCode, httpStatus) {};
    }
    
    /**
     * 원인과 함께 예외 생성
     */
    public static BaseException of(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
        return new BaseException(message, errorCode, httpStatus, cause) {};
    }
}
