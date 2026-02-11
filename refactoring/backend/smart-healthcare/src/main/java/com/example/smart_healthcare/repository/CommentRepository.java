package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // ===== 1. 댓글 조회 (페이징) =====
    
    /**
     * 게시글별 댓글 조회 (삭제되지 않은 댓글만, 생성일순 정렬)
     * 
     * 프론트엔드에서 페이징을 통해 댓글/대댓글을 구분하여 표시할 수 있습니다.
     * - parentId가 null인 댓글: 최상위 댓글
     * - parentId가 있는 댓글: 대댓글
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.isDeleted = false ORDER BY c.createdAt ASC")
    Page<Comment> findByPostIdAndNotDeleted(@Param("postId") Long postId, Pageable pageable);
    
} 
