package com.example.smart_healthcare.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * API 응답을 위한 공통 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class ApiResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
    private LocalDateTime timestamp;
    private String method;

    // 정적 팩토리 메서드들
    public static <T> ApiResponseDto<T> success(T data) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.setSuccess(true);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> ApiResponseDto<T> success(String message, T data) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> ApiResponseDto<T> error(String error) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.setSuccess(false);
        response.setError(error);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> ApiResponseDto<T> error(String message, String error) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setError(error);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public ApiResponseDto<T> withMethod(String method) {
        this.method = method;
        return this;
    }
}
