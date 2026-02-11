package com.example.smart_healthcare.dto.response;

import com.example.smart_healthcare.entity.PostReaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReactionResponseDto {
    private Long id;
    private Long postId;
    private Long userId;
    private PostReaction.ReactionType reactionType;
    private LocalDateTime createdAt;
    
    public static ReactionResponseDto toDto(PostReaction reaction) {
        return new ReactionResponseDto(
            reaction.getId(),
            reaction.getPost().getId(),
            reaction.getUser().getId(),
            reaction.getType(),
            reaction.getCreatedAt()
        );
    }
}
