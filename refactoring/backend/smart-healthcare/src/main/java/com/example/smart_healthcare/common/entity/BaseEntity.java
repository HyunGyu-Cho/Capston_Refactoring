package com.example.smart_healthcare.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 기본 엔티티 - 모든 엔티티가 공통으로 가져야 하는 필드들
 * - id: 기본키
 * - createdAt: 생성일시
 * - isDeleted: 논리 삭제 플래그
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted = false;
    
    // 논리 삭제 메서드
    public void delete() {
        this.isDeleted = true;
    }
    
    // 복구 메서드
    public void restore() {
        this.isDeleted = false;
    }
}