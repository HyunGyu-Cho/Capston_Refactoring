package com.example.smart_healthcare.dto.request;

import com.example.smart_healthcare.entity.Survey;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyRequestDto {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    private Long inbodyRecordId;

    @NotNull(message = "설문 답변은 필수입니다.")
    private String answerText;
    
    // 설문조사 상세 데이터 (JSON 형태로 저장됨)
    private String surveyData;

    /**
     * DTO를 Entity로 변환
     */
    public Survey toEntity() {
        return Survey.builder()
                .answerText(this.answerText)
                .surveyData(this.surveyData)
                .build();
    }
}