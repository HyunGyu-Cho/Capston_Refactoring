module.exports = {
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#3B82F6',
        secondary: '#6B7280',
        accent: '#FBBF24',
        success: '#22C55E',
        error: '#EF4444',
        info: '#0EA5E9',
        'bg-section': '#F9FAFB',
      },
      fontFamily: {
        sans: ['Noto Sans KR', 'sans-serif'],
        display: ['Montserrat', 'Noto Sans KR', 'sans-serif'],
      },
      boxShadow: {
        fancy: '0 4px 24px 0 rgba(0,0,0,0.12), 0 1.5px 4px 0 rgba(0,0,0,0.08)',
      },
      borderRadius: {
        xl: '1.25rem',
        '2xl': '2rem',
      },
    },
  },
  plugins: [],
};