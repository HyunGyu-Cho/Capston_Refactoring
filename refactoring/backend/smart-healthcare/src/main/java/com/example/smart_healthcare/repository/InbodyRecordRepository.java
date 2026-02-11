package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.InbodyRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface InbodyRecordRepository extends JpaRepository<InbodyRecord, Long> {

    // ===== 기본 조회 메서드들 =====
    
    /**
     * 사용자별 인바디 기록 조회 (페이징)
     */
    @Query("SELECT i FROM InbodyRecord i WHERE i.user.id = :userId AND i.isDeleted = false")
    Page<InbodyRecord> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 사용자별 기간별 인바디 기록 조회 (페이징)
     */
    @Query("SELECT i FROM InbodyRecord i WHERE i.user.id = :userId AND i.createdAt BETWEEN :startDate AND :endDate AND i.isDeleted = false")
    Page<InbodyRecord> findByUserIdAndPeriod(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    
    
    // ===== 관리자 기능용 메서드 =====
    
    /**
     * 특정 기간 내 생성된 기록 수 (AdminService에서 사용)
     */
    @Query("SELECT COUNT(i) FROM InbodyRecord i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    long countByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 삭제되지 않은 인바디 기록 조회 (관리자용)
     */
    @Query("SELECT i FROM InbodyRecord i WHERE i.isDeleted = false")
    Page<InbodyRecord> findByIsDeletedFalse(Pageable pageable);
    
    // ===== 논리 삭제 기능 =====
    
    /**
     * 특정 인바디 기록 논리 삭제
     */
    @Modifying
    @Query("UPDATE InbodyRecord i SET i.isDeleted = true WHERE i.id = :id AND i.user.id = :userId")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}