import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import Button from '../components/Button';
import { useState, useEffect } from 'react';
import { ArrowRight } from 'lucide-react';
import HeroWithBg from '../components/HeroWithBg';
import SectionWithWave from '../components/SectionWithWave';
import { INBODY_FEATURES } from '../data/inbody';
import { fetchBodyTypeAnalysis } from '../api/bodyAnalysis';
import { saveInbodyData, getLatestInbodyRecord } from '../api/inbody';
import { getCurrentUserId, useUser } from '../api/auth';

export default function InbodyInputPage() {
  const { setUserData, user, isLoggedIn } = useUser(); // isLoggedIn을 Hook에서 가져오기
  const [form, setForm] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [analysisProgress, setAnalysisProgress] = useState('');
  const [hasHistory, setHasHistory] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  // localStorage 관련 state 제거됨
  const navigate = useNavigate();

  useEffect(() => {
    // 로그인 체크
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }
    
    // 히스토리 확인
    checkHistory();
  }, [navigate, isLoggedIn]);

  // 히스토리 확인 함수
  const checkHistory = async () => {
    try {
      const userId = getCurrentUserId();
      if (!userId) return;
      
      const latestRecord = await getLatestInbodyRecord(userId);
      setHasHistory(!!latestRecord);
    } catch (error) {
      console.error('히스토리 확인 실패:', error);
    }
  };

  // 히스토리 데이터 로드 함수
  const loadHistoryData = async () => {
    try {
      setHistoryLoading(true);
      setError('');
      
      const userId = getCurrentUserId();
      if (!userId) {
        setError('로그인이 필요합니다.');
        return;
      }
      
      const latestRecord = await getLatestInbodyRecord(userId);
      if (latestRecord) {
        // getLatestInbodyRecord에서 이미 성별이 "남성"/"여성"으로 변환되어 옴
        setForm(latestRecord);
        setHasHistory(false); // 로드 완료 후 히스토리 섹션 숨기기
        console.log('✅ 히스토리 데이터 로드 완료:', latestRecord);
      } else {
        setError('히스토리 데이터를 찾을 수 없습니다.');
      }
    } catch (error) {
      console.error('히스토리 로드 실패:', error);
      setError('히스토리 데이터 로드에 실패했습니다.');
    } finally {
      setHistoryLoading(false);
    }
  };

  const handleChange = e => {
    const { name, value } = e.target;
    // 입력값을 그대로 저장해서, 사용자가 0 입력 후 바로 0.1 등으로 이어서 입력할 수 있도록 함
    setForm(prev => ({ ...prev, [name]: value }));
  };

  // sessionStorage 관련 함수들 제거됨

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // 입력 유효성 검사 (0도 유효 값으로 인정)
    for (const feature of INBODY_FEATURES) {
      const v = form[feature.name];
      const isEmpty = v === undefined || v === null || v === '';
      if (isEmpty) {
        setError(`${feature.name}을(를) 입력하세요.`);
        return;
      }
    }
    
    setError('');
    setLoading(true);
    setAnalysisProgress('인바디 데이터 저장 중...');
    
    try {
      // 1. 백엔드에 인바디 데이터 저장
      setAnalysisProgress('💾 인바디 데이터를 서버에 저장하고 있습니다...');
      
      // 실제 로그인된 사용자 ID 사용
      const userId = getCurrentUserId();
      if (!userId) {
        setError('로그인이 필요합니다.');
        return;
      }
      
      const saveResult = await saveInbodyData(form, userId);
      console.log('✅ 인바디 데이터 저장 완료:', saveResult);
      
      // sessionStorage 저장 제거 - 백엔드 데이터베이스만 사용
      
      setAnalysisProgress('🤖 ChatGPT가 체형을 분석하고 있습니다...');
      
      console.log('📤 원본 인바디 데이터:', form);
      
      // ChatGPT API로 체형 분석 실행 (데이터 변환은 API 함수에서 처리)
      const analysisData = await fetchBodyTypeAnalysis(form);
      
      console.log('🔍 ChatGPT 분석 결과:', analysisData);
      
      setAnalysisProgress('분석 결과를 저장하고 있습니다...');
      
      // 분석 결과를 사용자별 데이터로 저장
      if (isLoggedIn) {
        const bodyAnalysisResult = {
          bodyType: analysisData.bodyType,
          summary: analysisData.summary,
          method: 'OpenAI ChatGPT API',
          analyzedAt: new Date().toISOString(),
          inbodySnapshot: form,
          // OpenAI 추가 정보 (있는 경우)
          ...(analysisData.bmiCategory && { bmiCategory: analysisData.bmiCategory }),
          ...(analysisData.bodyFatCategory && { bodyFatCategory: analysisData.bodyFatCategory }),
          ...(analysisData.healthRisk && { healthRisk: analysisData.healthRisk }),
          ...(analysisData.medicalBasis && { medicalBasis: analysisData.medicalBasis }),
          ...(analysisData.muscleBalance && { muscleBalance: analysisData.muscleBalance }),
          ...(analysisData.bodyComposition && { bodyComposition: analysisData.bodyComposition }),
          ...(analysisData.metabolicHealth && { metabolicHealth: analysisData.metabolicHealth }),
          ...(analysisData.inbodyScore && { inbodyScore: analysisData.inbodyScore })
        };
        console.log('💾 저장할 분석 결과:', bodyAnalysisResult);
        setUserData('bodyAnalysis', bodyAnalysisResult);
      }
      
      setAnalysisProgress('✅ 분석 완료! 결과 페이지로 이동합니다...');
      
      // 분석 완료 후 결과 페이지로 자동 이동
      setTimeout(() => {
        const navigationData = { 
          inbody: form,
          analysisResult: analysisData,
          inbodyRecordId: analysisData.inbodyRecordId, // 체형분석에서 생성된 인바디 기록 ID
          justCompleted: true // 방금 분석 완료되었음을 표시
        };
        console.log('🚀 네비게이션 데이터:', navigationData);
        navigate('/body-analysis', { state: navigationData });
      }, 1000);
      
    } catch (err) {
      console.error('체형분석 실패:', err);
      
      // 네트워크 오류인 경우 더 친화적인 메시지 표시
      let errorMessage = err.message;
      if (err.message.includes('Failed to fetch') || err.message.includes('NetworkError')) {
        errorMessage = '백엔드 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.';
      }
      
      setError(`분석 실패: ${errorMessage}`);
      setAnalysisProgress('');
      setLoading(false);
    }
  };

  return (
    <Layout>
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 py-16">
        <div className="w-full max-w-4xl mx-auto px-6">
        {/* 헤더 */}
        <div className="mb-12 text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full mb-4">
            <span className="text-2xl">📏</span>
          </div>
          <h1 className="text-3xl font-bold text-gray-800 mb-3">인바디 데이터 입력</h1>
          <p className="text-gray-500 mb-6">정확한 건강 분석을 위해 인바디 데이터를 입력해 주세요</p>
          <button
            onClick={() => navigate('/inbody-history')}
            className="inline-flex items-center gap-2 text-sm text-blue-600 hover:text-blue-700 transition-colors"
          >
            <span className="text-lg">📊</span>
            인바디 히스토리 보기
          </button>
        </div>
      
        {/* 기존 데이터 로드 옵션 */}
        {hasHistory && (
          <div className="mb-8 p-6 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-2xl border border-blue-200">
            <div className="flex items-center gap-3 mb-3">
              <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                <span className="text-white text-sm">📋</span>
              </div>
              <h3 className="text-lg font-semibold text-gray-800">기존 인바디 데이터가 있습니다</h3>
            </div>
            <p className="text-gray-600 mb-4">
              이전에 입력한 인바디 데이터를 불러와서 다시 분석하시겠습니까?
            </p>
            <div className="flex gap-3">
              <button
                onClick={loadHistoryData}
                disabled={historyLoading}
                className="flex items-center gap-2 px-4 py-2 text-sm bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50 transition-colors"
              >
                {historyLoading ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    로딩 중...
                  </>
                ) : (
                  <>
                    <span>📥</span>
                    기존 데이터 불러오기
                  </>
                )}
              </button>
              <button
                onClick={() => {
                  setForm({});
                  checkHistory();
                }}
                className="flex items-center gap-2 px-4 py-2 text-sm bg-white text-gray-700 rounded-lg hover:bg-gray-50 border border-gray-300 transition-colors"
              >
                <span>✨</span>
                새로 입력하기
              </button>
            </div>
          </div>
        )}
      
        {/* Form Section */}
        <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-blue-500 rounded-full flex items-center justify-center">
              <span className="text-white text-sm">📝</span>
            </div>
            <h2 className="text-xl font-semibold text-gray-800">인바디 데이터 입력</h2>
          </div>
          
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {INBODY_FEATURES.map(feature => (
                <div key={feature.name} className="group">
                  <label className="block mb-2 text-sm font-medium text-gray-700 flex items-center gap-2">
                    <span className="text-lg">📊</span>
                    {feature.name}
                  </label>
                  {feature.type === 'select' ? (
                    <select
                      name={feature.name}
                      value={form[feature.name] ?? ''}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all group-hover:border-gray-400"
                      required
                    >
                      <option value="">선택하세요</option>
                      {feature.options.map(option => (
                        <option key={option} value={option}>{option}</option>
                      ))}
                    </select>
                  ) : (
                    <input
                      name={feature.name}
                      type="number"
                      min={feature.min}
                      max={feature.max}
                      step={feature.step || 1}
                      value={form[feature.name] ?? ''}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all group-hover:border-gray-400"
                      placeholder={`${feature.name}을(를) 입력하세요${feature.unit ? ` (${feature.unit})` : ''}`}
                      required
                    />
                  )}
                </div>
              ))}
            </div>
            
            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white py-4 rounded-xl text-base font-medium hover:from-blue-600 hover:to-purple-700 disabled:opacity-50 transition-all transform hover:scale-[1.02] shadow-lg"
            >
              {loading ? (
                <>
                  <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  분석 중...
                </>
              ) : (
                <>
                  <span className="text-xl">🔍</span>
                  체형 분석하기
                </>
              )}
            </button>
            
            {/* 분석 진행 상태 표시 */}
            {loading && analysisProgress && (
              <div className="mt-6 p-4 bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-xl">
                <div className="flex items-center gap-3">
                  <div className="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center">
                    <span className="text-white text-xs">⚡</span>
                  </div>
                  <p className="text-sm text-blue-700 font-medium">{analysisProgress}</p>
                </div>
              </div>
            )}
          </form>
          
          {error && (
            <div className="mt-6 p-4 bg-red-50 border border-red-200 rounded-xl">
              <div className="flex items-center gap-3">
                <div className="w-6 h-6 bg-red-500 rounded-full flex items-center justify-center">
                  <span className="text-white text-xs">⚠️</span>
                </div>
                <p className="text-sm text-red-700">{error}</p>
              </div>
            </div>
          )}
        </div>
        </div>
      </div>
    </Layout>
  );
}