package com.example.smart_healthcare.entity;

// TODO: 신고 시스템은 향후 구현 예정
// import com.example.smart_healthcare.common.entity.BaseEntity;
// import jakarta.persistence.*;
// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;
// import java.time.LocalDateTime;
/*
@Entity
@Table(name = "post_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostReport extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;
    
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ReportType type;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;
    
    public enum ReportType {
        SPAM("스팸"),
        INAPPROPRIATE_CONTENT("부적절한 내용"),
        HARASSMENT("괴롭힘"),
        COPYRIGHT_VIOLATION("저작권 침해"),
        FAKE_INFORMATION("거짓 정보"),
        OTHER("기타");
        
        private final String displayName;
        
        ReportType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ReportStatus {
        PENDING("대기중"),
        UNDER_REVIEW("검토중"),
        RESOLVED("해결됨"),
        DISMISSED("기각됨");
        
        private final String displayName;
        
        ReportStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 신고 해결 처리
    public void resolve(ReportStatus status, User resolvedBy, String resolutionNote) {
        this.status = status;
        this.resolvedBy = resolvedBy;
        this.resolutionNote = resolutionNote;
        this.resolvedAt = LocalDateTime.now();
    }
}
*/