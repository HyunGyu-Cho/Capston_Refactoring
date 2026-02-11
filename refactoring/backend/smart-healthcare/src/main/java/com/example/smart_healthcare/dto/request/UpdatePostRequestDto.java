package com.example.smart_healthcare.dto.request;

import com.example.smart_healthcare.entity.CommunityPost.PostCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePostRequestDto {
    
    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 255, message = "제목은 1자 이상 255자 이하여야 합니다")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다")
    @Size(min = 1, max = 10000, message = "내용은 1자 이상 10000자 이하여야 합니다")
    private String content;
    
    @NotNull(message = "카테고리는 필수입니다")
    private PostCategory category;
    
    private Set<String> tags;
    
    @NotNull(message = "작성자 ID는 필수입니다")
    private Long authorId;
}
