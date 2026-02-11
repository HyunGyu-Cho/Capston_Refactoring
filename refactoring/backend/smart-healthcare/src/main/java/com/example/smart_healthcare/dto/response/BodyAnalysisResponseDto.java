package com.example.smart_healthcare.dto.response;

import java.time.LocalDateTime;

/**
 * 체형 분석 AI 응답 DTO
 * JSON Schema로 구조가 보장됨
 */
public record BodyAnalysisResponseDto(
        Long id,                // 분석 결과 ID
        String label,           // 체형 라벨: "근육형", "날씬", "과체중", "적정", "마름", "운동선수급" 등
        String summary,         // 한 줄 요약: "균형잡힌 체형으로 건강한 상태입니다"
        String reasoning,       // 판단 근거: "BMI 23.6(정상), 체지방률 17.5%(정상), 근육량 양호"
        String tips,            // 생활/운동 팁: "현재 상태 유지를 위해 주 3회 근력운동과 유산소 권장"
        String healthRisk,      // 건강 위험도: "낮음", "보통", "높음"
        String muscleBalance,   // 근육 균형 분석: "좌우 균형 양호, 상하체 비율 적절"
        String metabolicHealth, // 대사 건강: "기초대사량 정상, 내장지방 양호"
        String bodyComposition, // 체성분 종합: "근육량 충분, 체지방률 적절, 수분 균형 양호"
        String bmiCategory,         // BMI 분류: "저체중", "정상", "과체중", "비만" 등
        String bodyFatCategory,     // 체지방률 분류: "정상", "높음" 등
        String visceralFatCategory, // 내장지방 위험도: "정상", "주의", "위험"
        Integer inbodyScore,        // 인바디점수 (있다면)
        String analysisMethod,      // 분석 방법: "AI", "RULE" 등
        LocalDateTime analyzedAt // 분석 날짜
) {}
