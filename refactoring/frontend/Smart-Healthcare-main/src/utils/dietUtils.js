// 식단 종류 및 체형별 맞춤 추천 유틸리티

// 식단 종류 정의
export const DIET_TYPES = {
  BREAKFAST: '아침식단',
  LUNCH: '점심식단',
  DINNER: '저녁식단',
  SNACK: '간식',
  PRE_WORKOUT: '운동전식단',
  POST_WORKOUT: '운동후식단',
  HEALTHY: '건강식단',
  WEIGHT_LOSS: '다이어트식단',
  MUSCLE_GAIN: '근육증량식단',
  BALANCED: '균형식단'
};

// 식단 종류별 색상 매핑
export const DIET_TYPE_COLORS = {
  [DIET_TYPES.BREAKFAST]: 'bg-yellow-100 text-yellow-800',
  [DIET_TYPES.LUNCH]: 'bg-orange-100 text-orange-800',
  [DIET_TYPES.DINNER]: 'bg-purple-100 text-purple-800',
  [DIET_TYPES.SNACK]: 'bg-green-100 text-green-800',
  [DIET_TYPES.PRE_WORKOUT]: 'bg-blue-100 text-blue-800',
  [DIET_TYPES.POST_WORKOUT]: 'bg-red-100 text-red-800',
  [DIET_TYPES.HEALTHY]: 'bg-emerald-100 text-emerald-800',
  [DIET_TYPES.WEIGHT_LOSS]: 'bg-pink-100 text-pink-800',
  [DIET_TYPES.MUSCLE_GAIN]: 'bg-indigo-100 text-indigo-800',
  [DIET_TYPES.BALANCED]: 'bg-teal-100 text-teal-800'
};

// 식단 종류별 이모지
export const DIET_TYPE_EMOJIS = {
  [DIET_TYPES.BREAKFAST]: '🌅',
  [DIET_TYPES.LUNCH]: '☀️',
  [DIET_TYPES.DINNER]: '🌙',
  [DIET_TYPES.SNACK]: '🍎',
  [DIET_TYPES.PRE_WORKOUT]: '⚡',
  [DIET_TYPES.POST_WORKOUT]: '💪',
  [DIET_TYPES.HEALTHY]: '🥗',
  [DIET_TYPES.WEIGHT_LOSS]: '🥕',
  [DIET_TYPES.MUSCLE_GAIN]: '🍗',
  [DIET_TYPES.BALANCED]: '🍽️'
};

// 체형별 식단 우선순위 매핑
export const BODY_TYPE_DIET_PRIORITY = {
  '운동선수급': {
    primary: [DIET_TYPES.PRE_WORKOUT, DIET_TYPES.POST_WORKOUT, DIET_TYPES.MUSCLE_GAIN],
    secondary: [DIET_TYPES.BREAKFAST, DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.SNACK, DIET_TYPES.HEALTHY],
    focus: '성능 향상 및 회복',
    calorieTarget: '높음',
    proteinTarget: '매우 높음'
  },
  '근육형': {
    primary: [DIET_TYPES.MUSCLE_GAIN, DIET_TYPES.POST_WORKOUT],
    secondary: [DIET_TYPES.BREAKFAST, DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.SNACK, DIET_TYPES.HEALTHY],
    focus: '근육량 유지 및 균형',
    calorieTarget: '보통',
    proteinTarget: '높음'
  },
  '적정': {
    primary: [DIET_TYPES.BALANCED, DIET_TYPES.HEALTHY],
    secondary: [DIET_TYPES.BREAKFAST, DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.SNACK, DIET_TYPES.PRE_WORKOUT, DIET_TYPES.POST_WORKOUT],
    focus: '건강 유지',
    calorieTarget: '보통',
    proteinTarget: '보통'
  },
  '날씬': {
    primary: [DIET_TYPES.MUSCLE_GAIN, DIET_TYPES.BREAKFAST],
    secondary: [DIET_TYPES.LUNCH, DIET_TYPES.DINNER, DIET_TYPES.SNACK],
    tertiary: [DIET_TYPES.HEALTHY, DIET_TYPES.BALANCED],
    focus: '근육량 증가',
    calorieTarget: '높음',
    proteinTarget: '높음'
  },
  '근육형날씬': {
    primary: [DIET_TYPES.MUSCLE_GAIN, DIET_TYPES.POST_WORKOUT],
    secondary: [DIET_TYPES.BREAKFAST, DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.SNACK, DIET_TYPES.HEALTHY],
    focus: '근육량 증가 및 정의',
    calorieTarget: '높음',
    proteinTarget: '매우 높음'
  },
  '약간마름': {
    primary: [DIET_TYPES.MUSCLE_GAIN, DIET_TYPES.BREAKFAST],
    secondary: [DIET_TYPES.LUNCH, DIET_TYPES.DINNER, DIET_TYPES.SNACK],
    tertiary: [DIET_TYPES.HEALTHY, DIET_TYPES.BALANCED],
    focus: '근육량 증가',
    calorieTarget: '높음',
    proteinTarget: '높음'
  },
  '마름': {
    primary: [DIET_TYPES.MUSCLE_GAIN, DIET_TYPES.BREAKFAST, DIET_TYPES.SNACK],
    secondary: [DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.HEALTHY, DIET_TYPES.BALANCED],
    focus: '근육량 증가 및 체중 증가',
    calorieTarget: '매우 높음',
    proteinTarget: '매우 높음'
  },
  '과체중': {
    primary: [DIET_TYPES.WEIGHT_LOSS, DIET_TYPES.HEALTHY],
    secondary: [DIET_TYPES.BREAKFAST, DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.SNACK, DIET_TYPES.BALANCED],
    focus: '체지방 감소',
    calorieTarget: '낮음',
    proteinTarget: '보통'
  },
  '경도비만': {
    primary: [DIET_TYPES.WEIGHT_LOSS, DIET_TYPES.HEALTHY],
    secondary: [DIET_TYPES.BREAKFAST, DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.SNACK, DIET_TYPES.BALANCED],
    focus: '체지방 감소 및 근력 향상',
    calorieTarget: '낮음',
    proteinTarget: '보통'
  },
  '비만': {
    primary: [DIET_TYPES.WEIGHT_LOSS, DIET_TYPES.HEALTHY],
    secondary: [DIET_TYPES.BREAKFAST, DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.SNACK, DIET_TYPES.BALANCED],
    focus: '체지방 감소 및 기초 체력 향상',
    calorieTarget: '낮음',
    proteinTarget: '보통'
  },
  '마른비만': {
    primary: [DIET_TYPES.MUSCLE_GAIN, DIET_TYPES.WEIGHT_LOSS],
    secondary: [DIET_TYPES.BREAKFAST, DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.SNACK, DIET_TYPES.HEALTHY],
    focus: '근육량 증가 및 체지방 감소',
    calorieTarget: '보통',
    proteinTarget: '높음'
  },
  '고도비만': {
    primary: [DIET_TYPES.WEIGHT_LOSS, DIET_TYPES.HEALTHY],
    secondary: [DIET_TYPES.BREAKFAST, DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.SNACK, DIET_TYPES.BALANCED],
    focus: '체지방 감소 및 기초 체력 향상 (안전 우선)',
    calorieTarget: '낮음',
    proteinTarget: '보통'
  },
  '복부비만형': {
    primary: [DIET_TYPES.WEIGHT_LOSS, DIET_TYPES.HEALTHY],
    secondary: [DIET_TYPES.BREAKFAST, DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.SNACK, DIET_TYPES.BALANCED],
    focus: '복부 지방 감소 및 코어 강화',
    calorieTarget: '낮음',
    proteinTarget: '보통'
  },
  '근육형비만': {
    primary: [DIET_TYPES.WEIGHT_LOSS, DIET_TYPES.HEALTHY],
    secondary: [DIET_TYPES.BREAKFAST, DIET_TYPES.LUNCH, DIET_TYPES.DINNER],
    tertiary: [DIET_TYPES.SNACK, DIET_TYPES.BALANCED],
    focus: '체지방 감소 및 근육량 유지',
    calorieTarget: '낮음',
    proteinTarget: '높음'
  }
};

// 식단 영양소 타입
export const NUTRIENT_TYPES = {
  PROTEIN: { name: '단백질', color: 'text-red-600', description: '근육 형성' },
  CARBOHYDRATE: { name: '탄수화물', color: 'text-blue-600', description: '에너지 공급' },
  FAT: { name: '지방', color: 'text-yellow-600', description: '호르몬 생성' },
  VITAMIN: { name: '비타민', color: 'text-green-600', description: '면역력' },
  MINERAL: { name: '무기질', color: 'text-purple-600', description: '뼈 건강' },
  FIBER: { name: '식이섬유', color: 'text-orange-600', description: '소화 건강' }
};

// 식단 시간대별 특징
export const MEAL_TIME_CHARACTERISTICS = {
  [DIET_TYPES.BREAKFAST]: {
    focus: '에너지 공급',
    nutrients: ['탄수화물', '단백질', '비타민'],
    portion: '보통',
    timing: '07:00-09:00'
  },
  [DIET_TYPES.LUNCH]: {
    focus: '균형 잡힌 영양',
    nutrients: ['단백질', '탄수화물', '지방', '비타민', '무기질'],
    portion: '보통',
    timing: '12:00-14:00'
  },
  [DIET_TYPES.DINNER]: {
    focus: '회복 및 성장',
    nutrients: ['단백질', '지방', '비타민', '무기질'],
    portion: '적당',
    timing: '18:00-20:00'
  },
  [DIET_TYPES.SNACK]: {
    focus: '혈당 유지',
    nutrients: ['단백질', '탄수화물'],
    portion: '적음',
    timing: '간헐적'
  }
};

// 체형별 식단 우선순위 가져오기
export const getDietPriority = (bodyType) => {
  return BODY_TYPE_DIET_PRIORITY[bodyType] || BODY_TYPE_DIET_PRIORITY['적정'];
};

// 식단 종류별 색상 가져오기
export const getDietTypeColor = (dietType) => {
  return DIET_TYPE_COLORS[dietType] || 'bg-gray-100 text-gray-800';
};

// 식단 종류별 이모지 가져오기
export const getDietTypeEmoji = (dietType) => {
  return DIET_TYPE_EMOJIS[dietType] || '🍽️';
};

// 식단 정보 가져오기
export const getDietInfo = (dietType) => {
  return {
    emoji: getDietTypeEmoji(dietType),
    color: getDietTypeColor(dietType),
    displayName: dietType
  };
};

// 설문조사 기반 식단 선호도 분석
export const analyzeDietPreference = (survey) => {
  if (!survey) return null;
  
  const preferences = {
    mealsPerDay: survey.mealsPerDay || '3',
    mealLabeling: survey.mealLabeling || 'generic',
    selectedMeals: survey.selectedMeals || [],
    selectedMealsLabel: survey.selectedMealsLabel || '아침, 점심, 저녁 (하루 3끼)',
    focus: survey.text || '건강한 식단'
  };
  
  // 설문 내용에서 식단 선호도 추출
  const text = survey.text || '';
  if (text.includes('다이어트') || text.includes('체지방')) {
    preferences.focus = '체지방 감소';
  } else if (text.includes('근육') || text.includes('증량')) {
    preferences.focus = '근육량 증가';
  } else if (text.includes('건강') || text.includes('균형')) {
    preferences.focus = '건강한 식단';
  }
  
  return preferences;
};

// 식단 조합 생성
export const generateDietCombination = (bodyType, survey) => {
  const dietPriority = getDietPriority(bodyType);
  const dietPreference = analyzeDietPreference(survey);
  
  const combination = {
    primary: dietPriority.primary,
    secondary: dietPriority.secondary,
    tertiary: dietPriority.tertiary,
    focus: dietPriority.focus,
    calorieTarget: dietPriority.calorieTarget,
    proteinTarget: dietPriority.proteinTarget,
    preferences: dietPreference
  };
  
  return combination;
};
