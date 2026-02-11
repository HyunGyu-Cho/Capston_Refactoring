import Layout from '../components/Layout';
import BackButton from '../components/BackButton';
import { useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import AOS from 'aos';
import 'aos/dist/aos.css';
import { Dumbbell, Flame, Timer, Target, Info, AlertTriangle } from 'lucide-react';

export default function WorkoutDetailPage() {
  const location = useLocation();
  const { workout } = location.state || {};
  const navigate = useNavigate();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    AOS.init({ duration: 800, once: true });
    
    console.log('🔍 WorkoutDetailPage - location.state:', location.state);
    console.log('🔍 WorkoutDetailPage - workout:', workout);
    
    if (workout) {
      console.log('🔍 운동 데이터가 있음:', workout);
      setDetail(workout);
      setLoading(false);
    } else {
      console.log('🔍 운동 데이터가 없음, 기본 데이터 생성');
      // 기본 운동 데이터 생성
      const defaultWorkout = {
        name: '맞춤형 운동 프로그램',
        type: 'strength',
        duration: 30,
        intensity: 'medium',
        calories: 0,
        description: '개인 체력에 맞는 강도로 운동하세요.',
        reason: null,
        part: null,
        targetMuscles: null,
        sets: null,
        reps: null,
        restTime: null,
        steps: null,
        effects: null,
        tips: null,
        caution: null,
        infoUrl: null
      };
      setDetail(defaultWorkout);
      setLoading(false);
    }
  }, [workout, location.state]);

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
        <div className="text-center py-16">{error || '운동 정보를 찾을 수 없습니다.'}</div>
      </Layout>
    );
  }

  // 운동 부위, 난이도, 태그 등 예시 데이터 추출
  const tags = detail?.tags || [detail?.part, detail?.difficulty].filter(Boolean);

  // 운동 타입을 한글로 변환
  const getWorkoutTypeInKorean = (type) => {
    const typeMap = {
      'Strength': '근력',
      'Hypertrophy': '근비대',
      'Endurance': '지구력',
      'Cardio': '유산소',
      'Compound': '복합',
      'Isolation': '고립',
      'Circuit': '서킷',
      'Recovery': '회복'
    };
    return typeMap[type] || type;
  };

  // 운동 강도를 한글로 변환
  const getIntensityInKorean = (intensity) => {
    const intensityMap = {
      'Low': '저강도',
      'Medium': '중강도',
      'High': '고강도',
      'Very High': '매우 고강도'
    };
    return intensityMap[intensity] || intensity;
  };

  return (
    <Layout>
      <div className="min-h-screen bg-gradient-to-br from-orange-50 via-amber-50 to-yellow-50 py-16">
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

        {/* 운동명 */}
        <div className="mb-8 text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-orange-500 to-red-600 rounded-full mb-4">
            <span className="text-2xl">🏋️</span>
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

        {/* 운동 정보 (2열 배치) */}
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
              <span className="text-gray-900 font-semibold">{detail.calories || '-'} kcal</span>
            </div>

            {/* 시간 */}
            <div className="flex items-center justify-between p-4 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl border border-blue-100">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-r from-blue-400 to-indigo-500 rounded-full flex items-center justify-center flex-shrink-0">
                  <span className="text-white text-lg">⏱️</span>
                </div>
                <span className="text-gray-700 font-medium">시간</span>
              </div>
              <span className="text-gray-900 font-semibold">{detail.duration || '-'}분</span>
            </div>

            {/* 난이도 */}
            <div className="flex items-center justify-between p-4 bg-gradient-to-r from-green-50 to-emerald-50 rounded-xl border border-green-100">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-r from-green-400 to-emerald-500 rounded-full flex items-center justify-center flex-shrink-0">
                  <span className="text-white text-lg">🎯</span>
                </div>
                <span className="text-gray-700 font-medium">난이도</span>
              </div>
              <span className="text-gray-900 font-semibold">{getIntensityInKorean(detail.intensity) || '-'}</span>
            </div>

            {/* 운동 부위 */}
            <div className="flex items-center justify-between p-4 bg-gradient-to-r from-purple-50 to-violet-50 rounded-xl border border-purple-100">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-r from-purple-400 to-violet-500 rounded-full flex items-center justify-center flex-shrink-0">
                  <span className="text-white text-lg">💪</span>
                </div>
                <span className="text-gray-700 font-medium">운동 부위</span>
              </div>
              <span className="text-gray-900 font-semibold text-right">{detail.targetMuscles ? detail.targetMuscles.join(', ') : detail.part || '-'}</span>
            </div>

            {/* 운동 타입 */}
            {detail.type && (
              <div className="flex items-center justify-between p-4 bg-gradient-to-r from-yellow-50 to-orange-50 rounded-xl border border-yellow-100">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-gradient-to-r from-yellow-400 to-orange-500 rounded-full flex items-center justify-center flex-shrink-0">
                    <span className="text-white text-lg">🏷️</span>
                  </div>
                  <span className="text-gray-700 font-medium">운동 타입</span>
                </div>
                <span className="text-gray-900 font-semibold">{getWorkoutTypeInKorean(detail.type)}</span>
              </div>
            )}

            {/* 세트 */}
            {detail.sets && (
              <div className="flex items-center justify-between p-4 bg-gradient-to-r from-blue-50 to-cyan-50 rounded-xl border border-blue-100">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-gradient-to-r from-blue-400 to-cyan-500 rounded-full flex items-center justify-center flex-shrink-0">
                    <span className="text-white text-lg">📊</span>
                  </div>
                  <span className="text-gray-700 font-medium">세트</span>
                </div>
                <span className="text-gray-900 font-semibold">{detail.sets}세트</span>
              </div>
            )}

            {/* 횟수 */}
            {detail.reps && (
              <div className="flex items-center justify-between p-4 bg-gradient-to-r from-green-50 to-teal-50 rounded-xl border border-green-100">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-gradient-to-r from-green-400 to-teal-500 rounded-full flex items-center justify-center flex-shrink-0">
                    <span className="text-white text-lg">🔢</span>
                  </div>
                  <span className="text-gray-700 font-medium">횟수</span>
                </div>
                <span className="text-gray-900 font-semibold">{detail.reps}</span>
              </div>
            )}

            {/* 휴식 시간 */}
            {detail.restTime && (
              <div className="flex items-center justify-between p-4 bg-gradient-to-r from-purple-50 to-pink-50 rounded-xl border border-purple-100">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-gradient-to-r from-purple-400 to-pink-500 rounded-full flex items-center justify-center flex-shrink-0">
                    <span className="text-white text-lg">⏸️</span>
                  </div>
                  <span className="text-gray-700 font-medium">휴식 시간</span>
                </div>
                <span className="text-gray-900 font-semibold">{detail.restTime}</span>
              </div>
            )}
          </div>
        </div>

        {/* 운동 방법 */}
        <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8 mb-8">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 bg-gradient-to-r from-blue-400 to-indigo-500 rounded-full flex items-center justify-center">
              <span className="text-white text-lg">📝</span>
            </div>
            <h2 className="text-xl font-semibold text-gray-800">운동 방법</h2>
          </div>
          {Array.isArray(detail.steps) ? (
            <ul className="space-y-4">
              {detail.steps.map((step, idx) => (
                <li key={idx} className="flex gap-4 items-start">
                  <span className="flex-shrink-0 w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-600 text-white rounded-full flex items-center justify-center font-bold">
                    {idx + 1}
                  </span>
                  <span className="text-gray-700 flex-1 pt-2 leading-relaxed text-left">{step}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-gray-700 leading-relaxed text-left">{detail.description || '개인 체력에 맞는 강도로 운동하세요.'}</p>
          )}
          
          {/* 영상 및 링크 */}
          {(detail.videoUrl || detail.infoUrl) && (
            <div className="mt-6 flex flex-wrap gap-3">
              {detail.videoUrl && (
                <a
                  href={detail.videoUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 bg-gradient-to-r from-red-500 to-pink-600 text-white px-6 py-3 rounded-xl font-medium shadow-lg hover:from-red-600 hover:to-pink-700 transition-all transform hover:scale-[1.02]"
                >
                  <span className="text-lg">🎥</span>
                  운동 영상 보기
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
                  운동 소개 보기
                </a>
              )}
            </div>
          )}
        </div>

        {/* 추천 이유 */}
        {detail.reason && (
          <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8 mb-8">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-gradient-to-r from-green-400 to-emerald-500 rounded-full flex items-center justify-center">
                <span className="text-white text-lg">💡</span>
              </div>
              <h2 className="text-xl font-semibold text-gray-800">추천 이유</h2>
            </div>
            <p className="text-gray-700 leading-relaxed text-left whitespace-pre-line">
              {detail.reason
                .split(/\.\s+/)
                .filter(s => s.trim().length > 0)
                .map((sentence, idx, arr) => (
                  <span key={idx}>
                    {sentence.trim()}
                    {idx < arr.length - 1 ? '.' : detail.reason.trim().endsWith('.') ? '' : '.'}
                    {idx < arr.length - 1 && <br />}
                  </span>
                ))}
            </p>
          </div>
        )}
        {/* 효과 */}
        <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8 mb-8">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 bg-gradient-to-r from-orange-400 to-red-500 rounded-full flex items-center justify-center">
              <span className="text-white text-lg">✨</span>
            </div>
            <h2 className="text-xl font-semibold text-gray-800">효과</h2>
          </div>
          {detail.effects?.length > 0 ? (
            <div className="space-y-4">
              {detail.effects.map((eff, idx) => {
                // 문자열인 경우와 객체인 경우를 모두 처리
                const isString = typeof eff === 'string';
                return (
                  <div key={idx} className="flex gap-4 items-start">
                    <span className="flex-shrink-0 w-10 h-10 bg-gradient-to-r from-orange-500 to-yellow-600 text-white rounded-full flex items-center justify-center font-bold">
                      {idx + 1}
                    </span>
                    <div className="flex-1 pt-2">
                      {isString ? (
                        <p className="text-gray-700 leading-relaxed text-left">{eff}</p>
                      ) : (
                        <>
                          {eff.img && (
                            <img
                              src={eff.img}
                              alt={eff.title}
                              className="w-20 h-20 object-cover rounded-xl border mb-2"
                            />
                          )}
                          <h3 className="font-semibold mb-1 text-gray-800 text-left">{eff.title}</h3>
                          <p className="text-gray-600 leading-relaxed text-left">{eff.desc}</p>
                        </>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="text-center py-8">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl">✨</span>
              </div>
              <p className="text-gray-500">이 운동의 효과 정보를 준비 중입니다</p>
              <p className="text-gray-400 text-sm mt-2">곧 더 자세한 정보를 제공할 예정입니다</p>
            </div>
          )}
        </div>

        {/* 팁 & 주의사항 */}
        {(detail.tips || detail.caution) && (
          <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8 mb-8">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-gradient-to-r from-yellow-400 to-orange-500 rounded-full flex items-center justify-center">
                <span className="text-white text-lg">⚠️</span>
              </div>
              <h2 className="text-xl font-semibold text-gray-800">팁 & 주의사항</h2>
            </div>
            <div className="space-y-4">
              {detail.tips && (
                <div className="p-4 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl">
                  <div className="flex items-start gap-3">
                    <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center flex-shrink-0 mt-1">
                      <span className="text-white text-sm">💡</span>
                    </div>
                    <p className="text-gray-700 leading-relaxed flex-1 pt-1 text-left">{detail.tips}</p>
                  </div>
                </div>
              )}
              {detail.caution && (
                <div className="p-4 bg-gradient-to-r from-red-50 to-pink-50 rounded-xl">
                  <div className="flex items-start gap-3">
                    <div className="w-8 h-8 bg-red-500 rounded-full flex items-center justify-center flex-shrink-0 mt-1">
                      <span className="text-white text-sm">⚠️</span>
                    </div>
                    <p className="text-red-700 font-medium leading-relaxed flex-1 pt-1 text-left">{detail.caution}</p>
                  </div>
                </div>
              )}
              {detail.cautionImg && (
                <img
                  src={detail.cautionImg}
                  alt="주의사항"
                  className="w-full rounded-xl mt-4"
                />
              )}
            </div>
          </div>
        )}
        </div>
      </div>
    </Layout>
  );
}