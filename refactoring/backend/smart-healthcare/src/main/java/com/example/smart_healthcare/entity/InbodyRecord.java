package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
@Entity
@Table(name = "inbody_record")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InbodyRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;



    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private Gender gender; // MALE, FEMALE

    private Integer birthYear;                // 사용자 출생년도
    private Float weight;                     // 체중
    private Float totalBodyWater;             // 총체수분
    private Float protein;                    // 단백질
    private Float mineral;                    // 무기질
    private Float bodyFatMass;                // 체지방량
    private Float muscleMass;                 // 근육량
    private Float fatFreeMass;                // 제지방량
    private Float skeletalMuscleMass;         // 골격근량
    private Float bodyFatPercentage;          // 체지방률
    private Float rightArmMuscleMass;         // 오른팔 근육량
    private Float leftArmMuscleMass;          // 왼팔 근육량
    private Float trunkMuscleMass;            // 몸통 근육량
    private Float rightLegMuscleMass;         // 오른다리 근육량
    private Float leftLegMuscleMass;          // 왼다리 근육량
    private Float rightArmFatMass;            // 오른팔 체지방량
    private Float leftArmFatMass;             // 왼팔 체지방량
    private Float trunkFatMass;               // 몸통 체지방량
    private Float rightLegFatMass;            // 오른다리 체지방량
    private Float leftLegFatMass;             // 왼다리 체지방량
    private Integer inbodyScore;              // 인바디 점수
    private Float idealWeight;                // 표준체중
    private Float weightControl;              // 체중조절
    private Float fatControl;                 // 체지방조절
    private Float muscleControl;              // 근육조절
    private Integer basalMetabolism;          // 기초대사량
    private Float abdominalFatPercentage;     // 복부지방률
    private Float visceralFatLevel;           // 내장지방레벨
    private Float obesityDegree;              // 비만도
    private Float bmi;                        // BMI
    private Float boneMineralContent;         // 골무기질량
    private Float waistCircumference;         // 허리둘레

    public enum Gender {
        MALE, FEMALE
    }
    
    // ===== Builder를 사용한 팩토리 메서드들 =====

    /**
     * InbodyDataRequestDto에서 엔티티 생성 (AI 분석용)
     */
    public static InbodyRecord toEntity(InbodyDataRequestDto dto, User user) {
        return InbodyRecord.builder()
                .user(user)
                .gender(dto.gender() != null ? Gender.valueOf(dto.gender()) : null)
                .birthYear(dto.birthYear())
                .weight(dto.weight())
                .totalBodyWater(dto.totalBodyWater())
                .protein(dto.protein())
                .mineral(dto.mineral())
                .bodyFatMass(dto.bodyFatMass())
                .muscleMass(dto.muscleMass())
                .fatFreeMass(dto.fatFreeMass())
                .skeletalMuscleMass(dto.skeletalMuscleMass())
                .bodyFatPercentage(dto.bodyFatPercentage())
                .rightArmMuscleMass(dto.rightArmMuscleMass())
                .leftArmMuscleMass(dto.leftArmMuscleMass())
                .trunkMuscleMass(dto.trunkMuscleMass())
                .rightLegMuscleMass(dto.rightLegMuscleMass())
                .leftLegMuscleMass(dto.leftLegMuscleMass())
                .rightArmFatMass(dto.rightArmFatMass())
                .leftArmFatMass(dto.leftArmFatMass())
                .trunkFatMass(dto.trunkFatMass())
                .rightLegFatMass(dto.rightLegFatMass())
                .leftLegFatMass(dto.leftLegFatMass())
                .inbodyScore(dto.inbodyScore())
                .idealWeight(dto.idealWeight())
                .weightControl(dto.weightControl())
                .fatControl(dto.fatControl())
                .muscleControl(dto.muscleControl())
                .basalMetabolism(dto.basalMetabolism())
                .abdominalFatPercentage(dto.abdominalFatPercentage())
                .visceralFatLevel(dto.visceralFatLevel())
                .obesityDegree(dto.obesityDegree())
                .bmi(dto.bmi())
                .boneMineralContent(dto.boneMineralContent())
                .waistCircumference(dto.waistCircumference())
                .build();
    }
}