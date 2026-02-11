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

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    private CommunityPost post;

    @Column(name = "parent_id")
    private Long parentId;
    
    // ===== Builder를 사용한 팩토리 메서드들 =====
    
    /**
     * 새 댓글 생성
     * 
     * @param content 댓글 내용
     * @param author 작성자
     * @param post 게시글
     * @return 생성된 댓글
     * @throws BusinessException 댓글 생성 규칙 위반 시
     */
    public static Comment createComment(String content, User author, CommunityPost post) {
        // 댓글 내용 검증
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "댓글 내용을 입력해주세요.");
        }
        
        if (content.length() > 1000) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "댓글은 1000자를 초과할 수 없습니다.");
        }
        
        // 작성자 검증
        if (author == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "작성자 정보가 필요합니다.");
        }
        
        // 게시글 검증
        if (post == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "게시글 정보가 필요합니다.");
        }
        
        if (post.getIsDeleted()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "삭제된 게시글에는 댓글을 작성할 수 없습니다.");
        }
        
        return Comment.builder()
                .content(content.trim())
                .author(author)
                .post(post)
                .build();
    }
    
    /**
     * 대댓글 생성
     * 
     * @param content 댓글 내용
     * @param author 작성자
     * @param post 게시글
     * @param parentId 부모 댓글 ID
     * @return 생성된 대댓글
     * @throws BusinessException 대댓글 생성 규칙 위반 시
     */
    public static Comment createReply(String content, User author, CommunityPost post, Long parentId) {
        // 댓글 내용 검증
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "댓글 내용을 입력해주세요.");
        }
        
        if (content.length() > 1000) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "댓글은 1000자를 초과할 수 없습니다.");
        }
        
        // 작성자 검증
        if (author == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "작성자 정보가 필요합니다.");
        }
        
        // 게시글 검증
        if (post == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "게시글 정보가 필요합니다.");
        }
        
        if (post.getIsDeleted()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "삭제된 게시글에는 댓글을 작성할 수 없습니다.");
        }
        
        // 부모 댓글 ID 검증
        if (parentId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "부모 댓글 ID가 필요합니다.");
        }
        
        return Comment.builder()
                .content(content.trim())
                .author(author)
                .post(post)
                .parentId(parentId)
                .build();
    }
    
    
    /**
     * 댓글 수정
     * 
     * @param newContent 새로운 댓글 내용
     * @throws BusinessException 댓글 수정 규칙 위반 시
     */
    public void updateContent(String newContent) {
        // 댓글 내용 검증
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "댓글 내용을 입력해주세요.");
        }
        
        if (newContent.length() > 1000) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "댓글은 1000자를 초과할 수 없습니다.");
        }
        
        this.content = newContent.trim();
    }
    
    /**
     * 댓글 부분 수정 (PATCH 방식)
     * 
     * @param newContent 새로운 댓글 내용 (null이면 기존 값 유지)
     * @param authorId 수정 요청자 ID (권한 확인용)
     * @throws BusinessException 권한이 없거나 삭제된 댓글인 경우
     */
    public void patch(String newContent, Long authorId) {
        // 삭제 상태 확인
        if (this.getIsDeleted()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "삭제된 댓글은 수정할 수 없습니다.");
        }
        
        // 권한 확인
        if (!this.getAuthor().getId().equals(authorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "댓글을 수정할 권한이 없습니다.");
        }
        
        // 내용이 제공된 경우에만 수정
        if (newContent != null) {
            if (newContent.trim().isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "댓글 내용을 입력해주세요.");
            }
            
            if (newContent.length() > 1000) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "댓글은 1000자를 초과할 수 없습니다.");
            }
            
            this.content = newContent.trim();
        }
    }
    
    /**
     * 댓글 삭제 가능 여부 확인
     * 
     * @param requesterId 요청자 ID
     * @return 삭제 가능 여부
     */
    public boolean canBeDeletedBy(Long requesterId) {
        if (this.getIsDeleted()) {
            return false; // 이미 삭제된 댓글
        }
        
        if (this.author == null || !this.author.getId().equals(requesterId)) {
            return false; // 작성자가 아니거나 작성자 정보가 없음
        }
        
        return true;
    }
}
