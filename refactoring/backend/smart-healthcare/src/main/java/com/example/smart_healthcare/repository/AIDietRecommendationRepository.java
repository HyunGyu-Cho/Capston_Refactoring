package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.AIDietRecommendation;
import com.example.smart_healthcare.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * AI 식단 추천 Repository
 */
@Repository
public interface AIDietRecommendationRepository extends JpaRepository<AIDietRecommendation, Long> {
    
    /**
     * 사용자별 식단 추천 히스토리 조회 (페이징)
     */
    @Query("SELECT d FROM AIDietRecommendation d WHERE d.user = :user AND d.isDeleted = false ORDER BY d.createdAt DESC")
    Page<AIDietRecommendation> findHistoryByUser(@Param("user") User user, Pageable pageable);

    //논리삭제
    @Query("UPDATE AIDietRecommendation d SET d.isDeleted = true WHERE d.id = :id AND d.user.id = :userId")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 삭제되지 않은 식단 추천 조회 (관리자용)
     */
    @Query("SELECT d FROM AIDietRecommendation d WHERE d.isDeleted = false")
    Page<AIDietRecommendation> findByIsDeletedFalse(Pageable pageable);
}
