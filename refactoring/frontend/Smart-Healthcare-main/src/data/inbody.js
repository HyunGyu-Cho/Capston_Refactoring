// src/data/inbody.js

// 정적 인바디 입력 항목 정의
export const INBODY_FEATURES = [
  { name: '성별', type: 'select', options: ['남성', '여성'] },
  { name: '사용자 출생년도', type: 'number', min: 1900, },
  { name: '체중', type: 'number', min: 0, max: 2000, step: 0.01, unit: 'kg' },
  { name: '총체수분', type: 'number', min: 0, max: 10000, step: 0.01, unit: 'L' },
  { name: '단백질', type: 'number', min: 0, max: 10000, step: 0.01, unit: 'kg' },
  { name: '무기질', type: 'number', min: 0, max: 10000, step: 0.01, unit: 'kg' },
  { name: '체지방량', type: 'number', min: 0, max: 10000, step: 0.01, unit: 'kg' },
  { name: '근육량', type: 'number', min: 0, max: 1000, step: 0.01, unit: 'kg' },
  { name: '제지방량', type: 'number', min: 0, max: 1500, step: 0.01, unit: 'kg' },
  { name: '골격근량', type: 'number', min: 0, max: 1000, step: 0.01, unit: 'kg' },
  { name: '체질량지수', type: 'number', min: 0, max: 1000, step: 0.01 },
  { name: '체지방률', type: 'number', min: 0, max: 1000, step: 0.01, unit: '%' },
  { name: '오른팔 근육량', type: 'number', min: 0, max: 1000, step: 0.01, unit: 'kg' },
  { name: '왼팔 근육량', type: 'number', min: 0, max: 1000, step: 0.01, unit: 'kg' },
  { name: '몸통 근육량', type: 'number', min: 0, max: 5000, step: 0.01, unit: 'kg' },
  { name: '오른다리 근육량', type: 'number', min: 0, max: 1000, step: 0.01, unit: 'kg' },
  { name: '왼다리 근육량', type: 'number', min: 0, max: 1000, step: 0.01, unit: 'kg' },
  { name: '오른팔 체지방량', type: 'number', min: 0, max: 1000, step: 0.01, unit: 'kg' },
  { name: '왼팔 체지방량', type: 'number', min: 0, max: 1000, step: 0.01, unit: 'kg' },
  { name: '몸통 체지방량', type: 'number', min: 0, max: 1000, step: 0.01, unit: 'kg' },
  { name: '오른다리 체지방량', type: 'number', min: 0, max: 1500, step: 0.01, unit: 'kg' },
  { name: '왼다리 체지방량', type: 'number', min: 0, max: 1500, step: 0.01, unit: 'kg' },
  { name: '인바디점수', type: 'number', min: 0, max: 1000, unit: '점' },
  { name: '적정체중', type: 'number', min: 40, max: 1200, step: 0.01, unit: 'kg' },
  { name: '체중조절', type: 'number', min: -1000, max: 500, step: 0.01, unit: 'kg' },
  { name: '지방조절', type: 'number', min: -1000, max: 1000, step: 0.01, unit: 'kg' },
  { name: '근육조절', type: 'number', min: -1000, max: 1000, step: 0.01, unit: 'kg' },
  { name: '기초대사량', type: 'number', min: 0, max: 3000, unit: 'kcal' },
  { name: '복부지방률', type: 'number', min: 0, max: 1000, step: 0.01, unit: '%' },
  { name: '내장지방레벨', type: 'number', min: 0, max: 1000, step: 0.01, unit: '레벨' },
  { name: '비만도', type: 'number', min: 0, max: 1000, step: 0.01, unit: '%' },
  { name: '골무기질량', type: 'number', min: 0, max: 1000, step: 0.01 },
  { name: '복부둘레', type: 'number', min: 0, max: 1000, step: 0.01, unit: 'cm' }

]; 