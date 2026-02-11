// 체형별 색상 및 스타일 유틸리티 함수들

// 14개 체형 분류 정의
export const BODY_TYPES = {
  ATHLETE: '운동선수급',
  MUSCULAR: '근육형',
  OPTIMAL: '적정',
  SLIM: '날씬',
  MUSCULAR_SLIM: '근육형날씬',
  SLIGHTLY_THIN: '약간마름',
  THIN: '마름',
  OVERWEIGHT: '과체중',
  MILD_OBESE: '경도비만',
  OBESE: '비만',
  SKINNY_FAT: '마른비만',
  SEVERE_OBESE: '고도비만',
  ABDOMINAL_OBESE: '복부비만형',
  MUSCULAR_OBESE: '근육형비만'
};

// 체형별 색상 매핑
export const BODY_TYPE_COLORS = {
  [BODY_TYPES.ATHLETE]: 'text-purple-600',           // 🟣 운동선수급: 보라색
  [BODY_TYPES.MUSCULAR]: 'text-blue-600',            // 🔵 근육형: 파란색
  [BODY_TYPES.OPTIMAL]: 'text-green-600',            // 🟢 적정: 초록색
  [BODY_TYPES.SLIM]: 'text-teal-600',                // 🔷 날씬: 청록색
  [BODY_TYPES.MUSCULAR_SLIM]: 'text-cyan-600',       // 🔵 근육형날씬: 시안색
  [BODY_TYPES.SLIGHTLY_THIN]: 'text-yellow-600',     // 🟡 약간마름: 노란색
  [BODY_TYPES.THIN]: 'text-orange-600',              // 🟠 마름: 주황색
  [BODY_TYPES.OVERWEIGHT]: 'text-yellow-600',        // 🟡 과체중: 노란색
  [BODY_TYPES.MILD_OBESE]: 'text-orange-600',        // 🟠 경도비만: 주황색
  [BODY_TYPES.OBESE]: 'text-red-600',                // 🔴 비만: 빨간색
  [BODY_TYPES.SKINNY_FAT]: 'text-red-600',           // 🔴 마른비만: 빨간색
  [BODY_TYPES.SEVERE_OBESE]: 'text-red-800',         // 🟤 고도비만: 진한 빨간색
  [BODY_TYPES.ABDOMINAL_OBESE]: 'text-red-700',      // 🔴 복부비만형: 빨간색
  [BODY_TYPES.MUSCULAR_OBESE]: 'text-indigo-600'     // 🟦 근육형비만: 인디고색
};

// 체형별 배경 색상 매핑
export const BODY_TYPE_BG_COLORS = {
  [BODY_TYPES.ATHLETE]: 'bg-purple-100 text-purple-800',
  [BODY_TYPES.MUSCULAR]: 'bg-blue-100 text-blue-800',
  [BODY_TYPES.OPTIMAL]: 'bg-green-100 text-green-800',
  [BODY_TYPES.SLIM]: 'bg-teal-100 text-teal-800',
  [BODY_TYPES.MUSCULAR_SLIM]: 'bg-cyan-100 text-cyan-800',
  [BODY_TYPES.SLIGHTLY_THIN]: 'bg-yellow-100 text-yellow-800',
  [BODY_TYPES.THIN]: 'bg-orange-100 text-orange-800',
  [BODY_TYPES.OVERWEIGHT]: 'bg-yellow-100 text-yellow-800',
  [BODY_TYPES.MILD_OBESE]: 'bg-orange-100 text-orange-800',
  [BODY_TYPES.OBESE]: 'bg-red-100 text-red-800',
  [BODY_TYPES.SKINNY_FAT]: 'bg-red-100 text-red-800',
  [BODY_TYPES.SEVERE_OBESE]: 'bg-red-200 text-red-900',
  [BODY_TYPES.ABDOMINAL_OBESE]: 'bg-red-100 text-red-800',
  [BODY_TYPES.MUSCULAR_OBESE]: 'bg-indigo-100 text-indigo-800'
};

// 체형별 이모지 매핑
export const BODY_TYPE_EMOJIS = {
  [BODY_TYPES.ATHLETE]: '🏆',
  [BODY_TYPES.MUSCULAR]: '💪',
  [BODY_TYPES.OPTIMAL]: '✅',
  [BODY_TYPES.SLIM]: '🌿',
  [BODY_TYPES.MUSCULAR_SLIM]: '💪🌿',
  [BODY_TYPES.SLIGHTLY_THIN]: '📏',
  [BODY_TYPES.THIN]: '📐',
  [BODY_TYPES.OVERWEIGHT]: '⚖️',
  [BODY_TYPES.MILD_OBESE]: '⚠️',
  [BODY_TYPES.OBESE]: '🚨',
  [BODY_TYPES.SKINNY_FAT]: '🎭',
  [BODY_TYPES.SEVERE_OBESE]: '🚨🚨',
  [BODY_TYPES.ABDOMINAL_OBESE]: '🍎',
  [BODY_TYPES.MUSCULAR_OBESE]: '💪⚠️'
};

// 체형별 표시명 매핑
export const BODY_TYPE_DISPLAY_NAMES = {
  [BODY_TYPES.ATHLETE]: '운동선수급',
  [BODY_TYPES.MUSCULAR]: '근육형',
  [BODY_TYPES.OPTIMAL]: '적정',
  [BODY_TYPES.SLIM]: '날씬',
  [BODY_TYPES.MUSCULAR_SLIM]: '근육형 날씬',
  [BODY_TYPES.SLIGHTLY_THIN]: '약간 마름',
  [BODY_TYPES.THIN]: '마름',
  [BODY_TYPES.OVERWEIGHT]: '과체중',
  [BODY_TYPES.MILD_OBESE]: '경도 비만',
  [BODY_TYPES.OBESE]: '비만',
  [BODY_TYPES.SKINNY_FAT]: '마른 비만',
  [BODY_TYPES.SEVERE_OBESE]: '고도 비만',
  [BODY_TYPES.ABDOMINAL_OBESE]: '복부 비만형',
  [BODY_TYPES.MUSCULAR_OBESE]: '근육형 비만'
};

// 체형별 색상 가져오기
export const getBodyTypeColor = (bodyType) => {
  return BODY_TYPE_COLORS[bodyType] || 'text-gray-600';
};

// 체형별 배경 색상 가져오기
export const getBodyTypeBgColor = (bodyType) => {
  return BODY_TYPE_BG_COLORS[bodyType] || 'bg-gray-100 text-gray-800';
};

// 체형별 이모지 가져오기
export const getBodyTypeEmoji = (bodyType) => {
  return BODY_TYPE_EMOJIS[bodyType] || '❓';
};

// 체형별 표시명 가져오기
export const getBodyTypeDisplayName = (bodyType) => {
  return BODY_TYPE_DISPLAY_NAMES[bodyType] || bodyType;
};

// 체형별 상세 정보 가져오기
export const getBodyTypeInfo = (bodyType) => {
  return {
    emoji: getBodyTypeEmoji(bodyType),
    displayName: getBodyTypeDisplayName(bodyType),
    color: getBodyTypeColor(bodyType),
    bgColor: getBodyTypeBgColor(bodyType)
  };
};

// 체형 분류 카테고리별 그룹화
export const BODY_TYPE_CATEGORIES = {
  ATHLETIC: {
    name: '🏃‍♂️ 운동/근육 중심 체형',
    types: [BODY_TYPES.ATHLETE, BODY_TYPES.MUSCULAR, BODY_TYPES.MUSCULAR_SLIM, BODY_TYPES.MUSCULAR_OBESE]
  },
  NORMAL: {
    name: '⚖️ 정상/적정 체형',
    types: [BODY_TYPES.OPTIMAL, BODY_TYPES.SLIM]
  },
  UNDERWEIGHT: {
    name: '📉 저체중/마름 체형',
    types: [BODY_TYPES.SLIGHTLY_THIN, BODY_TYPES.THIN]
  },
  OVERWEIGHT: {
    name: '📈 과체중/비만 체형',
    types: [BODY_TYPES.OVERWEIGHT, BODY_TYPES.MILD_OBESE, BODY_TYPES.OBESE, BODY_TYPES.SKINNY_FAT, BODY_TYPES.SEVERE_OBESE]
  },
  SPECIAL: {
    name: '🎯 특수 체형',
    types: [BODY_TYPES.ABDOMINAL_OBESE]
  }
};

// 체형이 특정 카테고리에 속하는지 확인
export const getBodyTypeCategory = (bodyType) => {
  for (const [categoryKey, category] of Object.entries(BODY_TYPE_CATEGORIES)) {
    if (category.types.includes(bodyType)) {
      return category;
    }
  }
  return null;
};
