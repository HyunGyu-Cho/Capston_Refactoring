// 카테고리 관련 유틸리티 함수들

// 카테고리 옵션 정의
export const categories = [
  { value: 'EXERCISE', label: '운동' },
  { value: 'DIET', label: '식단' },
  { value: 'QUESTION', label: '질문' },
  { value: 'FREE', label: '자유게시판' },
  { value: 'TIP', label: '팁' },
  { value: 'REVIEW', label: '후기' },
  { value: 'SUCCESS_STORY', label: '성공후기' }
];

// 카테고리 표시 이름 가져오기 (하위 호환성 지원)
export const getCategoryDisplayName = (category) => {
  if (typeof category === 'string') {
    // 기존 방식: enum 값
    return categories.find(cat => cat.value === category)?.label || category;
  } else if (category && category.categoryName) {
    // 새로운 방식: categoryName 필드
    return category.categoryName;
  }
  return category;
};

// 카테고리별 색상 스타일 가져오기
export const getCategoryStyle = (category) => {
  const categoryValue = typeof category === 'string' ? category : category?.value;
  
  switch (categoryValue) {
    case 'EXERCISE':
      return 'bg-blue-100 text-blue-800';
    case 'DIET':
      return 'bg-green-100 text-green-800';
    case 'QUESTION':
      return 'bg-yellow-100 text-yellow-800';
    case 'TIP':
      return 'bg-purple-100 text-purple-800';
    case 'REVIEW':
      return 'bg-orange-100 text-orange-800';
    case 'SUCCESS_STORY':
      return 'bg-pink-100 text-pink-800';
    case 'FREE':
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

// 카테고리 값으로부터 라벨 가져오기
export const getCategoryLabel = (categoryValue) => {
  return categories.find(cat => cat.value === categoryValue)?.label || categoryValue;
};

// 카테고리 값 유효성 검사
export const isValidCategory = (categoryValue) => {
  return categories.some(cat => cat.value === categoryValue);
};
