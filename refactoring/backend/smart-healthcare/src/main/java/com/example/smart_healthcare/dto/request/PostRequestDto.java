package com.example.smart_healthcare.dto.request;

import com.example.smart_healthcare.entity.CommunityPost;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * 커뮤니티 게시글 요청 DTO
 * - 클라이언트에서 서버로 전송되는 데이터
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 255, message = "제목은 1자 이상 255자 이하여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(min = 1, max = 10000, message = "내용은 1자 이상 10000자 이하여야 합니다")
    private String content;

    @NotNull(message = "카테고리는 필수입니다")
    private CommunityPost.PostCategory category;

    private Set<String> tags;

    @NotNull(message = "작성자 ID는 필수입니다")
    private Long authorId;

    /**
     * PostRequestDto를 CommunityPost 엔티티로 변환하는 메서드
     * @return CommunityPost 엔티티
     */
    public CommunityPost toEntity() {
        CommunityPost post = new CommunityPost();
        post.setTitle(this.title);
        post.setContent(this.content);
        post.setCategory(this.category);
        post.setTags(this.tags);
        return post;
    }
}
