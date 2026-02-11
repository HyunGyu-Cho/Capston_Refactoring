package com.example.smart_healthcare.dto.request;

/**
 * AI 분석용 인바디 데이터 DTO
 * 프론트엔드에서 넘어오는 모든 인바디 필드를 포함
 */
public record InbodyDataRequestDto(
        // 기본 정보
        Long userId,                // 사용자 ID
        String gender,              // "MALE" / "FEMALE"
        Integer birthYear,          // 1999
        Float weight,               // 72.4 kg
        
        // 체수분/영양소 (프론트엔드 순서에 맞춤)
        Float totalBodyWater,       // 총체수분 L
        Float protein,              // 단백질 kg
        Float mineral,              // 무기질 kg
        
        // 핵심 체성분 데이터
        Float bodyFatMass,          // 체지방량 kg
        Float muscleMass,           // 근육량 kg
        Float fatFreeMass,          // 제지방량 kg
        Float skeletalMuscleMass,   // 골격근량 kg
        Float bmi,                  // 23.6
        Float bodyFatPercentage,    // 체지방률 %
        
        // 분절 근육량 (좌우/상하체 균형 분석용)
        Float rightArmMuscleMass,   // 오른팔 근육량
        Float leftArmMuscleMass,    // 왼팔 근육량
        Float trunkMuscleMass,      // 몸통 근육량
        Float rightLegMuscleMass,   // 오른다리 근육량
        Float leftLegMuscleMass,    // 왼다리 근육량
        
        // 분절 체지방량
        Float rightArmFatMass,      // 오른팔 체지방량
        Float leftArmFatMass,       // 왼팔 체지방량
        Float trunkFatMass,         // 몸통 체지방량
        Float rightLegFatMass,      // 오른다리 체지방량
        Float leftLegFatMass,       // 왼다리 체지방량
        
        // 건강 지표 (프론트엔드 순서에 맞춤)
        Integer inbodyScore,        // 인바디점수 (0-100)
        Float idealWeight,          // 적정체중 kg
        Float weightControl,        // 체중조절 kg
        Float fatControl,           // 지방조절 kg
        Float muscleControl,        // 근육조절 kg
        Integer basalMetabolism,    // 기초대사량 kcal
        Float abdominalFatPercentage, // 복부지방률 %
        Float visceralFatLevel,     // 내장지방레벨 (1-20)
        Float obesityDegree,        // 비만도 %
        Float boneMineralContent,   // 골무기질량 kg
        Float waistCircumference,   // 허리둘레 cm
        
        // 설문조사 데이터
        SurveyDataRequestDto survey        // 설문조사 정보
) {
    
    /**
     * 현재 연령 계산 (만 나이)
     */
    public int getCurrentAge() {
        return java.time.Year.now().getValue() - birthYear;
    }
    
    /**
     * 성별을 한국어로 반환
     */
    public String getGenderKorean() {
        return "MALE".equals(gender) ? "남성" : "여성";
    }
    
}
