import { useState } from "react";
import Button from "./Button";
import StarRating from "./StarRating";

export default function EvaluationForm({ onSubmit, loading }) {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0) return setError("별점을 선택해 주세요.");
    if (!comment.trim() || comment.length < 5) return setError("5자 이상 의견을 입력해 주세요.");
    setError("");
    await onSubmit({ rating, comment });
    setRating(0);
    setComment("");
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-md p-8 w-full max-w-md flex flex-col gap-6 mb-10">
      <h2 className="text-2xl font-bold text-gray-900 mb-2">서비스 평가하기</h2>
      <div className="flex flex-col items-center gap-2">
        <span className="text-lg text-gray-700">별점을 선택해 주세요</span>
        <StarRating value={rating} onChange={setRating} disabled={loading} />
      </div>
      <div className="flex flex-col gap-1">
        <label className="text-gray-800 font-semibold text-base mb-1 tracking-wide">의견</label>
        <textarea
          className="px-4 py-3 border border-blue-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary shadow-sm transition duration-200 hover:border-primary focus:border-primary resize-none"
          value={comment}
          onChange={e => setComment(e.target.value)}
          placeholder="후기를 남겨주세요 (5자 이상)"
          disabled={loading}
          rows={4}
        />
      </div>
      {error && <div className="text-red-500 text-sm">{error}</div>}
      <Button type="submit" disabled={loading || rating === 0 || !comment}>
        {loading ? "제출 중..." : "평가 제출"}
      </Button>
    </form>
  );
} 