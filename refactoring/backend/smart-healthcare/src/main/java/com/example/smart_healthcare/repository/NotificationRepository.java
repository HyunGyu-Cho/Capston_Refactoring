package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.Notification;
import com.example.smart_healthcare.entity.Notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(Long recipientId, Boolean isRead);

    List<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(Long recipientId, NotificationType type);

    long countByRecipientIdAndIsRead(Long recipientId, Boolean isRead);

    List<Notification> findByRelatedPostId(Long postId);
    
    List<Notification> findByRelatedCommentId(Long commentId);
}
