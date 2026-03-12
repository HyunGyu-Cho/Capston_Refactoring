package com.example.smart_healthcare.common.api;

import java.util.List;

public record ApiError(
        String code,
        String message,
        String traceId,
        List<FieldErrorItem> errors
) {
}
