import React from 'react';

export default function SectionWithWave({ children, bgColor = 'bg-white', className = '' }) {
  return (
    <div className={`relative ${bgColor} py-12 md:py-20 ${className}`}>
      {/* 위쪽 웨이브 */}
      <div className="absolute top-0 left-0 w-full overflow-hidden leading-none pointer-events-none">
        <svg className="relative block w-full h-10 md:h-16" viewBox="0 0 1200 120" preserveAspectRatio="none">
          <path d="M0,0 V40 C150,80 350,0 600,40 S1050,80 1200,40 V0 H0 Z" fill="currentColor" />
        </svg>
      </div>
      <div className="relative z-10 container mx-auto px-6">{children}</div>
      {/* 아래쪽 웨이브 */}
      <div className="absolute bottom-0 left-0 w-full overflow-hidden leading-none pointer-events-none rotate-180">
        <svg className="relative block w-full h-10 md:h-16" viewBox="0 0 1200 120" preserveAspectRatio="none">
          <path d="M0,0 V40 C150,80 350,0 600,40 S1050,80 1200,40 V0 H0 Z" fill="currentColor" />
        </svg>
      </div>
    </div>
  );
} 