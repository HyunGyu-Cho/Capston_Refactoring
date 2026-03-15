package com.example.smart_healthcare.common.error;

import com.example.smart_healthcare.common.api.ApiError;
import com.example.smart_healthcare.common.api.ApiResponse;
import com.example.smart_healthcare.common.api.FieldErrorItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        ApiError error = new ApiError(
                ex.getErrorCode().code(),
                ex.getErrorCode().message(),
                traceId,
                null
        );
        return ResponseEntity.status(ex.getErrorCode().status()).body(ApiResponse.fail(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorItem> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toItem)
                .toList();
        ApiError error = new ApiError("AUTH-400-VAL", "Validation failed", getTraceId(request), errors);
        return ResponseEntity.badRequest().body(ApiResponse.fail(error));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        ApiError error = new ApiError("AUTH-400-VAL", "Validation failed", getTraceId(request), null);
        return ResponseEntity.badRequest().body(ApiResponse.fail(error));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ApiError error = new ApiError("COMMON-400-001", ex.getMessage(), getTraceId(request), null);
        return ResponseEntity.badRequest().body(ApiResponse.fail(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex, HttpServletRequest request) {
        ApiError error = new ApiError("COMMON-500-001", "Internal server error", getTraceId(request), null);
        return ResponseEntity.internalServerError().body(ApiResponse.fail(error));
    }

    private FieldErrorItem toItem(FieldError fieldError) {
        return new FieldErrorItem(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private String getTraceId(HttpServletRequest request) {
        Object traceId = request.getAttribute("traceId");
        if (traceId == null) {
            return UUID.randomUUID().toString();
        }
        return traceId.toString();
    }
}
