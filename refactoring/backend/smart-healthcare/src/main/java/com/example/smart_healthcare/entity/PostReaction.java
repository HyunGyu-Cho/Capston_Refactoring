package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_reaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostReaction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private ReactionType type; // LIKE, DISLIKE
    
   
    @Column(name = "user_post_key", unique = true, nullable = false)
    private String userPostKey; // user_id + "_" + post_id
    
    @PrePersist
    @PreUpdate
    protected void onCreate() {
        if (userPostKey == null) {
            userPostKey = user.getId() + "_" + post.getId();
        }
    }
    
    public enum ReactionType {
        LIKE, DISLIKE
    }
}
