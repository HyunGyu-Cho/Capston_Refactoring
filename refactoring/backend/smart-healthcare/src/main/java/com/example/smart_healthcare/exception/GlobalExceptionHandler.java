package com.example.smart_healthcare.exception;

import com.example.smart_healthcare.common.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 * 모든 Controller에서 발생하는 예외를 중앙에서 처리합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBaseException(BaseException e) {
        log.error("비즈니스 예외 발생: {}", e.getMessage(), e);
        
        HttpStatus status = e.getHttpStatus();
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        
        return ResponseEntity.status(status)
                .body(ApiResponseDto.error(e.getMessage()));
    }

    /**
     * 유효성 검증 실패 예외 처리 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e) {
        log.error("유효성 검증 실패: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponseDto<Map<String, String>> response = new ApiResponseDto<>();
        response.setSuccess(false);
        response.setError("입력 데이터가 유효하지 않습니다.");
        response.setData(errors);
        response.setTimestamp(java.time.LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 바인딩 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleBindException(BindException e) {
        log.error("바인딩 예외 발생: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponseDto<Map<String, String>> response = new ApiResponseDto<>();
        response.setSuccess(false);
        response.setError("입력 데이터 바인딩에 실패했습니다.");
        response.setData(errors);
        response.setTimestamp(java.time.LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.error("타입 불일치 예외 발생: {}", e.getMessage());
        
        String message = String.format("'%s' 값이 올바르지 않습니다.", e.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(message));
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgumentException(
            IllegalArgumentException e) {
        log.error("잘못된 인수 예외 발생: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(e.getMessage()));
    }

    /**
     * NullPointerException 처리
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleNullPointerException(
            NullPointerException e) {
        log.error("Null 포인터 예외 발생: {}", e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("서버 내부 오류가 발생했습니다."));
    }

    /**
     * 모든 예외 처리 (최종 fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAllExceptions(Exception e) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("서버 내부 오류가 발생했습니다."));
    }
}