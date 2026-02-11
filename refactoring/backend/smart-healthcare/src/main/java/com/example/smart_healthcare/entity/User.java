package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 100) // 소셜 로그인시 비밀번호가 null일 수 있음
    private String password;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private Role role;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

   
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private AuthProvider provider; // 로그인 제공자
 
    
    public enum Role { USER, MANAGER, ADMIN }
    
   
    public enum AuthProvider { 
        LOCAL,      // 이메일/비밀번호 회원가입
        GOOGLE,     // 구글 소셜 로그인
        KAKAO,      // 카카오 소셜 로그인 
        NAVER       // 네이버 소셜 로그인
    }
    
    /**
     * 로컬 회원가입용 팩토리 메서드
     */
    public static User createLocalUser(String email, String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();
    }
    
    /**
     * 소셜 로그인용 팩토리 메서드
     */
    public static User createSocialUser(String email, AuthProvider provider) {
        return User.builder()
                .email(email)
                .provider(provider)
                .role(Role.USER)
                .build();
    }
    
}