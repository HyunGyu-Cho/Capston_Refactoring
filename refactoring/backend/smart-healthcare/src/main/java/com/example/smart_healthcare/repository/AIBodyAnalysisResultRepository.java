package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.AIBodyAnalysisResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AI 체형 분석 결과 Repository
 * 필요한 기능만 유지: 페이징, 기간별 조회, 통계, 논리삭제
 */
@Repository
public interface AIBodyAnalysisResultRepository extends JpaRepository<AIBodyAnalysisResult, Long> {
    
    /**
     * 사용자별 최신 분석 결과 조회
     */
    @Query("SELECT a FROM AIBodyAnalysisResult a WHERE a.user.id = :userId AND a.isDeleted = false ORDER BY a.createdAt DESC")
    Optional<AIBodyAnalysisResult> findLatestByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자별 분석 히스토리 조회 (페이징)
     */
    @Query("SELECT a FROM AIBodyAnalysisResult a WHERE a.user.id = :userId AND a.isDeleted = false ORDER BY a.createdAt DESC")
    Page<AIBodyAnalysisResult> findHistoryByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 사용자별 기간 내 분석 조회 (페이징)
     */
    @Query("SELECT a FROM AIBodyAnalysisResult a WHERE a.user.id = :userId AND a.isDeleted = false " +
           "AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AIBodyAnalysisResult> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate, 
                                                       Pageable pageable);
    
    /**
     * 사용자별 분석 수 조회
     */
    @Query("SELECT COUNT(a) FROM AIBodyAnalysisResult a WHERE a.user.id = :userId AND a.isDeleted = false")
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 기간 내 분석 수 조회
     */
    @Query("SELECT COUNT(a) FROM AIBodyAnalysisResult a WHERE a.isDeleted = false AND a.createdAt BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 체형 라벨별 통계
     */
    @Query("SELECT a.label, COUNT(a) FROM AIBodyAnalysisResult a WHERE a.isDeleted = false GROUP BY a.label ORDER BY COUNT(a) DESC")
    List<Object[]> getBodyTypeStats();
    
    /**
     * 사용자별 모든 분석 논리삭제
     */
    @Modifying
    @Query("UPDATE AIBodyAnalysisResult a SET a.isDeleted = true WHERE a.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 분석 논리삭제
     */
    @Modifying
    @Query("UPDATE AIBodyAnalysisResult a SET a.isDeleted = true WHERE a.id = :id")
    int softDeleteById(@Param("id") Long id);
    
    /**
     * 삭제되지 않은 체형 분석 결과 조회 (관리자용)
     */
    @Query("SELECT a FROM AIBodyAnalysisResult a WHERE a.isDeleted = false")
    Page<AIBodyAnalysisResult> findByIsDeletedFalse(Pageable pageable);
}
    