package com.example.smart_healthcare.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 반응 제거 이벤트
 */
@Getter
public class ReactionRemovedEvent extends ReactionEvent {
    public ReactionRemovedEvent(Long postId, Long userId, String reactionType) {
        super(new Object(), postId, userId, reactionType);
    }
}