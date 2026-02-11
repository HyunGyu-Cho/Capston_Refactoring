package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.AIWorkoutRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * AI 운동 추천 Repository
 */
@Repository
public interface AIWorkoutRecommendationRepository extends JpaRepository<AIWorkoutRecommendation, Long> {
    
    /**
     * 사용자별 운동 추천 히스토리 조회 (페이징)
     */
    @Query("SELECT w FROM AIWorkoutRecommendation w WHERE w.user.id = :userId AND w.isDeleted = false ORDER BY w.createdAt DESC")
    Page<AIWorkoutRecommendation> findHistoryByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 특정 운동 추천 논리 삭제
     */
    @Modifying
    @Query("UPDATE AIWorkoutRecommendation w SET w.isDeleted = true WHERE w.id = :id")
    int softDeleteById(@Param("id") Long id);
    
    /**
     * 삭제되지 않은 운동 추천 조회 (관리자용)
     */
    @Query("SELECT w FROM AIWorkoutRecommendation w WHERE w.isDeleted = false")
    Page<AIWorkoutRecommendation> findByIsDeletedFalse(Pageable pageable);
    
}
