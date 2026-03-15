package com.example.smart_healthcare.member.domain;

import com.example.smart_healthcare.member.domain.enums.AuthProvider;
import com.example.smart_healthcare.member.domain.enums.Gender;
import com.example.smart_healthcare.member.domain.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_email", columnNames = "email")
})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender; // MALE, FEMALE

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status; // ACTIVE, INACTIVE, DELETED

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider; // LOCAL, GOOGLE, Naver, Kakao

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Member(String email, String passwordHash, String nickname, Gender gender, LocalDate birthDate) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
        this.status = MemberStatus.ACTIVE;
        this.authProvider = AuthProvider.LOCAL;
    }

    public static Member create(String email, String passwordHash, String nickname, Gender gender, LocalDate birthDate) {
        return new Member(email, passwordHash, nickname, gender, birthDate);
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
