package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.Survey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 설문조사 Repository
 * - 사용자별 설문조사 조회 (페이징)
 * - 설문조사 삭제 기능
 */
@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    
    /**
     * 사용자별 설문조사 히스토리 조회 (페이징)
     */
    @Query("SELECT s FROM Survey s WHERE s.user.id = :userId AND s.isDeleted = false ORDER BY s.createdAt DESC")
    Page<Survey> findHistoryByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 사용자별 특정 설문조사 조회 (삭제용 보안 강화)
     */
    @Query("SELECT s FROM Survey s WHERE s.id = :id AND s.user.id = :userId AND s.isDeleted = false")
    Optional<Survey> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 사용자별 모든 설문조사 논리삭제 (사용자 탈퇴 시에 사용된다)
     */
    @Modifying
    @Query("UPDATE Survey s SET s.isDeleted = true WHERE s.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 설문조사 논리삭제
     */
    @Modifying
    @Query("UPDATE Survey s SET s.isDeleted = true WHERE s.id = :id")
    int softDeleteById(@Param("id") Long id);
    
    /**
     * 삭제되지 않은 설문조사 조회 (관리자용)
     */
    @Query("SELECT s FROM Survey s WHERE s.isDeleted = false")
    Page<Survey> findByIsDeletedFalse(Pageable pageable);
}