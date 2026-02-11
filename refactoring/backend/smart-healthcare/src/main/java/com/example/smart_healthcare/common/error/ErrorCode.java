package com.example.smart_healthcare.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // 기본 HTTP 상태 코드 분류
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "400", "잘못된 요청입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "403", "권한이 없습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "404", "리소스를 찾을 수 없습니다"),
    CONFLICT(HttpStatus.CONFLICT, "409", "충돌이 발생했습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500", "서버 오류가 발생했습니다"),
    
    // 클라이언트가 특별한 처리가 필요한 핵심 도메인 상황
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "409", "이미 사용 중인 이메일입니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "401", "잘못된 인증 정보입니다"),
    INSUFFICIENT_DATA(HttpStatus.BAD_REQUEST, "400", "추천을 위한 데이터가 부족합니다");
    
    private final HttpStatus status;
    private final String code;
    private final String message;
}
