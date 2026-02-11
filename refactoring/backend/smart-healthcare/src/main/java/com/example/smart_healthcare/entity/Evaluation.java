package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.common.entity.BaseEntity;
import com.example.smart_healthcare.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "evaluation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation extends BaseEntity {
    
    @Column(nullable = false)
    private Integer rating; 
    
    @Column(columnDefinition = "TEXT")
    private String comment; 
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user; 
}
