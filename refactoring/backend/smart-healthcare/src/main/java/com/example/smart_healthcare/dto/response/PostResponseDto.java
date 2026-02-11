package com.example.smart_healthcare.dto.response;

import com.example.smart_healthcare.entity.CommunityPost;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 커뮤니티 게시글 응답 DTO
 * - 서버에서 클라이언트로 전송되는 데이터
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {

    private Long id;
    private String title;
    private String content;
    private CommunityPost.PostCategory category;
    private Set<String> tags;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isEdited;
    private Integer viewCount;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer commentCount;

    /**
     * CommunityPost 엔티티를 PostResponseDto로 변환하는 팩토리 메서드
     * @param post CommunityPost 엔티티
     * @return PostResponseDto
     */
    public static PostResponseDto toDto(CommunityPost post) {
        return new PostResponseDto(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getCategory(),
            post.getTags(),
            post.getAuthor().getId(),
            post.getAuthor().getEmail(),
            post.getCreatedAt(),
            post.getUpdatedAt(),
            post.getIsEdited(),
            post.getViewCount(),
            post.getLikeCount(),
            post.getDislikeCount(),
            post.getComments() != null ? post.getComments().size() : 0
        );
    }

}
