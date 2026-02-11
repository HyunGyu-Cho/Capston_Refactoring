/**
 * 프론트엔드와 백엔드 간 데이터 변환 유틸리티
 */

/**
 * 프론트엔드 인바디 데이터를 백엔드 DTO 형식으로 변환
 */
export function mapInbodyDataToBackend(frontendData, userId) {
  const fieldMapping = {
    // 기본 정보
    "성별": "gender",
    "사용자 출생년도": "birthYear",
    "체중": "weight",
    "체질량지수": "bmi",
    
    // 체성분 데이터
    "총체수분": "totalBodyWater",
    "단백질": "protein",
    "무기질": "mineral",
    "체지방량": "bodyFatMass",
    "근육량": "muscleMass",
    "제지방량": "fatFreeMass",
    "골격근량": "skeletalMuscleMass",
    "체지방률": "bodyFatPercentage",
    
    // 분절 근육량
    "오른팔 근육량": "rightArmMuscleMass",
    "왼팔 근육량": "leftArmMuscleMass",
    "몸통 근육량": "trunkMuscleMass",
    "오른다리 근육량": "rightLegMuscleMass",
    "왼다리 근육량": "leftLegMuscleMass",
    
    // 분절 체지방량
    "오른팔 체지방량": "rightArmFatMass",
    "왼팔 체지방량": "leftArmFatMass",
    "몸통 체지방량": "trunkFatMass",
    "오른다리 체지방량": "rightLegFatMass",
    "왼다리 체지방량": "leftLegFatMass",
    
    // 기타 지표
    "인바디점수": "inbodyScore",
    "적정체중": "idealWeight",
    "체중조절": "weightControl",
    "지방조절": "fatControl",
    "근육조절": "muscleControl",
    "기초대사량": "basalMetabolism",
    "복부지방률": "abdominalFatPercentage",
    "내장지방레벨": "visceralFatLevel",
    "비만도": "obesityDegree",
    "골무기질량": "boneMineralContent",
    "복부둘레": "waistCircumference"
  };

  const backendData = {
    userId: userId
  };

  // 필드 매핑 및 변환
  Object.entries(frontendData).forEach(([frontendKey, value]) => {
    const backendKey = fieldMapping[frontendKey];
    if (backendKey && value !== undefined && value !== null) {
      // 특별한 변환 처리
      if (frontendKey === "성별") {
        // 0 -> "MALE", 1 -> "FEMALE"
        backendData[backendKey] = value === 0 ? "MALE" : "FEMALE";
      } else {
        // 숫자 필드는 숫자로 변환
        const numValue = parseFloat(value);
        backendData[backendKey] = isNaN(numValue) ? value : numValue;
      }
    }
  });

  return backendData;
}

/**
 * 프론트엔드 데이터를 InbodyDataDto 형식으로 변환 (AI API용)
 */
export function mapInbodyDataToAIDto(inbody, userId, survey = null) {
  // 안전한 숫자 변환 함수
  const safeParseFloat = (value) => {
    if (value === undefined || value === null || value === '') return null;
    const parsed = parseFloat(value);
    return isNaN(parsed) ? null : parsed;
  };
  
  const safeParseInt = (value) => {
    if (value === undefined || value === null || value === '') return null;
    const parsed = parseInt(value);
    return isNaN(parsed) ? null : parsed;
  };

  const aiData = {
    userId: userId,
    // inbody.js 순서대로 매핑
    gender: inbody["성별"] === "남성" ? "MALE" : "FEMALE",
    birthYear: safeParseInt(inbody["사용자 출생년도"]),
    weight: safeParseFloat(inbody["체중"]),
    totalBodyWater: safeParseFloat(inbody["총체수분"]),
    protein: safeParseFloat(inbody["단백질"]),
    mineral: safeParseFloat(inbody["무기질"]),
    bodyFatMass: safeParseFloat(inbody["체지방량"]),
    muscleMass: safeParseFloat(inbody["근육량"]),
    fatFreeMass: safeParseFloat(inbody["제지방량"]),
    skeletalMuscleMass: safeParseFloat(inbody["골격근량"]),
    bmi: safeParseFloat(inbody["체질량지수"]),
    bodyFatPercentage: safeParseFloat(inbody["체지방률"]),
    rightArmMuscleMass: safeParseFloat(inbody["오른팔 근육량"]),
    leftArmMuscleMass: safeParseFloat(inbody["왼팔 근육량"]),
    trunkMuscleMass: safeParseFloat(inbody["몸통 근육량"]),
    rightLegMuscleMass: safeParseFloat(inbody["오른다리 근육량"]),
    leftLegMuscleMass: safeParseFloat(inbody["왼다리 근육량"]),
    rightArmFatMass: safeParseFloat(inbody["오른팔 체지방량"]),
    leftArmFatMass: safeParseFloat(inbody["왼팔 체지방량"]),
    trunkFatMass: safeParseFloat(inbody["몸통 체지방량"]),
    rightLegFatMass: safeParseFloat(inbody["오른다리 체지방량"]),
    leftLegFatMass: safeParseFloat(inbody["왼다리 체지방량"]),
    inbodyScore: safeParseInt(inbody["인바디점수"]),
    idealWeight: safeParseFloat(inbody["적정체중"]),
    weightControl: safeParseFloat(inbody["체중조절"]),
    fatControl: safeParseFloat(inbody["지방조절"]),
    muscleControl: safeParseFloat(inbody["근육조절"]),
    basalMetabolism: safeParseInt(inbody["기초대사량"]),
    abdominalFatPercentage: safeParseFloat(inbody["복부지방률"]),
    visceralFatLevel: safeParseFloat(inbody["내장지방레벨"]),
    obesityDegree: safeParseFloat(inbody["비만도"]),
    boneMineralContent: safeParseFloat(inbody["골무기질량"]),
    waistCircumference: safeParseFloat(inbody["복부둘레"]), // 복부둘레만 사용
    survey: survey ? {
      text: survey.text || '',
      workoutFrequency: survey.workoutFrequency || '주 3회',
      selectedDays: survey.selectedDays || ['월', '수', '금'],
      // 백엔드 DTO(SurveyDataRequestDto)의 selectedDaysEn / mealsToGenerate 필드를 그대로 전달
      selectedDaysEn: survey.selectedDaysEn || null,
      preferredDays: survey.preferredDays || '월, 수, 금 (주 3회)',
      mealsPerDay: survey.mealsPerDay || '3',
      mealLabeling: survey.mealLabeling || 'generic',
      selectedMeals: survey.selectedMeals || [],
      selectedMealsLabel: survey.selectedMealsLabel || '아침, 점심, 저녁 (하루 3끼)',
      mealsToGenerate: survey.mealsToGenerate || survey.selectedMeals || ['breakfast', 'lunch', 'dinner']
    } : null // 설문조사 데이터
  };

  console.log('🔍 mapInbodyDataToAIDto 변환 결과:', aiData);
  return aiData;
}

/**
 * 백엔드 응답 데이터를 프론트엔드 형식으로 변환
 */
export function mapBackendResponseToFrontend(backendResponse) {
  // 백엔드 응답을 그대로 반환 (필요시 추가 변환)
  return backendResponse;
}

/**
 * 인바디 데이터 유효성 검사
 */
export function validateInbodyData(data) {
  console.log('🔍 validateInbodyData 호출됨, 데이터:', data);
  console.log('🔍 데이터 타입:', typeof data);
  console.log('🔍 데이터 키들:', data ? Object.keys(data) : '데이터가 null/undefined');
  
  const requiredFields = [
    "성별", "사용자 출생년도", "체중", "체질량지수", "체지방률", 
    "근육량", "골격근량", "제지방량"
  ];

  const missingFields = requiredFields.filter(field => {
    const value = data[field];
    const isMissing = (value === undefined || value === null || value === '');
    console.log(`🔍 필드 "${field}": 값="${value}", 누락여부=${isMissing}`);
    return isMissing;
  });

  if (missingFields.length > 0) {
    console.error('❌ 누락된 필수 필드:', missingFields);
    console.error('❌ 현재 데이터:', data);
    console.error('❌ 데이터 키들:', Object.keys(data || {}));
    throw new Error(`필수 필드가 누락되었습니다: ${missingFields.join(", ")}`);
  }

  console.log('✅ 모든 필수 필드가 존재합니다.');
  return true;
}
