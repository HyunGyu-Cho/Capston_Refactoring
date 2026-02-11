package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.common.entity.BaseEntity;
import com.example.smart_healthcare.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthReport extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ReportType type;
    
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;
    
    @Column(name = "period_start")
    private LocalDate periodStart;
    
    @Column(name = "period_end")
    private LocalDate periodEnd;
    
    @Column(columnDefinition = "TEXT")
    private String summary; // 요약 내용
    
    @Column(columnDefinition = "TEXT")
    private String details; // 상세 분석 내용
    
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations; // 개선 권장사항
    
    // BaseEntity에서 createdAt, updatedAt, version, isDeleted 상속
    
    public enum ReportType {
        WEEKLY("주간 리포트"),
        MONTHLY("월간 리포트"),
        QUARTERLY("분기 리포트"),
        YEARLY("연간 리포트"),
        CUSTOM("커스텀 리포트");
        
        private final String displayName;
        
        ReportType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}