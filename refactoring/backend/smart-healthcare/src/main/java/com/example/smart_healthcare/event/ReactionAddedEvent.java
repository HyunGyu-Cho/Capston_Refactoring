package com.example.smart_healthcare.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 반응 추가 이벤트
 */
@Getter
public class ReactionAddedEvent extends ReactionEvent {
    public ReactionAddedEvent(Long postId, Long userId, String reactionType) {
        super(new Object(), postId, userId, reactionType);
    }
}