package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    
    // TODO: 개발 마지막 단계에서 구현 예정
    // 이메일과 인증 제공자로 사용자 찾기
    // Optional<User> findByEmailAndProvider(String email, User.AuthProvider provider);
    
    // 역할별 사용자 목록
    List<User> findByRole(User.Role role);
    
    // 인증 제공자별 사용자 목록
    // List<User> findByProvider(User.AuthProvider provider);
    
    
    // 이메일 존재 여부 확인 (제거됨 - findByEmail로 대체)
    boolean existsByEmail(String email);
    
    // 이메일과 인증 제공자 조합 존재 여부 확인
    // boolean existsByEmailAndProvider(String email, User.AuthProvider provider);
    
    // ===== 관리자 기능용 메서드 =====
    
    // 역할별 사용자 수 카운트
    long countByRole(User.Role role);
    
    // 활성 사용자 수 카운트 
    long countByIsDeletedFalse();
    
    // 특정 시점 이후 가입한 사용자 수
    long countByCreatedAtAfter(LocalDateTime date);
    
    // 특정 기간 내 가입한 사용자 수
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // 검색 기능이 있는 페이징 (이메일로 검색)
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    
    // 삭제되지 않은 사용자만 조회 (페이징)
    Page<User> findByIsDeletedFalse(Pageable pageable);
    
    // 삭제되지 않은 사용자 중 이메일로 검색 (페이징)
    Page<User> findByIsDeletedFalseAndEmailContainingIgnoreCase(String email, Pageable pageable);
    
    // 사용자 통계 쿼리 (기존 - 문자열 타입)
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRoleString(@Param("role") User.Role role);
    
    // @Query("SELECT COUNT(u) FROM User u WHERE u.provider = :provider")
    // long countByProvider(@Param("provider") User.AuthProvider provider);
    
    // 최신 가입 사용자 조회 (로그용)
    List<User> findTop10ByOrderByCreatedAtDesc();
}