package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
    
    // 사용자별 히스토리 조회
    List<UserHistory> findByUserIdOrderByDateDesc(Long userId);
    
    // 사용자별 모든 히스토리 조회 (간단한 버전)
    List<UserHistory> findByUserId(Long userId);
    
    // 사용자별 특정 날짜 히스토리 조회
    List<UserHistory> findByUserIdAndDate(Long userId, LocalDate date);
    
    // 사용자별 특정 유형 히스토리 조회
    List<UserHistory> findByUserIdAndType(Long userId, String type);
    
    // 사용자별 특정 유형 히스토리 조회 (간단한 버전)
    List<UserHistory> findByUserIdAndTypeOrderByDateDesc(Long userId, String type);
    
    // 사용자별 특정 기간 히스토리 조회
    List<UserHistory> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate startDate, LocalDate endDate);
    
    // 사용자별 특정 기간 히스토리 조회 (간단한 버전)
    List<UserHistory> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    // 사용자별 특정 유형과 기간 히스토리 조회
    List<UserHistory> findByUserIdAndTypeAndDateBetweenOrderByDateDesc(Long userId, String type, LocalDate startDate, LocalDate endDate);
    
    // 사용자별 완료된 히스토리 조회
    @Query(value = "SELECT * FROM user_history uh WHERE uh.user_id = :userId AND uh.completed = true ORDER BY uh.date DESC", nativeQuery = true)
    List<UserHistory> findByUserIdAndCompletedTrueOrderByDateDesc(@Param("userId") Long userId);
    
    // 사용자별 미완료 히스토리 조회
    @Query(value = "SELECT * FROM user_history uh WHERE uh.user_id = :userId AND uh.completed = false ORDER BY uh.date DESC", nativeQuery = true)
    List<UserHistory> findByUserIdAndCompletedFalseOrderByDateDesc(@Param("userId") Long userId);
    
    // 특정 날짜의 특정 유형 히스토리 조회
    List<UserHistory> findByDateAndType(LocalDate date, String type);
    
    // 사용자별 최근 히스토리 조회
    @Query(value = "SELECT * FROM user_history uh WHERE uh.user_id = :userId ORDER BY uh.date DESC", nativeQuery = true)
    List<UserHistory> findRecentHistoryByUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);
    
    // 사용자별 통계 쿼리
    @Query(value = "SELECT COUNT(*) FROM user_history uh WHERE uh.user_id = :userId", nativeQuery = true)
    long countByUserId(@Param("userId") Long userId);
    
    @Query(value = "SELECT COUNT(*) FROM user_history uh WHERE uh.user_id = :userId AND uh.type = :type", nativeQuery = true)
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
    
    @Query(value = "SELECT COUNT(*) FROM user_history uh WHERE uh.user_id = :userId AND uh.completed = true", nativeQuery = true)
    long countCompletedByUserId(@Param("userId") Long userId);
    
    // 사용자별 연속 완료 일수 계산
    @Query(value = "SELECT COUNT(DISTINCT uh.date) FROM user_history uh WHERE uh.user_id = :userId AND uh.completed = true AND uh.date >= :startDate", nativeQuery = true)
    long countConsecutiveCompletedDaysByUserId(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}