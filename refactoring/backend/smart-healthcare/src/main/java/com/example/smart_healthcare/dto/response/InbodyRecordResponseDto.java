package com.example.smart_healthcare.dto.response;

import com.example.smart_healthcare.entity.InbodyRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 인바디 기록 응답 DTO
 * - 서버에서 클라이언트로 전송되는 데이터
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InbodyRecordResponseDto {
    
    private Long id;
    private Long userId;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private InbodyRecord.Gender gender;
    private Integer birthYear;
    private Float weight;
    private Float totalBodyWater;
    private Float protein;
    private Float mineral;
    private Float bodyFatMass;
    private Float muscleMass;
    private Float fatFreeMass;
    private Float skeletalMuscleMass;
    private Float bodyFatPercentage;
    private Float rightArmMuscleMass;
    private Float leftArmMuscleMass;
    private Float trunkMuscleMass;
    private Float rightLegMuscleMass;
    private Float leftLegMuscleMass;
    private Float rightArmFatMass;
    private Float leftArmFatMass;
    private Float trunkFatMass;
    private Float rightLegFatMass;
    private Float leftLegFatMass;
    private Integer inbodyScore;
    private Float idealWeight;
    private Float weightControl;
    private Float fatControl;
    private Float muscleControl;
    private Integer basalMetabolism;
    private Float abdominalFatPercentage;
    private Float visceralFatLevel;
    private Float obesityDegree;
    private Float bmi;
    private Float boneMineralContent;
    private Float waistCircumference;
    
    /**
     * InbodyRecord 엔티티를 InbodyRecordResponseDto로 변환하는 팩토리 메서드
     * @param record InbodyRecord 엔티티
     * @return InbodyRecordResponseDto
     */
    public static InbodyRecordResponseDto toDto(InbodyRecord record) {
        return new InbodyRecordResponseDto(
            record.getId(),
            record.getUser().getId(),
            record.getCreatedAt(),
            record.getIsDeleted(),
            record.getGender(),
            record.getBirthYear(),
            record.getWeight(),
            record.getTotalBodyWater(),
            record.getProtein(),
            record.getMineral(),
            record.getBodyFatMass(),
            record.getMuscleMass(),
            record.getFatFreeMass(),
            record.getSkeletalMuscleMass(),
            record.getBodyFatPercentage(),
            record.getRightArmMuscleMass(),
            record.getLeftArmMuscleMass(),
            record.getTrunkMuscleMass(),
            record.getRightLegMuscleMass(),
            record.getLeftLegMuscleMass(),
            record.getRightArmFatMass(),
            record.getLeftArmFatMass(),
            record.getTrunkFatMass(),
            record.getRightLegFatMass(),
            record.getLeftLegFatMass(),
            record.getInbodyScore(),
            record.getIdealWeight(),
            record.getWeightControl(),
            record.getFatControl(),
            record.getMuscleControl(),
            record.getBasalMetabolism(),
            record.getAbdominalFatPercentage(),
            record.getVisceralFatLevel(),
            record.getObesityDegree(),
            record.getBmi(),
            record.getBoneMineralContent(),
            record.getWaistCircumference()
        );
    }
}
