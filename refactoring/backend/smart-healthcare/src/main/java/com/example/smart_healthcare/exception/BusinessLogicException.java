package com.example.smart_healthcare.exception;

import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 오류 시 발생하는 예외
 * 422 Unprocessable Entity 상태 코드와 함께 반환됨
 */
public class BusinessLogicException extends BaseException {
    
    private final String businessRule;
    private final String context;
    
    public BusinessLogicException(String message, String businessRule, String context) {
        super(message, "BUSINESS_LOGIC_ERROR", HttpStatus.UNPROCESSABLE_ENTITY);
        this.businessRule = businessRule;
        this.context = context;
    }
    
    public BusinessLogicException(String message, String businessRule, String context, Throwable cause) {
        super(message, "BUSINESS_LOGIC_ERROR", HttpStatus.UNPROCESSABLE_ENTITY, cause);
        this.businessRule = businessRule;
        this.context = context;
    }
    
    public String getBusinessRule() {
        return businessRule;
    }
    
    public String getContext() {
        return context;
    }
    
    @Override
    public String toLogString() {
        return String.format("BusinessLogicException[%s]: %s (Rule: %s, Context: %s)", 
            getClass().getSimpleName(), 
            getMessage(), 
            businessRule, 
            context);
    }
    
    @Override
    public boolean isRecoverable() {
        return true; // 비즈니스 로직 오류는 사용자가 수정할 수 있음
    }
    
    @Override
    public boolean isValidationError() {
        return true; // 비즈니스 규칙 위반은 유효성 검사 오류로 간주
    }
}
