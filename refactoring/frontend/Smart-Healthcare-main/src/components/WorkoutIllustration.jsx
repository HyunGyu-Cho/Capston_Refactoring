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
      <linearGradient id={`grad-${id}`} x1="0%" y1="0%" x2="100%" y2="100%">
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
      fill={`url(#grad-${id})`}
    />
    {/* 배경 장식 원 */}
    <circle cx="28" cy="24" r="10" fill="rgba(255,255,255,0.28)" />
    <circle cx="140" cy="18" r="14" fill="rgba(255,255,255,0.22)" />
    <circle cx="142" cy="78" r="10" fill="rgba(255,255,255,0.18)" />

    {/* 메인 아이콘 영역 */}
    <rect
      x="22"
      y="34"
      width="116"
      height="46"
      rx="14"
      fill="rgba(15,23,42,0.08)"
    />

    {/* 간단한 덤벨/바 형태 라인 */}
    <rect x="36" y="53" width="88" height="6" rx="3" fill={accent} />
    <rect x="44" y="46" width="10" height="20" rx="3" fill="white" opacity="0.9" />
    <rect x="106" y="46" width="10" height="20" rx="3" fill="white" opacity="0.9" />

    {/* 이모지 + 라벨 */}
    <text
      x="32"
      y="30"
      fontSize="16"
      fontFamily="system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    >
      {emoji}
    </text>
    <text
      x="50"
      y="32"
      fontSize="12"
      fontWeight="600"
      fill="#0f172a"
      fontFamily="system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    >
      {label}
    </text>
  </svg>
);

// 운동 카테고리/인덱스 기반 10가지 변형
const workoutVariants = [
  { key: 'upper', from: '#fee2e2', to: '#fecaca', accent: '#ef4444', emoji: '💪', label: '상체 근력' },
  { key: 'lower', from: '#dcfce7', to: '#bbf7d0', accent: '#22c55e', emoji: '🦵', label: '하체 근력' },
  { key: 'core', from: '#ffedd5', to: '#fed7aa', accent: '#fb923c', emoji: '🔥', label: '코어 & 복근' },
  { key: 'cardio', from: '#e0f2fe', to: '#bae6fd', accent: '#0ea5e9', emoji: '🏃‍♂️', label: '유산소' },
  { key: 'full', from: '#e0f2fe', to: '#f5d0fe', accent: '#6366f1', emoji: '🏋️‍♂️', label: '전신 서킷' },
  { key: 'stretch', from: '#fef9c3', to: '#fef3c7', accent: '#eab308', emoji: '🧘‍♂️', label: '스트레칭' },
  { key: 'hiit', from: '#fee2e2', to: '#fecaca', accent: '#f97316', emoji: '⚡', label: '고강도 인터벌' },
  { key: 'shoulder', from: '#ede9fe', to: '#ddd6fe', accent: '#8b5cf6', emoji: '🤸‍♂️', label: '어깨 & 자세' },
  { key: 'back', from: '#e0f2fe', to: '#bfdbfe', accent: '#3b82f6', emoji: '🧍‍♂️', label: '등 & 자세' },
  { key: 'balance', from: '#f1f5f9', to: '#e2e8f0', accent: '#64748b', emoji: '⚖️', label: '균형 & 회복' },
];

const mapCategoryToKey = (category) => {
  if (!category) return 'full';
  const c = String(category);
  if (c.includes('등')) return 'back';
  if (c.includes('하체')) return 'lower';
  if (c.includes('가슴')) return 'upper';
  if (c.includes('어깨')) return 'shoulder';
  if (c.includes('복근') || c.toLowerCase().includes('core')) return 'core';
  if (c.includes('유산소') || c.toLowerCase().includes('cardio')) return 'cardio';
  if (c.includes('스트레칭') || c.toLowerCase().includes('stretch')) return 'stretch';
  if (c.toLowerCase().includes('hiit') || c.includes('인터벌')) return 'hiit';
  return 'full';
};

// label을 넘기면 실제 추천된 카테고리/운동명을 그대로 표기하고,
// 넘기지 않으면 기본 라벨(전신 서킷, 스트레칭 등)을 사용
export default function WorkoutIllustration({ category, index = 0, label }) {
  const key = mapCategoryToKey(category);
  const baseIndex = workoutVariants.findIndex((v) => v.key === key);
  const start = baseIndex >= 0 ? baseIndex : 0;
  const variant = workoutVariants[(start + index) % workoutVariants.length];

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