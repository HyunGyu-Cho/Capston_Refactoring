package com.example.smart_healthcare.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 리뷰 생성 이벤트
 */
@Getter
public class ReviewCreatedEvent extends ApplicationEvent {
    private final Long evaluationId;
    private final Long reviewPostId;
    private final Long userId;
    
    public ReviewCreatedEvent(Long evaluationId, Long reviewPostId, Long userId) {
        super(new Object());
        this.evaluationId = evaluationId;
        this.reviewPostId = reviewPostId;
        this.userId = userId;
    }
}