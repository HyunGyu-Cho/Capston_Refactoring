package com.example.smart_healthcare.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 운동 추천 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutRecommendationRequestDto {

    @NotNull(message = "인바디 데이터는 필수입니다.")
    @Valid
    private InbodyDataRequestDto inbody;

    @NotNull(message = "설문 데이터는 필수입니다.")
    private Map<String, Object> survey;
}
