package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.common.entity.BaseEntity;
import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "community_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPost extends BaseEntity {
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private User author;
    
   
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(length = 20)
    private PostCategory category;
    
    
    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private Set<String> tags = new HashSet<>();
    
  
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;
    
  
    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;
    
    @Column(name = "dislike_count", nullable = false)
    @Builder.Default
    private Integer dislikeCount = 0;
    
    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "is_edited", nullable = false)
    @Builder.Default
    private Boolean isEdited = false;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        if (getIsDeleted() == null) {
            setIsDeleted(false);
        }
        if (viewCount == null) {
            viewCount = 0;
        }
        if (likeCount == null) {
            likeCount = 0;
        }
        if (dislikeCount == null) {
            dislikeCount = 0;
        }
        if (commentCount == null) {
            commentCount = 0;
        }
        if (isEdited == null) {
            isEdited = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        isEdited = true;
    }
    

    public void incrementViewCount() {
        this.viewCount++;
    }
    

    public void incrementLikeCount() {
        this.likeCount++;
    }
    
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    

    public void incrementDislikeCount() {
        this.dislikeCount++;
    }
    
    public void decrementDislikeCount() {
        if (this.dislikeCount > 0) {
            this.dislikeCount--;
        }
    }
    
    public void incrementCommentCount() {
        if (commentCount == null) {
            commentCount = 0;
        }
        this.commentCount++;
    }
    
    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
    

    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            this.tags.add(tag.trim());
        }
    }
    

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }
    
    public enum PostCategory {
        EXERCISE("운동"),
        DIET("식단"),
        QUESTION("질문"),
        FREE("자유게시판"),
        TIP("팁"),
        REVIEW("후기"),
        SUCCESS_STORY("성공후기");
       
        
        private final String categoryName;
        
        PostCategory(String categoryName) {
            this.categoryName = categoryName;
        }
        
        public String getCategoryName() {
            return categoryName;
        }
    }
    
    // ===== Builder를 사용한 팩토리 메서드들 =====
    
    /**
     * 새 게시글 생성
     */
    public static CommunityPost createPost(String title, String content, User author, PostCategory category, Set<String> tags) {
        return CommunityPost.builder()
                .title(title)
                .content(content)
                .author(author)
                .category(category)
                .tags(tags != null ? tags : new HashSet<>())
                .viewCount(0)
                .likeCount(0)
                .dislikeCount(0)
                .isEdited(false)
                .build();
    }
    
    /**
     * 게시글 내용 업데이트
     */
    public void updateContent(String title, String content, PostCategory category, Set<String> tags) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags != null ? tags : new HashSet<>();
        this.isEdited = true;
    }
    
    /**
     * 게시글 부분 수정 (PATCH 방식)
     * 
     * @param title 새로운 제목 (null이면 기존 값 유지)
     * @param content 새로운 내용 (null이면 기존 값 유지)
     * @param category 새로운 카테고리 (null이면 기존 값 유지)
     * @param tags 새로운 태그 (null이면 기존 값 유지)
     * @param authorId 수정 요청자 ID (권한 확인용)
     * @throws BusinessException 권한이 없거나 삭제된 게시글인 경우
     */
    public void patch(String title, String content, PostCategory category, Set<String> tags, Long authorId) {
        // 삭제 상태 확인
        if (this.getIsDeleted()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "삭제된 게시글은 수정할 수 없습니다.");
        }
        
        // 권한 확인
        if (!this.getAuthor().getId().equals(authorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "게시글을 수정할 권한이 없습니다.");
        }
        
        // 입력 데이터 검증 및 부분 업데이트
        if (title != null) {
            if (title.trim().isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "제목을 입력해주세요.");
            }
            if (title.length() > 200) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "제목은 200자를 초과할 수 없습니다.");
            }
            this.title = title.trim();
        }
        
        if (content != null) {
            if (content.trim().isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "내용을 입력해주세요.");
            }
            if (content.length() > 10000) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "내용은 10000자를 초과할 수 없습니다.");
            }
            this.content = content.trim();
        }
        
        if (category != null) {
            this.category = category;
        }
        
        if (tags != null) {
            this.tags = tags;
        }
        
        // 수정 플래그 설정
        this.isEdited = true;
    }

    
    /**
     * 좋아요/싫어요 수 업데이트
     */
    public void updateReactionCounts(int likeCount, int dislikeCount) {
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }
}
