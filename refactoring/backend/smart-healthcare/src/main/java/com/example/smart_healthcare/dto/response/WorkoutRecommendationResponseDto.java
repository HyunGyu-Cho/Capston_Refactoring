package com.example.smart_healthcare.dto.response;

import java.util.Map;

/**
 * 운동 추천 AI 응답 DTO
 * JSON Schema로 구조가 보장됨
 */
public record WorkoutRecommendationResponseDto(
        String programName,     // 프로그램명: "4주 체지방 감량 프로그램"
        String weeklySchedule,  // 주간 스케줄: "주 3-4회, 1회 45-60분"
        String caution,         // 주의사항: "무릎 부상 주의, 점진적 강도 증가"
        String warmup,          // 준비운동: "5분 가벼운 조깅 + 동적 스트레칭"
        String mainSets,        // 본운동: "1일차: 상체(팔굽혀펴기 3세트 12회), 2일차: 하체(스쿼트 3세트 15회)"
        String cooldown,        // 정리운동: "5분 정적 스트레칭 + 호흡법"
        String equipment,       // 필요 장비: "덤벨, 요가매트, 저항밴드"
        String targetMuscles,   // 타겟 근육: "전신, 특히 코어와 하체 중심"
        String expectedResults, // 기대 효과: "4주 후 체지방 2-3% 감소, 근지구력 향상"
        Map<String, Object> workouts  // 요일별 운동 프로그램: { "Monday": [...], "Wednesday": [...], "Friday": [...] }
) {}
