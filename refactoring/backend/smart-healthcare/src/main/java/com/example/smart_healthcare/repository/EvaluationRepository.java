package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.Evaluation;
import com.example.smart_healthcare.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    
    // ===== 1. 최신순으로 모든 평가 조회 (페이징) =====
    
    /**
     * 최신순으로 모든 평가 조회 (페이징)
     */
    Page<Evaluation> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // ===== 2. 내 평가 조회 =====
    
    /**
     * 특정 사용자의 평가 조회
     */
    Optional<Evaluation> findByUserId(Long userId);
    
    // ===== 3. 전체 평가 개수 =====
    
    /**
     * 전체 평가 개수 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.isDeleted = false")
    Long getTotalCount();
    
    // ===== 4. 평점 평균 계산 =====
    
    /**
     * 전체 평점 평균 계산 (삭제되지 않은 것만)
     */
    @Query("SELECT AVG(e.rating) FROM Evaluation e WHERE e.isDeleted = false")
    Double getAverageRating();
    
    // ===== 5. 특정 점수별 평가 조회 (페이징) =====
    
    /**
     * 특정 평점의 평가 조회 (페이징)
     */
    Page<Evaluation> findByRatingOrderByCreatedAtDesc(Integer rating, Pageable pageable);
    
    /**
     * 평점별 평가 개수 통계
     */
    @Query("SELECT e.rating, COUNT(e) FROM Evaluation e WHERE e.isDeleted = false GROUP BY e.rating ORDER BY e.rating")
    List<Object[]> getRatingStatistics();
}