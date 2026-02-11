package com.example.smart_healthcare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "user_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDate date;

    private String type; // "workout" or "diet"

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload; // JSON 문자열로 저장

    private Boolean completed;
}