package com.example.smart_healthcare.dto.response;

import com.example.smart_healthcare.entity.Survey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResponseDto {

    private Long id;
    private Long userId;
    private Long inbodyRecordId;
    private String answerText;
    private LocalDateTime createdAt;
    
    // 설문조사 상세 데이터 (JSON 파싱된 결과)
    private String surveyData;
    private String text;
    private Integer workoutFrequency;
    private List<String> selectedDays;
    private List<String> selectedDaysEn;
    private String preferredDays;
    private Integer mealsPerDay;
    private String mealLabeling;
    private List<String> selectedMeals;
    private String selectedMealsLabel;
    private List<String> mealsToGenerate;

    /**
     * Entity를 DTO로 변환
     */
    public static SurveyResponseDto toDto(Survey survey) {
        SurveyResponseDtoBuilder builder = SurveyResponseDto.builder()
                .id(survey.getId())
                .userId(survey.getUser().getId())
                .inbodyRecordId(survey.getInbody() != null ? survey.getInbody().getId() : null)
                .answerText(survey.getAnswerText())
                .createdAt(survey.getCreatedAt())
                .surveyData(survey.getSurveyData());
        
        // surveyData가 있으면 JSON 파싱
        if (survey.getSurveyData() != null && !survey.getSurveyData().isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> dataMap = objectMapper.readValue(survey.getSurveyData(), Map.class);
                
                builder.text((String) dataMap.get("text"))
                       .workoutFrequency(dataMap.get("workoutFrequency") != null ? 
                           ((Number) dataMap.get("workoutFrequency")).intValue() : null)
                       .selectedDays((List<String>) dataMap.get("selectedDays"))
                       .selectedDaysEn((List<String>) dataMap.get("selectedDaysEn"))
                       .preferredDays((String) dataMap.get("preferredDays"))
                       .mealsPerDay(dataMap.get("mealsPerDay") != null ? 
                           ((Number) dataMap.get("mealsPerDay")).intValue() : null)
                       .mealLabeling((String) dataMap.get("mealLabeling"))
                       .selectedMeals((List<String>) dataMap.get("selectedMeals"))
                       .selectedMealsLabel((String) dataMap.get("selectedMealsLabel"))
                       .mealsToGenerate((List<String>) dataMap.get("mealsToGenerate"));
            } catch (JsonProcessingException e) {
                log.error("surveyData JSON 파싱 실패: {}", survey.getSurveyData(), e);
            }
        }
        
        return builder.build();
    }
}