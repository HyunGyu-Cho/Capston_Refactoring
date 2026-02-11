package com.example.smart_healthcare.dto.response;

import com.example.smart_healthcare.entity.Comment;
import com.example.smart_healthcare.entity.User;

import java.time.LocalDateTime;

/**
 * 댓글 응답 DTO
 */
public record CommentResponseDto(
    Long id,
    String content,
    Long authorId,
    String authorName,
    String authorEmail,
    Long postId,
    Long parentId,
    LocalDateTime createdAt,
    Boolean isDeleted
) {
    
    /**
     * Comment 엔티티를 CommentResponseDto로 변환
     */
    public static CommentResponseDto toDto(Comment comment) {
        User author = comment.getAuthor();
        
        return new CommentResponseDto(
            comment.getId(),
            comment.getContent(),
            author != null ? author.getId() : null,
            author != null ? author.getEmail() : null, // User 엔티티에 name 필드가 없으므로 email 사용
            author != null ? author.getEmail() : null,
            comment.getPost() != null ? comment.getPost().getId() : null,
            comment.getParentId(),
            comment.getCreatedAt(),
            comment.getIsDeleted()
        );
    }
}
