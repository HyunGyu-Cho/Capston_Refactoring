package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.common.entity.BaseEntity;
import com.example.smart_healthcare.dto.response.WorkoutRecommendationResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * AI 기반 운동 추천 결과 엔티티
 * 새로운 AI 구조의 모든 운동 추천 필드를 저장
 */
@Entity
@Table(name = "ai_workout_recommendation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIWorkoutRecommendation extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BIGINT")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbody_record_id", columnDefinition = "BIGINT")
    private InbodyRecord inbodyRecord;
    
    // 운동 목표
    @Column(length = 200)
    private String goal;
    
    // AI 운동 추천 결과 필드들 (WorkoutRecommendationResponseDto와 일치)
    @Column(length = 100)
    private String programName; // 프로그램명
    
    @Column(columnDefinition = "TEXT")
    private String weeklySchedule; // 주간 스케줄
    
    @Column(columnDefinition = "TEXT")
    private String caution; // 주의사항
    
    @Column(columnDefinition = "TEXT")
    private String warmup; // 준비운동
    
    @Column(columnDefinition = "TEXT")
    private String mainSets; // 본운동
    
    @Column(columnDefinition = "TEXT")
    private String cooldown; // 정리운동
    
    @Column(columnDefinition = "TEXT")
    private String equipment; // 필요 장비
    
    @Column(columnDefinition = "TEXT")
    private String targetMuscles; // 타겟 근육
    
    @Column(columnDefinition = "TEXT")
    private String expectedResults; // 기대 효과
    
    @Column(columnDefinition = "TEXT")
    private String workouts; // 요일별 운동 프로그램 (JSON 형태)
    
    @Column(length = 50)
    @Builder.Default
    private String recommendationMethod = "AI"; // 추천 방법
    
    /**
     * DTO에서 엔티티로 변환하는 정적 메서드
     */
    public static AIWorkoutRecommendation toEntity(WorkoutRecommendationResponseDto dto, User user, InbodyRecord inbodyRecord, String goal) {
        // workouts Map을 JSON 문자열로 변환
        String workoutsJson = null;
        if (dto.workouts() != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                workoutsJson = objectMapper.writeValueAsString(dto.workouts());
            } catch (Exception e) {
                // JSON 변환 실패 시 null로 설정
                workoutsJson = null;
            }
        }
        
        return AIWorkoutRecommendation.builder()
                .user(user)
                .inbodyRecord(inbodyRecord)
                .goal(goal)
                .programName(dto.programName())
                .weeklySchedule(dto.weeklySchedule())
                .caution(dto.caution())
                .warmup(dto.warmup())
                .mainSets(dto.mainSets())
                .cooldown(dto.cooldown())
                .equipment(dto.equipment())
                .targetMuscles(dto.targetMuscles())
                .expectedResults(dto.expectedResults())
                .workouts(workoutsJson)
                .recommendationMethod("AI")
                .build();
    }
    
    /**
     * 엔티티에서 DTO로 변환하는 메서드
     */
    public WorkoutRecommendationResponseDto toDto() {
        // workouts JSON 문자열을 Map으로 파싱
        Map<String, Object> parsedWorkouts = null;
        if (this.workouts != null && !this.workouts.trim().isEmpty()) {
            try {
                // ObjectMapper를 사용하여 JSON 문자열을 Map으로 파싱
                ObjectMapper objectMapper = new ObjectMapper();
                parsedWorkouts = objectMapper.readValue(this.workouts, new TypeReference<Map<String, Object>>() {});
                System.out.println("✅ workouts JSON 파싱 성공 - ID: " + this.getId());
                System.out.println("✅ 파싱된 workouts 키: " + parsedWorkouts.keySet());
            } catch (Exception e) {
                // JSON 파싱 실패 시 빈 맵 반환
                parsedWorkouts = new java.util.HashMap<>();
                System.err.println("❌ workouts JSON 파싱 실패 - ID: " + this.getId());
                System.err.println("❌ 에러: " + e.getMessage());
                System.err.println("❌ workouts 데이터: " + this.workouts);
            }
        } else {
            // workouts가 null이거나 비어있는 경우 빈 맵 반환
            parsedWorkouts = new java.util.HashMap<>();
            System.err.println("⚠️ workouts가 null이거나 비어있음 - ID: " + this.getId());
        }
        
        return new WorkoutRecommendationResponseDto(
                this.programName,
                this.weeklySchedule,
                this.caution,
                this.warmup,
                this.mainSets,
                this.cooldown,
                this.equipment,
                this.targetMuscles,
                this.expectedResults,
                parsedWorkouts
        );
    }
    
    
}
