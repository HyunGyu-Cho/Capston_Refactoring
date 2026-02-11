package com.example.smart_healthcare.exception;

/**
 * OpenAI API 관련 예외
 */
public class OpenAIException extends RuntimeException {
    private final String errorCode;
    private final int httpStatus;

    public OpenAIException(String message) {
        super(message);
        this.errorCode = "OPENAI_ERROR";
        this.httpStatus = 500;
    }

    public OpenAIException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public OpenAIException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "OPENAI_ERROR";
        this.httpStatus = 500;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
