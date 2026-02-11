import { useState } from "react";
import Button from "./Button";
import InputField from "./InputField";
import StarRating from "./StarRating";

export default function EvaluationWithReviewForm({ onSubmit, loading, onSuccess }) {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState("");
  const [reviewTitle, setReviewTitle] = useState("");
  const [reviewContent, setReviewContent] = useState("");
  const [error, setError] = useState("");
  const [showReviewForm, setShowReviewForm] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (rating === 0) return setError("별점을 선택해 주세요.");
    if (!comment.trim() || comment.length < 5) return setError("5자 이상 의견을 입력해 주세요.");
    
    setError("");
    
    try {
      const response = await onSubmit({ 
        rating, 
        comment, 
        reviewTitle, 
        reviewContent,
        showReviewForm 
      });
      
      if (response.success && response.reviewUrl) {
        // 성공 시 후기 페이지로 이동
        onSuccess(response);
      } else {
        // 평점만 작성한 경우
        onSuccess(response);
      }
      
      // 폼 초기화
      setRating(0);
      setComment("");
      setReviewTitle("");
      setReviewContent("");
      setShowReviewForm(false);
      
    } catch (error) {
      setError("제출에 실패했습니다: " + error.message);
    }
  };

  const toggleReviewForm = () => {
    setShowReviewForm(!showReviewForm);
    if (!showReviewForm) {
      // 후기 폼을 보여줄 때 자동으로 제목과 내용 생성
      setReviewTitle(generateAutoTitle(rating, comment));
      setReviewContent(generateAutoContent(rating, comment));
    }
  };

  const generateAutoTitle = (rating, comment) => {
    if (rating >= 4) return "👍 서비스 후기 - 만족스러운 서비스";
    if (rating >= 3) return "😊 서비스 후기 - 괜찮은 서비스";
    return "🤔 서비스 후기 - 개선이 필요한 서비스";
  };

  const generateAutoContent = (rating, comment) => {
    let content = "";
    if (rating >= 4) {
      content = "전반적으로 매우 만족스러운 서비스입니다.\n\n";
    } else if (rating >= 3) {
      content = "전반적으로 괜찮은 서비스입니다.\n\n";
    } else {
      content = "개선이 필요한 부분이 있는 서비스입니다.\n\n";
    }
    
    content += `📝 사용자 의견:\n${comment}\n\n`;
    content += `⭐ 평점: ${"★".repeat(rating)}${"☆".repeat(5 - rating)} (${rating}/5점)`;
    
    return content;
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-md p-8 w-full max-w-2xl flex flex-col gap-6 mb-10">
      <h2 className="text-2xl font-bold text-gray-900 mb-2">서비스 평가하기</h2>
      
      {/* 별점 선택 */}
      <div className="flex flex-col items-center gap-2">
        <span className="text-lg text-gray-700">별점을 선택해 주세요</span>
        <StarRating value={rating} onChange={setRating} disabled={loading} />
      </div>
      
      {/* 기본 의견 */}
      <InputField
        label="의견"
        value={comment}
        onChange={e => setComment(e.target.value)}
        placeholder="서비스에 대한 의견을 남겨주세요 (5자 이상)"
        disabled={loading}
        required
      />
      
      {/* 후기 작성 옵션 */}
      <div className="flex items-center gap-3">
        <input
          type="checkbox"
          id="createReview"
          checked={showReviewForm}
          onChange={toggleReviewForm}
          className="w-4 h-4 text-blue-600 rounded"
        />
        <label htmlFor="createReview" className="text-gray-700 font-medium">
          커뮤니티에 후기도 함께 작성하기
        </label>
      </div>
      
      {/* 후기 작성 폼 */}
      {showReviewForm && (
        <div className="border-t pt-6 space-y-4">
          <h3 className="text-lg font-semibold text-gray-800">후기 작성</h3>
          
          <InputField
            label="후기 제목"
            value={reviewTitle}
            onChange={e => setReviewTitle(e.target.value)}
            placeholder="후기 제목을 입력하세요"
            disabled={loading}
          />
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              후기 내용
            </label>
            <textarea
              value={reviewContent}
              onChange={e => setReviewContent(e.target.value)}
              placeholder="상세한 후기를 작성해주세요"
              disabled={loading}
              rows={6}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          
          <div className="bg-blue-50 p-4 rounded-lg">
            <p className="text-sm text-blue-800">
              💡 후기는 커뮤니티의 "후기" 카테고리에 자동으로 저장되며, 
              다른 사용자들과 공유됩니다. 평점과 의견을 바탕으로 자동 생성된 
              내용을 수정하여 더 상세한 후기를 작성할 수 있습니다.
            </p>
          </div>
        </div>
      )}
      
      {error && <div className="text-red-500 text-sm">{error}</div>}
      
      <Button 
        type="submit" 
        disabled={loading || rating === 0 || !comment}
        className="w-full"
      >
        {loading ? "제출 중..." : showReviewForm ? "평점과 후기 제출" : "평가 제출"}
      </Button>
    </form>
  );
}
