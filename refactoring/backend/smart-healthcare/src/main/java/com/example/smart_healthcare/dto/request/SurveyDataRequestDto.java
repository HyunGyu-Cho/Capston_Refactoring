package com.example.smart_healthcare.dto.request;

import java.util.List;

/**
 * AI 분석용 설문조사 데이터 DTO
 */
public record SurveyDataRequestDto(
        String text,                    // 설문조사 텍스트
        String workoutFrequency,        // 운동 빈도 (예: "주 3회")
        List<String> selectedDays,      // 선택된 운동 요일 (예: ["월", "수", "금"])
        List<String> selectedDaysEn,     // ✅ 영문 운동 요일 (예: ["Monday", "Wednesday", "Friday"])
        String preferredDays,           // 선호 운동 요일 설명
        String mealsPerDay,             // 하루 식사 횟수 (예: "3")
        String mealLabeling,            // 식사 라벨링 방식 ("generic" 또는 "byType")
        List<String> selectedMeals,     // 선택된 식사 유형 (예: ["아침", "점심", "저녁"])
        String selectedMealsLabel,      // 선택된 식사 라벨 설명
        List<String> mealsToGenerate    // ✅ 생성할 끼니 목록 (예: ["breakfast", "lunch", "dinner"])
) {
    
    /**
     * 운동 일수 계산
     */
    public int getWorkoutDaysCount() {
        return selectedDays != null ? selectedDays.size() : 3; // 기본값 3일
    }
    
    /**
     * 식사 횟수 계산
     */
    public int getMealCount() {
        if (mealLabeling != null && "byType".equals(mealLabeling)) {
            return selectedMeals != null ? selectedMeals.size() : 3; // 기본값 3끼
        } else {
            try {
                return Integer.parseInt(mealsPerDay);
            } catch (NumberFormatException e) {
                return 3; // 기본값 3끼
            }
        }
    }
    
    /**
     * 영문 운동 요일 반환 (우선 사용)
     */
    public List<String> getSelectedDaysEn() {
        if (selectedDaysEn != null && !selectedDaysEn.isEmpty()) {
            return selectedDaysEn;
        }
        return getWorkoutDaysInEnglish(); // fallback
    }
    
    /**
     * 운동 요일을 영어로 변환 (fallback)
     */
    public List<String> getWorkoutDaysInEnglish() {
        if (selectedDays == null) {
            return List.of("Monday", "Wednesday", "Friday"); // 기본값
        }
        
        return selectedDays.stream()
                .map(this::convertDayToEnglish)
                .toList();
    }
    
    /**
     * 식사 유형을 영어로 변환
     */
    public List<String> getMealTypesInEnglish() {
        if (selectedMeals == null) {
            return List.of("breakfast", "lunch", "dinner"); // 기본값
        }
        
        return selectedMeals.stream()
                .map(this::convertMealToEnglish)
                .toList();
    }
    
    private String convertDayToEnglish(String koreanDay) {
        return switch (koreanDay) {
            case "월", "월요일" -> "Monday";
            case "화", "화요일" -> "Tuesday";
            case "수", "수요일" -> "Wednesday";
            case "목", "목요일" -> "Thursday";
            case "금", "금요일" -> "Friday";
            case "토", "토요일" -> "Saturday";
            case "일", "일요일" -> "Sunday";
            default -> "Monday";
        };
    }
    
    private String convertMealToEnglish(String koreanMeal) {
        return switch (koreanMeal) {
            case "아침" -> "breakfast";
            case "점심" -> "lunch";
            case "저녁" -> "dinner";
            case "간식" -> "snack";
            default -> "breakfast";
        };
    }
}
