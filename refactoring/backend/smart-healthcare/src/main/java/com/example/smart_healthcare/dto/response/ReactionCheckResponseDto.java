package com.example.smart_healthcare.dto.response;

import com.example.smart_healthcare.entity.PostReaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReactionCheckResponseDto {
    private boolean hasReaction;
    private PostReaction.ReactionType reactionType;
    private Long reactionId;
}
