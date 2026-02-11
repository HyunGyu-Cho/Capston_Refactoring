// 운동/식단별 이미지 매핑 유틸
// 실제 이미지는 public/assets/workouts, public/assets/foods 아래에 넣어주세요.
// 예:
// - public/assets/workouts/pushup.jpg
// - public/assets/workouts/bench_press.jpg
// - public/assets/workouts/squat.jpg
// - public/assets/workouts/plank.jpg
// - public/assets/workouts/lunge.jpg
// - public/assets/workouts/dumbbell.jpg
// - public/assets/workouts/burpee.jpg
// - public/assets/workouts/high_knee.jpg

const DEFAULT_WORKOUT_IMAGE = '/assets/exercise.jpg';
const DEFAULT_DIET_IMAGE = '/assets/food.jpg';

/**
 * 운동 이름을 기준으로 대표 이미지를 매핑
 * @param {object} workout - 운동 객체 (name 필수)
 * @returns {string} 이미지 URL
 */
export function getWorkoutImage(workout) {
  const name = (workout?.name || '').toLowerCase();

  // 이름에 포함된 키워드로 매핑 (한글/영문 둘 다 지원)
  if (name.includes('푸시업') || name.includes('push')) {
    return '/assets/workouts/pushup.jpg';
  }
  if (name.includes('벤치') || name.includes('bench')) {
    return '/assets/workouts/bench_press.jpg';
  }
  if (name.includes('덤벨') || name.includes('dumbbell')) {
    return '/assets/workouts/dumbbell.jpg';
  }
  if (name.includes('스쿼트') || name.includes('squat')) {
    return '/assets/workouts/squat.jpg';
  }
  if (name.includes('플랭크') || name.includes('plank')) {
    return '/assets/workouts/plank.jpg';
  }
  if (name.includes('런지') || name.includes('lunge')) {
    return '/assets/workouts/lunge.jpg';
  }
  if (name.includes('버피') || name.includes('burpee')) {
    return '/assets/workouts/burpee.jpg';
  }
  if (name.includes('하이니') || name.includes('high knee') || name.includes('high-knee')) {
    return '/assets/workouts/high_knee.jpg';
  }
  if (name.includes('풀업') || name.includes('pull up') || name.includes('pull-up')) {
    return '/assets/workouts/pullup.jpg';
  }

  // 매핑이 없는 경우 기본 운동 이미지
  return DEFAULT_WORKOUT_IMAGE;
}

/**
 * 식단 이름/카테고리를 기준으로 대표 이미지를 매핑 (필요시 확장)
 * @param {object} diet - 식단 객체 (name 또는 dietCategory)
 * @returns {string} 이미지 URL
 */
export function getDietImage(diet) {
  const name = (diet?.name || '').toLowerCase();
  const category = (diet?.dietCategory || diet?.category || '').toLowerCase();

  // 카테고리 기반 예시 (원하면 더 세분화 가능)
  if (category.includes('아침') || category.includes('breakfast')) {
    return '/assets/foods/breakfast.jpg';
  }
  if (category.includes('점심') || category.includes('lunch')) {
    return '/assets/foods/lunch.jpg';
  }
  if (category.includes('저녁') || category.includes('dinner')) {
    return '/assets/foods/dinner.jpg';
  }
  if (category.includes('간식') || category.includes('snack')) {
    return '/assets/foods/snack.jpg';
  }

  // 이름 기반 간단 매핑 예시
  if (name.includes('샐러드') || name.includes('salad')) {
    return '/assets/foods/salad.jpg';
  }
  if (name.includes('볶음밥') || name.includes('fried rice')) {
    return '/assets/foods/fried_rice.jpg';
  }
  if (name.includes('연어') || name.includes('salmon')) {
    return '/assets/foods/salmon.jpg';
  }

  // 매핑이 없는 경우 기본 식단 이미지
  return DEFAULT_DIET_IMAGE;
}


