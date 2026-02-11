package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.common.entity.BaseEntity;
import com.example.smart_healthcare.dto.response.BodyAnalysisResponseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 기반 체형 분석 결과 엔티티
 * 새로운 AI 구조의 모든 분석 필드를 저장
 */
@Entity
@Table(name = "ai_body_analysis_result")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIBodyAnalysisResult extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BIGINT")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbody_record_id", columnDefinition = "BIGINT")
    private InbodyRecord inbodyRecord;
    
    // AI 분석 결과 필드들 (BodyAnalysisResponseDto와 일치)
    @Column(nullable = false, length = 50)
    private String label; // 체형 라벨
    
    @Column(columnDefinition = "TEXT")
    private String summary; // 한 줄 요약
    
    @Column(columnDefinition = "TEXT")
    private String reasoning; // 판단 근거
    
    @Column(columnDefinition = "TEXT")
    private String tips; // 생활/운동 팁
    
    @Column(length = 200)
    private String healthRisk; // 건강 위험도
    
    @Column(columnDefinition = "TEXT")
    private String muscleBalance; // 근육 균형 분석
    
    @Column(columnDefinition = "TEXT")
    private String metabolicHealth; // 대사 건강
    
    @Column(columnDefinition = "TEXT")
    private String bodyComposition; // 체성분 종합

    // 추가 요약 지표
    @Column(length = 50)
    private String bmiCategory; // BMI 분류

    @Column(length = 50)
    private String bodyFatCategory; // 체지방률 분류

    @Column(length = 50)
    private String visceralFatCategory; // 내장지방 분류

    @Column
    private Integer inbodyScore; // 인바디 점수
    
    @Column(length = 20)
    @Default
    private String analysisMethod = "AI"; // 분석 방법
    
    /**
     * DTO에서 엔티티로 변환하는 정적 메서드
     */
    public static AIBodyAnalysisResult toEntity(BodyAnalysisResponseDto dto, User user, InbodyRecord inbodyRecord) {
        return AIBodyAnalysisResult.builder()
                .user(user)
                .inbodyRecord(inbodyRecord)
                .label(dto.label())
                .summary(dto.summary())
                .reasoning(dto.reasoning())
                .tips(dto.tips())
                .healthRisk(dto.healthRisk())
                .muscleBalance(dto.muscleBalance())
                .metabolicHealth(dto.metabolicHealth())
                .bodyComposition(dto.bodyComposition())
                .bmiCategory(dto.bmiCategory())
                .bodyFatCategory(dto.bodyFatCategory())
                .visceralFatCategory(dto.visceralFatCategory())
                .inbodyScore(dto.inbodyScore())
                .analysisMethod("AI")
                .build();
    }
    
    /**
     * 엔티티에서 DTO로 변환하는 메서드
     */
    public BodyAnalysisResponseDto toDto() {
        return new BodyAnalysisResponseDto(
                this.getId(),
                this.label,
                this.summary,
                this.reasoning,
                this.tips,
                this.healthRisk,
                this.muscleBalance,
                this.metabolicHealth,
                this.bodyComposition,
                this.bmiCategory,
                this.bodyFatCategory,
                this.visceralFatCategory,
                this.inbodyScore,
                this.analysisMethod != null ? this.analysisMethod : "AI",
                this.getCreatedAt()
        );
    }
}
