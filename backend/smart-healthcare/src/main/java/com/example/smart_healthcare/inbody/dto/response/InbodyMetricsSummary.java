package com.example.smart_healthcare.inbody.dto.response;

import java.math.BigDecimal;

public record InbodyMetricsSummary(
        BigDecimal heightCm,
        BigDecimal weightKg,
        BigDecimal bmi,
        BigDecimal bodyFatMassKg,
        BigDecimal bodyFatPercent,
        BigDecimal skeletalMuscleMassKg,
        BigDecimal bodyWaterL,
        BigDecimal waistHipRatio,
        Integer visceralFatLevel
) {
}
