package com.example.smart_healthcare.config;

import com.example.smart_healthcare.entity.*;
import com.example.smart_healthcare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * 애플리케이션 시작 시 샘플 데이터 자동 생성
 * 4가지 체형 타입의 사용자와 인바디 데이터를 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final InbodyRecordRepository inbodyRecordRepository;
    private final SurveyRepository surveyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("🚀 샘플 데이터 초기화 시작");
        
        try {
            // 각 사용자를 개별적으로 체크하고 생성
            createUserIfNotExists("underweight@sample.com", "근육이 없는 저체중 사용자", this::createUnderweightUser);
            createUserIfNotExists("abdominal@sample.com", "근육이 없는 복부비만 사용자", this::createAbdominalObesityUser);
            createUserIfNotExists("muscular@sample.com", "근육이 많은 비만 사용자", this::createMuscularOverweightUser);
            createUserIfNotExists("normal@sample.com", "평균체중의 평균근육량 사용자", this::createNormalUser);
            
            log.info("✅ 샘플 데이터 초기화 완료");
            
        } catch (Exception e) {
            log.error("❌ 샘플 데이터 초기화 실패", e);
        }
    }

    /**
     * 사용자가 존재하지 않을 때만 생성하는 헬퍼 메서드
     */
    private void createUserIfNotExists(String email, String userType, Runnable createUserMethod) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("ℹ️ {} ({})가 이미 존재합니다. 건너뜁니다.", userType, email);
        } else {
            log.info("👤 {} 생성 중...", userType);
            createUserMethod.run();
        }
    }

    /**
     * 1. 근육이 없는 저체중 사용자 생성
     * - 체중: 45kg, BMI: 17.5 (저체중)
     * - 근육량: 18kg (낮음)
     * - 체지방률: 8% (매우 낮음)
     */
    private void createUnderweightUser() {
        log.info("👤 근육이 없는 저체중 사용자 생성");
        
        // 사용자 생성
        User user = User.builder()
                .email("underweight@sample.com")
                .password(passwordEncoder.encode("sample123"))
                .role(User.Role.USER)
                .provider(User.AuthProvider.LOCAL)
                .build();
        user = userRepository.save(user);
        
        // 인바디 데이터 생성
        InbodyRecord inbody = InbodyRecord.builder()
                .user(user)
                .gender(InbodyRecord.Gender.FEMALE)
                .birthYear(1995)
                .weight(45.0f)
                .bmi(17.5f)
                .muscleMass(18.0f)
                .skeletalMuscleMass(16.5f)
                .bodyFatMass(3.6f)
                .bodyFatPercentage(8.0f)
                .totalBodyWater(25.0f)
                .protein(8.5f)  
                .mineral(2.8f)
                .fatFreeMass(41.4f)
                .rightArmMuscleMass(2.1f)
                .leftArmMuscleMass(2.0f)
                .trunkMuscleMass(7.5f)
                .rightLegMuscleMass(4.2f)
                .leftLegMuscleMass(4.1f)
                .rightArmFatMass(1.2f)
                .leftArmFatMass(1.1f)
                .trunkFatMass(2.5f)
                .rightLegFatMass(1.8f)
                .leftLegFatMass(1.7f)
                .inbodyScore(75)
                .idealWeight(55.0f)
                .weightControl(10.0f) // 체중 증가 필요
                .fatControl(2.0f) // 지방 증가 필요
                .muscleControl(5.0f) // 근육 증가 필요
                .basalMetabolism(1200)
                .abdominalFatPercentage(6.0f)
                .visceralFatLevel(3.0f)
                .obesityDegree(85.0f)
                .boneMineralContent(2.1f)
                .waistCircumference(65.0f)
                .build();
        inbodyRecordRepository.save(inbody);
        
        // 설문 데이터 생성
        Survey survey = Survey.builder()
                .user(user)
                .inbody(inbody)
                .answerText("근육량 증가와 체중 증가를 목표로 합니다. 주 4회 운동 가능하며, 고단백 식단을 선호합니다.")
                .build();
        surveyRepository.save(survey);
        
        log.info("✅ 저체중 사용자 생성 완료: userId={}", user.getId());
    }

    /**
     * 2. 근육이 없는 복부비만 사용자 생성
     * - 체중: 70kg, BMI: 24.2 (정상체중)
     * - 근육량: 25kg (낮음)
     * - 체지방률: 25% (높음), 복부지방률: 30%
     */
    private void createAbdominalObesityUser() {
        log.info("👤 근육이 없는 복부비만 사용자 생성");
        
        User user = User.builder()
                .email("abdominal@sample.com")
                .password(passwordEncoder.encode("sample123"))
                .role(User.Role.USER)
                .provider(User.AuthProvider.LOCAL)
                .build();
        user = userRepository.save(user);
        
        InbodyRecord inbody = InbodyRecord.builder()
                .user(user)
                .gender(InbodyRecord.Gender.MALE)
                .birthYear(1988)
                .weight(70.0f)
                .bmi(24.2f)
                .muscleMass(25.0f)
                .skeletalMuscleMass(22.0f)
                .bodyFatMass(17.5f)
                .bodyFatPercentage(25.0f)
                .totalBodyWater(35.0f)
                .protein(11.0f)
                .mineral(3.5f)
                .fatFreeMass(52.5f)
                .rightArmMuscleMass(2.8f)
                .leftArmMuscleMass(2.7f)
                .trunkMuscleMass(10.5f)
                .rightLegMuscleMass(5.8f)
                .leftLegMuscleMass(5.7f)
                .rightArmFatMass(1.5f)
                .leftArmFatMass(1.4f)
                .trunkFatMass(12.0f) // 복부 지방 집중
                .rightLegFatMass(2.5f)
                .leftLegFatMass(2.4f)
                .inbodyScore(65)
                .idealWeight(65.0f)
                .weightControl(5.0f) // 체중 감소 필요
                .fatControl(-8.0f) // 지방 감소 필요
                .muscleControl(8.0f) // 근육 증가 필요
                .basalMetabolism(1500)
                .abdominalFatPercentage(30.0f) // 복부지방률 높음
                .visceralFatLevel(12.0f) // 내장지방 높음
                .obesityDegree(110.0f)
                .boneMineralContent(3.2f)
                .waistCircumference(95.0f) // 허리둘레 큰 편
                .build();
        inbodyRecordRepository.save(inbody);
        
        Survey survey = Survey.builder()
                .user(user)
                .inbody(inbody)
                .answerText("복부 지방 감소와 근육량 증가를 목표로 합니다. 주 5회 운동 가능하며, 저탄고지 식단을 선호합니다.")
                .build();
        surveyRepository.save(survey);
        
        log.info("✅ 복부비만 사용자 생성 완료: userId={}", user.getId());
    }

    /**
     * 3. 근육이 많은 비만 사용자 생성
     * - 체중: 95kg, BMI: 30.8 (비만)
     * - 근육량: 45kg (매우 높음)
     * - 체지방률: 20% (정상 범위)
     */
    private void createMuscularOverweightUser() {
        log.info("👤 근육이 많은 비만 사용자 생성");
        
        User user = User.builder()
                .email("muscular@sample.com")
                .password(passwordEncoder.encode("sample123"))
                .role(User.Role.USER)
                .provider(User.AuthProvider.LOCAL)
                .build();
        user = userRepository.save(user);
        
        InbodyRecord inbody = InbodyRecord.builder()
                .user(user)
                .gender(InbodyRecord.Gender.MALE)
                .birthYear(1990)
                .weight(95.0f)
                .bmi(30.8f)
                .muscleMass(45.0f)
                .skeletalMuscleMass(40.0f)
                .bodyFatMass(19.0f)
                .bodyFatPercentage(20.0f)
                .totalBodyWater(50.0f)
                .protein(20.0f)
                .mineral(6.0f)
                .fatFreeMass(76.0f)
                .rightArmMuscleMass(5.5f)
                .leftArmMuscleMass(5.4f)
                .trunkMuscleMass(18.0f)
                .rightLegMuscleMass(10.5f)
                .leftLegMuscleMass(10.4f)
                .rightArmFatMass(1.2f)
                .leftArmFatMass(1.1f)
                .trunkFatMass(8.5f)
                .rightLegFatMass(4.1f)
                .leftLegFatMass(4.1f)
                .inbodyScore(85)
                .idealWeight(70.0f)
                .weightControl(-25.0f) // 체중 감소 필요
                .fatControl(-5.0f) // 지방 감소 필요
                .muscleControl(0.0f) // 근육 유지
                .basalMetabolism(2200)
                .abdominalFatPercentage(18.0f)
                .visceralFatLevel(8.0f)
                .obesityDegree(135.0f)
                .boneMineralContent(4.5f)
                .waistCircumference(88.0f)
                .build();
        inbodyRecordRepository.save(inbody);
        
        Survey survey = Survey.builder()
                .user(user)
                .inbody(inbody)
                .answerText("체중 감소와 근육 유지를 목표로 합니다. 주 6회 운동 가능하며, 고단백 저칼로리 식단을 선호합니다.")
                .build();
        surveyRepository.save(survey);
        
        log.info("✅ 근육이 많은 비만 사용자 생성 완료: userId={}", user.getId());
    }

    /**
     * 4. 평균체중의 평균근육량 사용자 생성
     * - 체중: 65kg, BMI: 22.5 (정상체중)
     * - 근육량: 32kg (평균)
     * - 체지방률: 18% (정상)
     */
    private void createNormalUser() {
        log.info("👤 평균체중의 평균근육량 사용자 생성");
        
        User user = User.builder()
                .email("normal@sample.com")
                .password(passwordEncoder.encode("sample123"))
                .role(User.Role.USER)
                .provider(User.AuthProvider.LOCAL)
                .build();
        user = userRepository.save(user);
        
        InbodyRecord inbody = InbodyRecord.builder()
                .user(user)
                .gender(InbodyRecord.Gender.FEMALE)
                .birthYear(1992)
                .weight(65.0f)
                .bmi(22.5f)
                .muscleMass(32.0f)
                .skeletalMuscleMass(28.5f)
                .bodyFatMass(11.7f)
                .bodyFatPercentage(18.0f)
                .totalBodyWater(42.0f)
                .protein(14.5f)
                .mineral(4.2f)
                .fatFreeMass(53.3f)
                .rightArmMuscleMass(3.5f)
                .leftArmMuscleMass(3.4f)
                .trunkMuscleMass(12.5f)
                .rightLegMuscleMass(7.8f)
                .leftLegMuscleMass(7.7f)
                .rightArmFatMass(1.3f)
                .leftArmFatMass(1.2f)
                .trunkFatMass(5.5f)
                .rightLegFatMass(2.8f)
                .leftLegFatMass(2.7f)
                .inbodyScore(90)
                .idealWeight(60.0f)
                .weightControl(5.0f) // 체중 감소 필요
                .fatControl(-2.0f) // 지방 감소 필요
                .muscleControl(2.0f) // 근육 약간 증가
                .basalMetabolism(1650)
                .abdominalFatPercentage(20.0f)
                .visceralFatLevel(6.0f)
                .obesityDegree(108.0f)
                .boneMineralContent(3.8f)
                .waistCircumference(75.0f)
                .build();
        inbodyRecordRepository.save(inbody);
        
        Survey survey = Survey.builder()
                .user(user)
                .inbody(inbody)
                .answerText("체중 감소와 근육량 유지를 목표로 합니다. 주 3회 운동 가능하며, 균형잡힌 식단을 선호합니다.")
                .build();
        surveyRepository.save(survey);
        
        log.info("✅ 평균 사용자 생성 완료: userId={}", user.getId());
    }
}
