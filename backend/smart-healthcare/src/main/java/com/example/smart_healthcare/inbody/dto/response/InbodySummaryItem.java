package com.example.smart_healthcare.inbody.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InbodySummaryItem(
        Long inbodyId,
        LocalDateTime measuredAt,
        BigDecimal weightKg,
        BigDecimal bmi,
        BigDecimal bodyFatPercent
) {
}
