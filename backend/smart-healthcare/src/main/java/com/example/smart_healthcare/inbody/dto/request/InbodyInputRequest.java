package com.example.smart_healthcare.inbody.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InbodyInputRequest(
        @NotNull(message = "measuredAt is required")
        LocalDateTime measuredAt,

        @NotNull(message = "heightCm is required")
        @DecimalMin(value = "0.01", message = "heightCm must be greater than 0")
        BigDecimal heightCm,

        @NotNull(message = "weightKg is required")
        @DecimalMin(value = "0.01", message = "weightKg must be greater than 0")
        BigDecimal weightKg,

        @DecimalMin(value = "0.0", message = "bodyFatMassKg must be greater than or equal to 0")
        BigDecimal bodyFatMassKg,

        @DecimalMin(value = "0.0", message = "skeletalMuscleMassKg must be greater than or equal to 0")
        BigDecimal skeletalMuscleMassKg,

        @DecimalMin(value = "0.0", message = "bodyWaterL must be greater than or equal to 0")
        BigDecimal bodyWaterL,

        @DecimalMin(value = "0.0", message = "waistHipRatio must be greater than or equal to 0")
        BigDecimal waistHipRatio,

        @DecimalMin(value = "0.0", message = "visceralFatLevel must be greater than or equal to 0")
        Integer visceralFatLevel
) {
}
