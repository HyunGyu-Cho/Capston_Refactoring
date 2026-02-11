import Layout from '../components/Layout';
import BackButton from '../components/BackButton';
import Card from '../components/Card';
import { useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Utensils, Flame, Timer, Info, AlertTriangle, Apple } from 'lucide-react';

export default function DietDetailPage() {
  const location = useLocation();
  const { diet } = location.state || {};
  const navigate = useNavigate();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    console.log('🔍 DietDetailPage - location.state:', location.state);
    console.log('🔍 DietDetailPage - diet:', diet);
    
    if (diet) {
      console.log('🔍 식단 데이터가 있음:', diet);
      setDetail(diet);
      setLoading(false);
    } else {
      console.log('🔍 식단 데이터가 없음, 기본 데이터 생성');
      // 기본 식단 데이터 생성
      const defaultDiet = {
        name: '맞춤형 아침식사',
        type: 'breakfast',
        nutrients: {
          calories: 0,
          carbs: 0,
          protein: 0,
          fat: 0
        },
        description: '균형잡힌 한식 위주 식단을 권장합니다.',
        reason: null,
        ingredients: null,
        instructions: null,
        tips: null,
        infoUrl: null,
        category: 'breakfast',
        recommendedTime: 'morning'
      };
      setDetail(defaultDiet);
      setLoading(false);
    }
  }, [diet, location.state]);

  // 태그/카테고리 예시 추출
  const tags = detail?.tags || [detail?.category, detail?.recommendedTime].filter(Boolean);

  if (loading) {
    return (
      <Layout>
        <div className="text-center py-16">로딩 중...</div>
      </Layout>
    );
  }

  if (error || !detail) {
    return (
      <Layout>
        <div className="text-center py-16">{error || '식단 정보를 찾을 수 없습니다.'}</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="min-h-screen bg-gradient-to-br from-green-50 via-emerald-50 to-teal-50 py-16">
        <div className="w-full max-w-4xl mx-auto px-6">
        {/* 헤더 */}
        <div className="mb-8">
          <button 
            onClick={() => navigate(-1)} 
            className="inline-flex items-center gap-2 text-sm text-blue-600 hover:text-blue-700 transition-colors mb-6"
          >
            <span className="text-lg">←</span>
            뒤로가기
          </button>
        </div>

        {/* 식단명 */}
        <div className="mb-8 text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-green-500 to-teal-600 rounded-full mb-4">
            <span className="text-2xl">🍽️</span>
          </div>
          <h1 className="text-3xl font-bold text-gray-800 mb-4">{detail.name}</h1>
          <div className="flex flex-wrap justify-center gap-2 mb-6">
            {tags && tags.map((tag, i) => (
              <span key={i} className="inline-flex items-center gap-1 bg-gradient-to-r from-yellow-100 to-orange-100 text-orange-800 px-3 py-1 rounded-full text-xs font-medium border border-orange-200">
                <span>🏷️</span>
                {tag}
              </span>
            ))}
          </div>
        </div>

        {/* 영양 정보 (2열 배치) */}
        <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8 mb-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* 칼로리 */}
            <div className="flex items-center justify-between p-4 bg-gradient-to-r from-red-50 to-orange-50 rounded-xl border border-red-100">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-r from-red-400 to-orange-500 rounded-full flex items-center justify-center flex-shrink-0">
                  <span className="text-white text-lg">🔥</span>
                </div>
                <span className="text-gray-700 font-medium">칼로리</span>
              </div>
              <span className="text-gray-900 font-semibold">{detail.nutrients?.calories || detail.calories || '-'} kcal</span>
            </div>

            {/* 탄수화물 */}
            <div className="flex items-center justify-between p-4 bg-gradient-to-r from-yellow-50 to-orange-50 rounded-xl border border-yellow-100">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-r from-yellow-400 to-orange-500 rounded-full flex items-center justify-center flex-shrink-0">
                  <span className="text-white text-lg">🍞</span>
                </div>
                <span className="text-gray-700 font-medium">탄수화물</span>
              </div>
              <span className="text-gray-900 font-semibold">{detail.nutrients?.carbs || '-'}g</span>
            </div>

            {/* 단백질 */}
            <div className="flex items-center justify-between p-4 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl border border-blue-100">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-r from-blue-400 to-indigo-500 rounded-full flex items-center justify-center flex-shrink-0">
                  <span className="text-white text-lg">💪</span>
                </div>
                <span className="text-gray-700 font-medium">단백질</span>
              </div>
              <span className="text-gray-900 font-semibold">{detail.nutrients?.protein || '-'}g</span>
            </div>

            {/* 지방 */}
            <div className="flex items-center justify-between p-4 bg-gradient-to-r from-purple-50 to-pink-50 rounded-xl border border-purple-100">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-r from-purple-400 to-pink-500 rounded-full flex items-center justify-center flex-shrink-0">
                  <span className="text-white text-lg">🥑</span>
                </div>
                <span className="text-gray-700 font-medium">지방</span>
              </div>
              <span className="text-gray-900 font-semibold">{detail.nutrients?.fat || '-'}g</span>
            </div>
          </div>
        </div>

        {/* 설명 */}
        <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8 mb-8">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 bg-gradient-to-r from-blue-400 to-indigo-500 rounded-full flex items-center justify-center">
              <span className="text-white text-lg">📝</span>
            </div>
            <h2 className="text-xl font-semibold text-gray-800">설명</h2>
          </div>
          <p className="text-gray-700 leading-relaxed text-left">{detail.description || '균형잡힌 한식 위주 식단을 권장합니다.'}</p>
        </div>

        {/* 추천 이유 */}
        <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8 mb-8">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 bg-gradient-to-r from-green-400 to-emerald-500 rounded-full flex items-center justify-center">
              <span className="text-white text-lg">💡</span>
            </div>
            <h2 className="text-xl font-semibold text-gray-800">추천 이유</h2>
          </div>
          <p className="text-gray-700 leading-relaxed text-left whitespace-pre-line">
            {(detail.reason || '개인 체형과 목표에 맞는 균형잡힌 영양소를 제공합니다.')
              .split(/\.\s+/)
              .filter(s => s.trim().length > 0)
              .map((sentence, idx, arr) => (
                <span key={idx}>
                  {sentence.trim()}
                  {idx < arr.length - 1 ? '.' : (detail.reason || '').trim().endsWith('.') ? '' : '.'}
                  {idx < arr.length - 1 && <br />}
                </span>
              ))}
          </p>
        </div>

        {/* 재료 */}
        {detail.ingredients && (
          <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8 mb-8">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-gradient-to-r from-yellow-400 to-orange-500 rounded-full flex items-center justify-center">
                <span className="text-white text-lg">🥘</span>
              </div>
              <h2 className="text-xl font-semibold text-gray-800">재료</h2>
            </div>
            <ul className="space-y-3">
              {Array.isArray(detail.ingredients)
                ? detail.ingredients.map((ing, idx) => (
                    <li key={idx} className="flex gap-3 items-center">
                      <span className="flex-shrink-0 w-6 h-6 bg-gradient-to-r from-yellow-500 to-orange-600 text-white rounded-full flex items-center justify-center text-xs font-bold">
                        {idx + 1}
                      </span>
                      <span className="text-gray-700 text-left">{ing}</span>
                    </li>
                  ))
                : <li className="text-gray-700 text-left">{detail.ingredients}</li>}
            </ul>
          </div>
        )}

        {/* 조리 방법 */}
        {detail.instructions && (
          <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8 mb-8">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-gradient-to-r from-purple-400 to-pink-500 rounded-full flex items-center justify-center">
                <span className="text-white text-lg">👨‍🍳</span>
              </div>
              <h2 className="text-xl font-semibold text-gray-800">조리 방법</h2>
            </div>
            <div className="space-y-4">
              {Array.isArray(detail.instructions) ? (
                detail.instructions.map((instruction, idx) => (
                  <div key={idx} className="flex gap-4 items-start">
                    <span className="flex-shrink-0 w-8 h-8 bg-gradient-to-r from-purple-500 to-pink-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                      {idx + 1}
                    </span>
                    <p className="text-gray-700 leading-relaxed text-left flex-1">{instruction}</p>
                  </div>
                ))
              ) : (
                <div className="space-y-4">
                  {detail.instructions.split(/\d+\.\s*/).filter(step => step.trim()).map((instruction, idx) => (
                    <div key={idx} className="flex gap-4 items-start">
                      <span className="flex-shrink-0 w-8 h-8 bg-gradient-to-r from-purple-500 to-pink-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                        {idx + 1}
                      </span>
                      <p className="text-gray-700 leading-relaxed text-left flex-1">{instruction.trim()}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {/* 영상 및 링크 */}
        {(detail.videoUrl || detail.infoUrl) && (
          <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8 mb-8">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-gradient-to-r from-red-400 to-pink-500 rounded-full flex items-center justify-center">
                <span className="text-white text-lg">🔗</span>
              </div>
              <h2 className="text-xl font-semibold text-gray-800">참고 자료</h2>
            </div>
            <div className="flex flex-wrap gap-3">
              {detail.videoUrl && (
                <a
                  href={detail.videoUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 bg-gradient-to-r from-red-500 to-pink-600 text-white px-6 py-3 rounded-xl font-medium shadow-lg hover:from-red-600 hover:to-pink-700 transition-all transform hover:scale-[1.02]"
                >
                  <span className="text-lg">🎥</span>
                  조리법 영상 보기
                </a>
              )}
              {detail.infoUrl && (
                <a
                  href={detail.infoUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 bg-gradient-to-r from-blue-500 to-indigo-600 text-white px-6 py-3 rounded-xl font-medium shadow-lg hover:from-blue-600 hover:to-indigo-700 transition-all transform hover:scale-[1.02]"
                >
                  <span className="text-lg">📖</span>
                  음식 소개 보기
                </a>
              )}
            </div>
          </div>
        )}

        {/* 팁 */}
        {detail.tips && (
          <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8 mb-8">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-gradient-to-r from-yellow-400 to-orange-500 rounded-full flex items-center justify-center">
                <span className="text-white text-lg">💡</span>
              </div>
              <h2 className="text-xl font-semibold text-gray-800">팁</h2>
            </div>
            <p className="text-gray-700 leading-relaxed text-left">{detail.tips}</p>
          </div>
        )}
        </div>
      </div>
    </Layout>
  );
}