package com.example.smart_healthcare.exception;

import org.springframework.http.HttpStatus;

/**
 * 요청된 리소스를 찾을 수 없을 때 발생하는 예외
 * 404 Not Found 상태 코드와 함께 반환됨
 */
public class ResourceNotFoundException extends BaseException {
    
    private final String resourceType;
    private final String resourceId;
    
    public ResourceNotFoundException(String message, String resourceType, String resourceId) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public ResourceNotFoundException(String message, String resourceType, String resourceId, Throwable cause) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, cause);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    @Override
    public String toLogString() {
        return String.format("ResourceNotFoundException[%s]: %s (Type: %s, ID: %s)", 
            getClass().getSimpleName(), 
            getMessage(), 
            resourceType, 
            resourceId);
    }
    
    @Override
    public boolean isRecoverable() {
        return true; // 리소스가 나중에 생성될 수 있음
    }
    
    @Override
    public boolean isNotFoundError() {
        return true; // 항상 리소스를 찾을 수 없는 오류
    }
}
