package com.example.smart_healthcare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    
    @Column(nullable = false, length = 255)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private NotificationType type;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // 관련 엔티티 ID (게시글 ID, 댓글 ID 등)
    @Column(name = "related_post_id")
    private Long relatedPostId;
    
    @Column(name = "related_comment_id")
    private Long relatedCommentId;
    
    @Column(name = "related_user_id")
    private Long relatedUserId;
    
    @Column(name = "action_url")
    private String actionUrl;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
    
    public enum NotificationType {
        COMMENT("댓글"),
        REPLY("답글"),
        LIKE("좋아요"),
        DISLIKE("싫어요"),
        MENTION("멘션"),
        SYSTEM("시스템"),
        REPORT("신고");
        
        private final String displayName;
        
        NotificationType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 알림 읽음 처리
    public void markAsRead() {
        this.isRead = true;
    }
    
    // 알림 생성 팩토리 메서드들
    public static Notification commentNotification(User recipient, User commenter, Long postId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(commenter.getEmail() + "님이 회원님의 게시글에 댓글을 남겼습니다.");
        notification.setType(NotificationType.COMMENT);
        notification.setRelatedPostId(postId);
        notification.setRelatedUserId(commenter.getId());
        notification.setActionUrl("/community/" + postId);
        return notification;
    }
    
    public static Notification likeNotification(User recipient, User liker, Long postId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(liker.getEmail() + "님이 회원님의 게시글을 좋아합니다.");
        notification.setType(NotificationType.LIKE);
        notification.setRelatedPostId(postId);
        notification.setRelatedUserId(liker.getId());
        notification.setActionUrl("/community/" + postId);
        return notification;
    }
    
    public static Notification mentionNotification(User recipient, User mentioner, Long postId, Long commentId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(mentioner.getEmail() + "님이 댓글에서 회원님을 멘션했습니다.");
        notification.setType(NotificationType.MENTION);
        notification.setRelatedPostId(postId);
        notification.setRelatedCommentId(commentId);
        notification.setRelatedUserId(mentioner.getId());
        notification.setActionUrl("/community/" + postId);
        return notification;
    }
}