package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.CommunityPost;
import com.example.smart_healthcare.entity.PostReaction;
import com.example.smart_healthcare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    
    // 특정 게시글의 특정 사용자 반응 조회 (논리삭제 제외)
    Optional<PostReaction> findByPostIdAndUserIdAndIsDeletedFalse(Long postId, Long userId);
    
    // 특정 게시글과 사용자로 반응 조회 (Entity 객체 사용, 논리삭제 제외)
    Optional<PostReaction> findByPostAndUserAndIsDeletedFalse(CommunityPost post, User user);
    
    // 특정 게시글의 특정 유형 반응 개수 (논리삭제 제외)
    long countByPostIdAndTypeAndIsDeletedFalse(Long postId, PostReaction.ReactionType type);
    
    // 특정 게시글의 모든 반응 조회 (논리삭제 제외)
    List<PostReaction> findByPostIdAndIsDeletedFalse(Long postId);
    
    // 특정 사용자의 모든 반응 조회 (논리삭제 제외)
    List<PostReaction> findByUserIdAndIsDeletedFalse(Long userId);
    
    // 특정 게시글의 추천/비추천 통계 (논리삭제 제외)
    @Query("SELECT pr.type, COUNT(pr) FROM PostReaction pr WHERE pr.post.id = :postId AND pr.isDeleted = false GROUP BY pr.type")
    List<Object[]> getReactionStatsByPostId(@Param("postId") Long postId);
    
    // 사용자가 특정 게시글에 반응했는지 확인 (논리삭제 제외)
    boolean existsByPostIdAndUserIdAndIsDeletedFalse(Long postId, Long userId);
    
    // 게시글 삭제 시 관련 반응 논리삭제
    @Modifying
    @Query("UPDATE PostReaction pr SET pr.isDeleted = true WHERE pr.post.id = :postId AND pr.isDeleted = false")
    void softDeleteByPostId(@Param("postId") Long postId);
    
    // 사용자 삭제 시 관련 반응 논리삭제
    @Modifying
    @Query("UPDATE PostReaction pr SET pr.isDeleted = true WHERE pr.user.id = :userId AND pr.isDeleted = false")
    void softDeleteByUserId(@Param("userId") Long userId);
}