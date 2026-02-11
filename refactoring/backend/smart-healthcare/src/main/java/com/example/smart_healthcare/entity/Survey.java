package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "survey")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Survey extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "inbody_id")
    private InbodyRecord inbody;


    @Column(columnDefinition = "TEXT")
    private String answerText;
    
    // 설문조사 추가 데이터 (JSON 형태)
    @Column(columnDefinition = "TEXT")
    private String surveyData;

}
