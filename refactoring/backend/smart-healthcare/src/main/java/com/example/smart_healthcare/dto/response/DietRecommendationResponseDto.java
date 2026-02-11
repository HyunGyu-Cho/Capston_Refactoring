package com.example.smart_healthcare.dto.response;

import java.util.Map;

/**
 * 식단 추천 AI 응답 DTO
 * JSON Schema로 구조가 보장됨
 */
public record DietRecommendationResponseDto(
        String mealStyle,       // 식단 스타일: "고단백·균형형 한식 위주"
        Object dailyCalories,   // 일일 권장 칼로리: 1900 (숫자) 또는 "1900kcal" (문자열 호환)
        Object macroSplit,      // 영양소 비율: { "carbs": 50, "protein": 25, "fat": 25 } (객체) 또는 "탄수화물 45%..." (문자열 호환)
        String sampleMenu,      // 예시 식단: "현미·살코기·채소 중심, 가공당 저감"
        Object shoppingList,    // 장보기 리스트: ["현미", "닭가슴살", ...] (배열) 또는 "현미, 닭가슴살..." (문자열 호환)
        String precautions,     // 유의사항: "과식·야식 자제, 나트륨·당류 저감"
        String mealTiming,      // 식사 타이밍: "아침 07:00 ~ 08:30 / 저녁 18:00 ~ 19:30"
        String hydration,       // 수분 섭취: "하루 2L 이상 수분 섭취"
        String supplements,     // 보충제 제안: "오메가3, 종합비타민(필요 시)"
        Map<String, Object> diets  // 요일별 식단 구조: { "Monday": { "breakfast": {...}, "dinner": {...} }, ... }
) {}
