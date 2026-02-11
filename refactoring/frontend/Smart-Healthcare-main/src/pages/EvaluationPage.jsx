import Header from "../components/Header";
import Footer from "../components/Footer";
import EvaluationForm from "../components/EvaluationForm";
import EvaluationList from "../components/EvaluationList";
import { useState, useEffect } from "react";
import Layout from "../components/Layout";
import HeroWithBg from "../components/HeroWithBg";
import SectionWithWave from "../components/SectionWithWave";
import { apiCall } from "../api/config";
import { useUser, getCurrentUserId } from "../api/auth";

export default function EvaluationPage() {
  const { user, isLoggedIn } = useUser();
  const [evaluations, setEvaluations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [listLoading, setListLoading] = useState(true);
  const [successMsg, setSuccessMsg] = useState("");

  // 후기 목록 불러오기
  const fetchEvaluations = async () => {
    setListLoading(true);
    try {
      const data = await apiCall("/api/evaluation");
      // 백엔드 응답 구조에 맞게 변환 (페이징된 응답)
      const evaluationsData = data.data?.content || data.data || [];
      const formattedEvaluations = evaluationsData.map(evaluation => ({
        id: evaluation.id,
        rating: evaluation.rating,
        comment: evaluation.comment,
        date: evaluation.createdAt ? new Date(evaluation.createdAt).toLocaleDateString('ko-KR') : '날짜 없음',
        user: `사용자 ${evaluation.userId}` || '익명'
      }));
      setEvaluations(formattedEvaluations);
    } catch (error) {
      console.error('평가 목록 조회 실패:', error);
      setEvaluations([]);
    } finally {
      setListLoading(false);
    }
  };
  useEffect(() => { fetchEvaluations(); }, []);

  // 제출 핸들러
  const handleSubmit = async ({ rating, comment }) => {
    setLoading(true);
    setSuccessMsg("");
    try {
      const userId = getCurrentUserId();
      if (!userId) {
        setSuccessMsg("로그인이 필요합니다.");
        return;
      }

      const data = await apiCall("/api/evaluation", {
        method: "POST",
        body: JSON.stringify({ 
          userId: userId,
          rating: rating, 
          comment: comment
        }),
      });
      
      setSuccessMsg(data.message || "후기가 성공적으로 등록되었습니다!");
      await fetchEvaluations();
    } catch (error) {
      console.error('평가 등록 실패:', error);
      setSuccessMsg("평가 등록에 실패했습니다: " + error.message);
    } finally {
      setLoading(false);
      setTimeout(() => setSuccessMsg(""), 3000);
    }
  };

  return (
    <Layout>
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-purple-50 to-pink-50 flex flex-col items-center justify-center py-16 px-6">
        <div className="w-full max-w-4xl flex flex-col items-center">
          {/* 헤더 */}
          <div className="mb-12 text-center w-full">
            <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-r from-yellow-400 to-orange-500 rounded-full mb-6 shadow-lg">
              <span className="text-3xl">⭐</span>
            </div>
            <h1 className="text-4xl font-bold text-gray-800 mb-4">서비스 평가하기</h1>
            <p className="text-gray-600 text-lg max-w-2xl mx-auto">
              별점과 의견을 남겨주시면 더 나은 서비스로 보답하겠습니다
            </p>
          </div>

          {/* 평가 폼 섹션 */}
          <div className="bg-white rounded-3xl shadow-xl border border-gray-100 p-8 mb-8 w-full max-w-2xl mx-auto">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-gradient-to-r from-purple-400 to-pink-500 rounded-full flex items-center justify-center">
                <span className="text-white text-lg">✍️</span>
              </div>
              <h2 className="text-2xl font-semibold text-gray-800">평가 작성하기</h2>
            </div>
            <p className="text-gray-500 mb-6 text-center">
              전체적인 만족도, UX/UI, 추천 정확도 등 자유롭게 평가해주세요
            </p>
            <EvaluationForm onSubmit={handleSubmit} loading={loading} />
            {successMsg && (
              <div className="mt-6 p-4 bg-gradient-to-r from-green-50 to-emerald-50 border border-green-200 rounded-xl">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center">
                    <span className="text-white text-sm">✓</span>
                  </div>
                  <p className="text-green-700 font-medium">{successMsg}</p>
                </div>
              </div>
            )}
          </div>

          {/* 후기 목록 섹션 */}
          <div className="bg-white rounded-3xl shadow-xl border border-gray-100 p-8 w-full max-w-2xl mx-auto">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-gradient-to-r from-blue-400 to-indigo-500 rounded-full flex items-center justify-center">
                <span className="text-white text-lg">💬</span>
              </div>
              <h3 className="text-2xl font-semibold text-gray-800">후기 목록</h3>
            </div>
            <EvaluationList evaluations={evaluations} loading={listLoading} />
          </div>
        </div>
      </div>
    </Layout>
  );
}