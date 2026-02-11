package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recommendation_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(name = "recommendation_type", nullable = false)
    private RecommendationType recommendationType;

    @Column(name = "recommendation_data", columnDefinition = "TEXT")
    private String recommendationData; // JSON 형태로 추천 데이터 저장

    @Column(name = "is_applied")
    private Boolean isApplied = false; // 사용자가 추천을 적용했는지 여부

    public enum RecommendationType {
        WORKOUT,    // 운동 추천
        DIET        // 식단 추천
    }

    /**
     * 추천 기록 생성 팩토리 메서드
     */
    public static RecommendationRecord createWorkoutRecommendation(User user, String workoutData) {
        RecommendationRecord record = new RecommendationRecord();
        record.setUser(user);
        record.setRecommendationType(RecommendationType.WORKOUT);
        record.setRecommendationData(workoutData);
        return record;
    }

    /**
     * 추천 기록 생성 팩토리 메서드
     */
    public static RecommendationRecord createDietRecommendation(User user, String dietData) {
        RecommendationRecord record = new RecommendationRecord();
        record.setUser(user);
        record.setRecommendationType(RecommendationType.DIET);
        record.setRecommendationData(dietData);
        return record;
    }
}
