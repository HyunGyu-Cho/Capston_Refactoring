package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.common.entity.BaseEntity;
import com.example.smart_healthcare.dto.response.DietRecommendationResponseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * AI 기반 식단 추천 결과 엔티티
 * 새로운 AI 구조의 모든 식단 추천 필드를 저장
 */
@Entity
@Table(name = "ai_diet_recommendation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIDietRecommendation extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BIGINT")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbody_record_id", columnDefinition = "BIGINT")
    private InbodyRecord inbodyRecord;
    
    // 식습관 선호도
    @Column(length = 500)
    private String dietaryPreference;
    
    // AI 식단 추천 결과 필드들 (DietRecommendationResponseDto와 일치)
    @Column(columnDefinition = "TEXT")
    private String mealStyle; // 식단 스타일
    
    @Column(length = 200)
    private String dailyCalories; // 일일 권장 칼로리
    
    @Column(length = 100)
    private String macroSplit; // 영양소 비율
    
    @Column(columnDefinition = "TEXT")
    private String sampleMenu; // 예시 식단
    
    @Column(columnDefinition = "TEXT")
    private String shoppingList; // 장보기 리스트
    
    @Column(columnDefinition = "TEXT")
    private String precautions; // 유의사항
    
    @Column(columnDefinition = "TEXT")
    private String mealTiming; // 식사 타이밍
    
    @Column(columnDefinition = "TEXT")
    private String hydration; // 수분 섭취
    
    @Column(columnDefinition = "TEXT")
    private String supplements; // 보충제 제안
    
    @Column(columnDefinition = "TEXT")
    private String diets; // 요일별 식단 (JSON 형태)
    
    @Column(length = 50)
    @Builder.Default
    private String recommendationMethod = "AI"; // 추천 방법
    
    /**
     * DTO에서 엔티티로 변환하는 정적 메서드
     */
    public static AIDietRecommendation toEntity(DietRecommendationResponseDto dto, User user, InbodyRecord inbodyRecord, String preference) {
        // diets Map을 JSON 문자열로 변환
        String dietsJson = null;
        if (dto.diets() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                dietsJson = objectMapper.writeValueAsString(dto.diets());
            } catch (Exception e) {
                // JSON 변환 실패 시 null로 설정
                dietsJson = null;
            }
        }
        
        // Object 타입을 String으로 변환
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String dailyCaloriesStr = convertToString(dto.dailyCalories(), mapper);
        String macroSplitStr = convertToString(dto.macroSplit(), mapper);
        String shoppingListStr = convertToString(dto.shoppingList(), mapper);
        
        return AIDietRecommendation.builder()
                .user(user)
                .inbodyRecord(inbodyRecord)
                .dietaryPreference(preference)
                .mealStyle(dto.mealStyle())
                .dailyCalories(dailyCaloriesStr)
                .macroSplit(macroSplitStr)
                .sampleMenu(dto.sampleMenu())
                .shoppingList(shoppingListStr)
                .precautions(dto.precautions())
                .mealTiming(dto.mealTiming())
                .hydration(dto.hydration())
                .supplements(dto.supplements())
                .diets(dietsJson)
                .recommendationMethod("AI")
                .build();
    }
    
    /**
     * Object를 String으로 변환 헬퍼
     */
    private static String convertToString(Object obj, com.fasterxml.jackson.databind.ObjectMapper mapper) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
    
    /**
     * 엔티티에서 DTO로 변환하는 메서드
     */
    public DietRecommendationResponseDto toDto() {
        // diets JSON 문자열을 Map으로 파싱
        Map<String, Object> parsedDiets = null;
        if (this.diets != null && !this.diets.trim().isEmpty()) {
            try {
                // ObjectMapper를 사용하여 JSON 문자열을 Map으로 파싱
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                parsedDiets = objectMapper.readValue(this.diets, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                System.out.println("✅ diets JSON 파싱 성공 - ID: " + this.getId());
                System.out.println("✅ 파싱된 diets 키: " + parsedDiets.keySet());
            } catch (Exception e) {
                // JSON 파싱 실패 시 빈 맵 반환
                parsedDiets = new java.util.HashMap<>();
                System.err.println("❌ diets JSON 파싱 실패 - ID: " + this.getId());
                System.err.println("❌ 에러: " + e.getMessage());
                System.err.println("❌ diets 데이터: " + this.diets);
            }
        } else {
            // diets가 null이거나 비어있는 경우 빈 맵 반환
            parsedDiets = new java.util.HashMap<>();
            System.err.println("⚠️ diets가 null이거나 비어있음 - ID: " + this.getId());
        }
        
        return new DietRecommendationResponseDto(
                this.mealStyle,
                this.dailyCalories,
                this.macroSplit,
                this.sampleMenu,
                this.shoppingList,
                this.precautions,
                this.mealTiming,
                this.hydration,
                this.supplements,
                parsedDiets
        );
    }
    
}
