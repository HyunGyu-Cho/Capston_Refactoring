package com.healthcare.member.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 255)
    private String fitnessGoal;

    protected Member() {
    }

    public Member(String email, String passwordHash, UserStatus status, String nickname, String fitnessGoal) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.nickname = nickname;
        this.fitnessGoal = fitnessGoal;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getNickname() {
        return nickname;
    }

    public String getFitnessGoal() {
        return fitnessGoal;
    }
}
