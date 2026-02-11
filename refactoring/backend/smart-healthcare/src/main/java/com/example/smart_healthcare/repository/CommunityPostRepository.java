package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.CommunityPost;
import com.example.smart_healthcare.entity.CommunityPost.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    
    // ===== 핵심 기능만 유지 =====
    
    // 논리삭제되지 않은 게시글 조회 (기본 목록)
    @Query("SELECT cp FROM CommunityPost cp WHERE cp.isDeleted = false")
    Page<CommunityPost> findByIsDeletedFalse(Pageable pageable);
    
    // 카테고리별 검색 - 논리삭제 제외 (페이징 지원)
    @Query("SELECT cp FROM CommunityPost cp WHERE cp.isDeleted = false AND cp.category = :category")
    Page<CommunityPost> findByCategory(@Param("category") PostCategory category, Pageable pageable);
    
    /**
     * 고급 검색 (페이징 지원)
     * 
     * 검색 조건:
     * - 통합 검색어 (keyword): 제목 OR 내용에 검색어가 포함된 게시글 조회
     * - 제목 검색 (title): 제목에만 검색어가 포함된 게시글 조회
     * - 내용 검색 (content): 내용에만 검색어가 포함된 게시글 조회
     * - 카테고리 필터: 특정 카테고리만 조회
     * - 작성자 필터: 특정 사용자가 작성한 게시글만 조회
     * - 날짜 범위: startDate ~ endDate 기간 내 작성된 게시글
     * 
     * 모든 조건은 선택적이며, null인 경우 해당 조건은 무시됩니다.
     * 
     * @param keyword 제목 OR 내용에서 검색할 키워드 (OR 조건)
     * @param title 제목에서만 검색할 키워드
     * @param content 내용에서만 검색할 키워드
     */
    @Query("""
           SELECT cp FROM CommunityPost cp
           WHERE cp.isDeleted = false
             AND (:keyword IS NULL OR cp.title LIKE :keyword OR cp.content LIKE :keyword)
             AND (:title IS NULL OR cp.title LIKE :title)
             AND (:content IS NULL OR cp.content LIKE :content)
             AND (:category IS NULL OR cp.category = :category)
             AND (:authorId IS NULL OR cp.author.id = :authorId)
             AND (:startDate IS NULL OR cp.createdAt >= :startDate)
             AND (:endDate IS NULL OR cp.createdAt <= :endDate)
           """)
    Page<CommunityPost> findByAdvancedSearch(
            @Param("keyword") String keyword,
            @Param("title") String title,
            @Param("content") String content,
            @Param("category") PostCategory category,
            @Param("authorId") Long authorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
}