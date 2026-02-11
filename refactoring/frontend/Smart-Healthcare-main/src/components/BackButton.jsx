// src/components/BackButton.jsx
export default function BackButton({ onClick }) {
  return (
    <button
      onClick={onClick}
      className="text-2xl font-bold text-gray-700 hover:text-primary transition"
      aria-label="뒤로가기"
    >
      ←
    </button>
  );
}
