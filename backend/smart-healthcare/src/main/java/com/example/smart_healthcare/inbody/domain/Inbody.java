package com.example.smart_healthcare.inbody.domain;

import com.example.smart_healthcare.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inbody",
        indexes = {
                @Index(name = "idx_inbody_member_measured_at_id", columnList = "member_id, measured_at DESC, inbody_id DESC")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inbody_member_measured_at", columnNames = {"member_id", "measured_at"})
        }
)
public class Inbody {

    @Id
    @Column(name = "inbody_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inbody_member"))
    private Member member;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "height_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "body_fat_mass_kg", precision = 5, scale = 2)
    private BigDecimal bodyFatMassKg;

    @Column(name = "skeletal_muscle_mass_kg", precision = 5, scale = 2)
    private BigDecimal skeletalMuscleMassKg;

    @Column(name = "body_water_l", precision = 5, scale = 2)
    private BigDecimal bodyWaterL;

    @Column(name = "waist_hip_ratio", precision = 4, scale = 2)
    private BigDecimal waistHipRatio;

    @Column(name = "visceral_fat_level")
    private Integer visceralFatLevel;

    @Column(name = "bmi", nullable = false, precision = 5, scale = 2)
    private BigDecimal bmi;

    @Column(name = "body_fat_percent", precision = 5, scale = 2)
    private BigDecimal bodyFatPercent;

    @Column(name = "age_at_measurement", nullable = false)
    private Integer ageAtMeasurement;

    @Column(name = "calculation_version", nullable = false, length = 20)
    private String calculationVersion;

    @Builder
    public Inbody(Long id,
                  Member member,
                  LocalDateTime measuredAt,
                  BigDecimal heightCm,
                  BigDecimal weightKg,
                  BigDecimal bodyFatMassKg,
                  BigDecimal skeletalMuscleMassKg,
                  BigDecimal bodyWaterL,
                  BigDecimal waistHipRatio,
                  Integer visceralFatLevel,
                  BigDecimal bmi,
                  BigDecimal bodyFatPercent,
                  Integer ageAtMeasurement,
                  String calculationVersion) {
        this.id = id;
        this.member = member;
        this.measuredAt = measuredAt;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.bodyFatMassKg = bodyFatMassKg;
        this.skeletalMuscleMassKg = skeletalMuscleMassKg;
        this.bodyWaterL = bodyWaterL;
        this.waistHipRatio = waistHipRatio;
        this.visceralFatLevel = visceralFatLevel;
        this.bmi = bmi;
        this.bodyFatPercent = bodyFatPercent;
        this.ageAtMeasurement = ageAtMeasurement;
        this.calculationVersion = calculationVersion;
    }
}
