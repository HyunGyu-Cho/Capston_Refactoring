package com.example.smart_healthcare.exception;

import org.springframework.http.HttpStatus;

/**
 * 인증되지 않은 접근 시 발생하는 예외
 * 401 Unauthorized 상태 코드와 함께 반환됨
 */
public class UnauthorizedException extends BaseException {
    
    private final String requiredRole;
    private final String attemptedAction;
    
    public UnauthorizedException(String message, String requiredRole, String attemptedAction) {
        super(message, "UNAUTHORIZED_ACCESS", HttpStatus.UNAUTHORIZED);
        this.requiredRole = requiredRole;
        this.attemptedAction = attemptedAction;
    }
    
    public UnauthorizedException(String message, String requiredRole, String attemptedAction, Throwable cause) {
        super(message, "UNAUTHORIZED_ACCESS", HttpStatus.UNAUTHORIZED, cause);
        this.requiredRole = requiredRole;
        this.attemptedAction = attemptedAction;
    }
    
    public String getRequiredRole() {
        return requiredRole;
    }
    
    public String getAttemptedAction() {
        return attemptedAction;
    }
    
    @Override
    public String toLogString() {
        return String.format("UnauthorizedException[%s]: %s (Required: %s, Action: %s)", 
            getClass().getSimpleName(), 
            getMessage(), 
            requiredRole, 
            attemptedAction);
    }
    
    @Override
    public boolean isRecoverable() {
        return true; // 사용자가 로그인하거나 권한을 얻을 수 있음
    }
    
    @Override
    public boolean isAuthenticationError() {
        return true; // 항상 인증 관련 오류
    }
}
