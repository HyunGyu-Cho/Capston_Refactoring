import React from 'react';

// 공통 SVG 베이스 컴포넌트
const IllustrationBase = ({ id, from, to, accent, emoji, label }) => (
  <svg
    viewBox="0 0 160 100"
    className="w-full h-40"
    role="img"
    aria-label={label}
  >
    <defs>
      <linearGradient id={`diet-grad-${id}`} x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stopColor={from} />
        <stop offset="100%" stopColor={to} />
      </linearGradient>
    </defs>
    <rect
      x="0"
      y="0"
      width="160"
      height="100"
      rx="18"
      fill={`url(#diet-grad-${id})`}
    />

    {/* 접시 모양 */}
    <circle cx="80" cy="58" r="32" fill="rgba(255,255,255,0.96)" />
    <circle cx="80" cy="58" r="24" fill="rgba(248,250,252,1)" />

    {/* 좌측 장식 */}
    <rect
      x="18"
      y="22"
      width="30"
      height="8"
      rx="4"
      fill="rgba(255,255,255,0.65)"
    />
    <rect
      x="18"
      y="36"
      width="22"
      height="6"
      rx="3"
      fill="rgba(255,255,255,0.4)"
    />

    {/* 포크/나이프 느낌의 라인 */}
    <rect x="50" y="44" width="4" height="28" rx="2" fill={accent} />
    <rect x="106" y="44" width="4" height="28" rx="2" fill={accent} />

    {/* 접시 안 이모지 */}
    <text
      x="80"
      y="62"
      textAnchor="middle"
      fontSize="20"
      fontFamily="system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    >
      {emoji}
    </text>

    {/* 라벨 */}
    <text
      x="20"
      y="30"
      fontSize="12"
      fontWeight="600"
      fill="#0f172a"
      fontFamily="system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    >
      {label}
    </text>
  </svg>
);

// 식단 카테고리/끼니 기반 10가지 변형
const dietVariants = [
  { key: 'breakfast', from: '#fef3c7', to: '#fde68a', accent: '#f59e0b', emoji: '🥣', label: '든든한 아침' },
  { key: 'lunch', from: '#fee2e2', to: '#fecaca', accent: '#ef4444', emoji: '🍱', label: '균형 점심' },
  { key: 'dinner', from: '#ede9fe', to: '#ddd6fe', accent: '#8b5cf6', emoji: '🍲', label: '가벼운 저녁' },
  { key: 'snack', from: '#dcfce7', to: '#bbf7d0', accent: '#22c55e', emoji: '🍏', label: '건강 간식' },
  { key: 'pre', from: '#e0f2fe', to: '#bae6fd', accent: '#0ea5e9', emoji: '⚡', label: '운동 전 에너지' },
  { key: 'post', from: '#fee2e2', to: '#fecaca', accent: '#fb7185', emoji: '💪', label: '운동 후 회복' },
  { key: 'healthy', from: '#dcfce7', to: '#bbf7d0', accent: '#16a34a', emoji: '🥗', label: '클린 식단' },
  { key: 'diet', from: '#fee2e2', to: '#fed7e2', accent: '#f97316', emoji: '🥕', label: '다이어트' },
  { key: 'muscle', from: '#e0f2fe', to: '#c7d2fe', accent: '#6366f1', emoji: '🍗', label: '근육 증량' },
  { key: 'balanced', from: '#f1f5f9', to: '#e2e8f0', accent: '#64748b', emoji: '🍽️', label: '균형 잡힌 한 끼' },
];

const mapDietCategoryToKey = (dietCategory, meal) => {
  const c = String(dietCategory || '');
  if (c.includes('아침')) return 'breakfast';
  if (c.includes('점심')) return 'lunch';
  if (c.includes('저녁')) return 'dinner';
  if (c.includes('간식')) return 'snack';
  if (c.includes('운동전')) return 'pre';
  if (c.includes('운동후')) return 'post';
  if (c.includes('건강')) return 'healthy';
  if (c.includes('다이어트')) return 'diet';
  if (c.includes('근육') || c.includes('증량')) return 'muscle';
  if (c.includes('균형')) return 'balanced';

  // dietCategory가 없을 때는 끼니 이름으로 판별
  if (meal === 'breakfast') return 'breakfast';
  if (meal === 'lunch') return 'lunch';
  if (meal === 'dinner') return 'dinner';
  if (meal === 'snack') return 'snack';

  return 'balanced';
};

// label을 넘기면 실제 추천된 식단 카테고리/끼니명을 그대로 표기하고,
// 넘기지 않으면 기본 라벨(든든한 아침, 균형 점심 등)을 사용
export default function DietIllustration({ meal, category, index = 0, label }) {
  const key = mapDietCategoryToKey(category, meal);
  const baseIndex = dietVariants.findIndex((v) => v.key === key);
  const start = baseIndex >= 0 ? baseIndex : 0;
  const variant = dietVariants[(start + index) % dietVariants.length];

  return (
    <IllustrationBase
      id={`${variant.key}-${index}`}
      from={variant.from}
      to={variant.to}
      accent={variant.accent}
      emoji={variant.emoji}
      label={label || variant.label}
    />
  );
}