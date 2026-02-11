package com.example.smart_healthcare.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 반응 이벤트들의 기본 추상 클래스
 */
@Getter
public abstract class ReactionEvent extends ApplicationEvent {
    private final Long postId;
    private final Long userId;
    private final String reactionType;
    
    protected ReactionEvent(Object source, Long postId, Long userId, String reactionType) {
        super(source);
        this.postId = postId;
        this.userId = userId;
        this.reactionType = reactionType;
    }
}