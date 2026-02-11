package com.example.smart_healthcare.dto.request;

import com.example.smart_healthcare.entity.PostReaction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddReactionRequestDto {
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    @NotNull(message = "반응 타입은 필수입니다")
    private PostReaction.ReactionType reactionType;
}
