package com.example.smart_healthcare.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 반응 변경 이벤트
 */
@Getter
public class ReactionChangedEvent extends ReactionEvent {
    private final String oldReactionType;
    
    public ReactionChangedEvent(Long postId, Long userId, String oldReactionType, String newReactionType) {
        super(new Object(), postId, userId, newReactionType);
        this.oldReactionType = oldReactionType;
    }
}