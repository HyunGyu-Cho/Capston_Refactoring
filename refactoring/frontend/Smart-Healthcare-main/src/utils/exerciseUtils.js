// 운동 종류 및 체형별 맞춤 추천 유틸리티

// 운동 종류 정의
export const EXERCISE_TYPES = {
  BACK: '등운동',
  LEG: '하체운동', 
  CHEST: '가슴운동',
  SHOULDER: '어깨운동',
  CORE: '복근운동',
  CARDIO: '유산소',
  OTHER: '기타'
};

// 운동 종류별 색상 매핑
export const EXERCISE_TYPE_COLORS = {
  [EXERCISE_TYPES.BACK]: 'bg-blue-100 text-blue-800',
  [EXERCISE_TYPES.LEG]: 'bg-green-100 text-green-800',
  [EXERCISE_TYPES.CHEST]: 'bg-red-100 text-red-800',
  [EXERCISE_TYPES.SHOULDER]: 'bg-purple-100 text-purple-800',
  [EXERCISE_TYPES.CORE]: 'bg-orange-100 text-orange-800',
  [EXERCISE_TYPES.CARDIO]: 'bg-pink-100 text-pink-800',
  [EXERCISE_TYPES.OTHER]: 'bg-gray-100 text-gray-800'
};

// 운동 종류별 이모지
export const EXERCISE_TYPE_EMOJIS = {
  [EXERCISE_TYPES.BACK]: '🏋️‍♂️',
  [EXERCISE_TYPES.LEG]: '🦵',
  [EXERCISE_TYPES.CHEST]: '💪',
  [EXERCISE_TYPES.SHOULDER]: '🤸‍♂️',
  [EXERCISE_TYPES.CORE]: '🔥',
  [EXERCISE_TYPES.CARDIO]: '🏃‍♂️',
  [EXERCISE_TYPES.OTHER]: '⚡'
};

// 체형별 운동 우선순위 매핑
export const BODY_TYPE_EXERCISE_PRIORITY = {
  '운동선수급': {
    primary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.CORE],
    secondary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG, EXERCISE_TYPES.CHEST],
    tertiary: [EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.OTHER],
    focus: '성능 향상 및 유지'
  },
  '근육형': {
    primary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.CHEST, EXERCISE_TYPES.LEG],
    secondary: [EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.CORE],
    tertiary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.OTHER],
    focus: '근육량 유지 및 균형'
  },
  '적정': {
    primary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.CORE],
    secondary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG],
    tertiary: [EXERCISE_TYPES.CHEST, EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.OTHER],
    focus: '전체적인 건강 유지'
  },
  '날씬': {
    primary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG, EXERCISE_TYPES.CHEST],
    secondary: [EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.CORE],
    tertiary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.OTHER],
    focus: '근육량 증가'
  },
  '근육형날씬': {
    primary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG, EXERCISE_TYPES.CHEST],
    secondary: [EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.CORE],
    tertiary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.OTHER],
    focus: '근육량 증가 및 정의'
  },
  '약간마름': {
    primary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG, EXERCISE_TYPES.CHEST],
    secondary: [EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.CORE],
    tertiary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.OTHER],
    focus: '근육량 증가'
  },
  '마름': {
    primary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG, EXERCISE_TYPES.CHEST],
    secondary: [EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.CORE],
    tertiary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.OTHER],
    focus: '근육량 증가 및 체중 증가'
  },
  '과체중': {
    primary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.CORE],
    secondary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG],
    tertiary: [EXERCISE_TYPES.CHEST, EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.OTHER],
    focus: '체지방 감소'
  },
  '경도비만': {
    primary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.CORE],
    secondary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG],
    tertiary: [EXERCISE_TYPES.CHEST, EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.OTHER],
    focus: '체지방 감소 및 근력 향상'
  },
  '비만': {
    primary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.CORE],
    secondary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG],
    tertiary: [EXERCISE_TYPES.CHEST, EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.OTHER],
    focus: '체지방 감소 및 기초 체력 향상'
  },
  '마른비만': {
    primary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG, EXERCISE_TYPES.CHEST],
    secondary: [EXERCISE_TYPES.CORE, EXERCISE_TYPES.CARDIO],
    tertiary: [EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.OTHER],
    focus: '근육량 증가 및 체지방 감소'
  },
  '고도비만': {
    primary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.CORE],
    secondary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG],
    tertiary: [EXERCISE_TYPES.CHEST, EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.OTHER],
    focus: '체지방 감소 및 기초 체력 향상 (안전 우선)'
  },
  '복부비만형': {
    primary: [EXERCISE_TYPES.CORE, EXERCISE_TYPES.CARDIO],
    secondary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG],
    tertiary: [EXERCISE_TYPES.CHEST, EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.OTHER],
    focus: '복부 지방 감소 및 코어 강화'
  },
  '근육형비만': {
    primary: [EXERCISE_TYPES.CARDIO, EXERCISE_TYPES.CORE],
    secondary: [EXERCISE_TYPES.BACK, EXERCISE_TYPES.LEG],
    tertiary: [EXERCISE_TYPES.CHEST, EXERCISE_TYPES.SHOULDER, EXERCISE_TYPES.OTHER],
    focus: '체지방 감소 및 근육량 유지'
  }
};

// 운동 강도별 설정
export const EXERCISE_INTENSITY = {
  LOW: { name: '낮음', color: 'text-green-600', description: '초보자용' },
  MEDIUM: { name: '보통', color: 'text-yellow-600', description: '중급자용' },
  HIGH: { name: '높음', color: 'text-red-600', description: '고급자용' }
};

// 운동 난이도별 설정
export const EXERCISE_DIFFICULTY = {
  BEGINNER: { name: '초급', color: 'text-blue-600', description: '기초 단계' },
  INTERMEDIATE: { name: '중급', color: 'text-orange-600', description: '중간 단계' },
  ADVANCED: { name: '고급', color: 'text-red-600', description: '고급 단계' }
};

// 운동 타입별 설정
export const EXERCISE_CATEGORY = {
  STRENGTH: { name: '근력', color: 'text-purple-600', description: '근력 향상' },
  CARDIO: { name: '유산소', color: 'text-pink-600', description: '심폐 기능 향상' },
  FLEXIBILITY: { name: '유연성', color: 'text-teal-600', description: '유연성 향상' }
};

// 체형별 운동 우선순위 가져오기
export const getExercisePriority = (bodyType) => {
  return BODY_TYPE_EXERCISE_PRIORITY[bodyType] || BODY_TYPE_EXERCISE_PRIORITY['적정'];
};

// 운동 종류별 색상 가져오기
export const getExerciseTypeColor = (exerciseType) => {
  return EXERCISE_TYPE_COLORS[exerciseType] || 'bg-gray-100 text-gray-800';
};

// 운동 종류별 이모지 가져오기
export const getExerciseTypeEmoji = (exerciseType) => {
  return EXERCISE_TYPE_EMOJIS[exerciseType] || '⚡';
};

// 운동 정보 가져오기
export const getExerciseInfo = (exerciseType) => {
  return {
    emoji: getExerciseTypeEmoji(exerciseType),
    color: getExerciseTypeColor(exerciseType),
    displayName: exerciseType
  };
};

// 설문조사 기반 운동 선호도 분석
export const analyzeExercisePreference = (survey) => {
  if (!survey) return null;
  
  const preferences = {
    frequency: survey.workoutFrequency || '주 3회',
    days: survey.selectedDays || ['월', '수', '금'],
    duration: '30-45분', // 기본값
    equipment: '없음', // 기본값
    focus: survey.text || '전체적인 건강 향상'
  };
  
  // 설문 내용에서 운동 선호도 추출
  const text = survey.text || '';
  if (text.includes('근육') || text.includes('근력')) {
    preferences.focus = '근력 향상';
  } else if (text.includes('체지방') || text.includes('다이어트')) {
    preferences.focus = '체지방 감소';
  } else if (text.includes('체력') || text.includes('지구력')) {
    preferences.focus = '체력 향상';
  }
  
  return preferences;
};
