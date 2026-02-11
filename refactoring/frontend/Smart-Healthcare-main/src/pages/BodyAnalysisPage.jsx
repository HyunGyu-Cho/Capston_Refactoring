// src/pages/BodyAnalysisPage.jsx
import { useLocation, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import Button from '../components/Button';
import { BarChart2, CheckCircle2 } from 'lucide-react';
import HeroWithBg from '../components/HeroWithBg';
import SectionWithWave from '../components/SectionWithWave';
import { useEffect, useState, useRef } from 'react';
import { fetchBodyTypeAnalysis, getLatestBodyAnalysis } from '../api/bodyAnalysis';
import { getLatestInbodyRecord } from '../api/inbody';
import { useUser, getCurrentUserId } from '../api/auth';
import { storageManager } from '../utils/storageManager';
import { getBodyTypeInfo } from '../utils/bodyTypeUtils';

export default function BodyAnalysisPage() {
  const locationState = useLocation().state;
  const navigate = useNavigate();
  const { user, setUserData, isLoggedIn } = useUser();
  const [loading, setLoading] = useState(true);
  const [bodyType, setBodyType] = useState('');
  const [summary, setSummary] = useState('');
  const [error, setError] = useState('');
  const [analysisMethod, setAnalysisMethod] = useState('OpenAI'); // 'OpenAI' 또는 'Legacy'
  const [analysisDetails, setAnalysisDetails] = useState(null); // OpenAI 분석 세부 정보
  const [inbody, setInbody] = useState(null); // 인바디 데이터를 state로 관리
  const alertShownRef = useRef(false);

  // 텍스트 포맷터: "1. ..." / "- ..." 앞에 줄바꿈을 자동 추가해 가독성 향상
  const formatMultiline = (text) => {
    if (!text) return '';
    let t = String(text);
    // 숫자 목록 (1. 2. 3.)처럼 "숫자. " 패턴일 때만 줄바꿈 추가 (소수점 24.0 등은 건드리지 않음)
    t = t.replace(/(?<!\n)(\d+\.\s+)/g, '\n$1');
    // 하이픈/불릿 목록 (- 항목, • 항목)
    t = t.replace(/(?<!\n)([-•]\s+)/g, '\n$1');
    return t.trim();
  };

  // 인바디 데이터 조회 함수 (백엔드 API 우선)
  const getInbodyData = async () => {
    // 1) location state에서 전달된 데이터
    if (locationState?.inbody) {
      console.log('✅ location state에서 인바디 데이터 찾음');
      return locationState.inbody;
    }
    
    // 2) 백엔드 API로 최신 인바디 데이터 조회 (우선순위)
    try {
      const userId = getCurrentUserId();
      if (userId) {
        console.log('🔍 백엔드 API로 인바디 데이터 조회 시작: userId=', userId);
        const backendInbody = await getLatestInbodyRecord(userId);
        if (backendInbody) {
          console.log('✅ 백엔드에서 인바디 데이터 찾음:', backendInbody);
          return backendInbody;
        }
      }
    } catch (e) {
      console.error('❌ 백엔드 인바디 데이터 조회 실패:', e);
    }
    
    // 3) sessionStorage에서 currentUser ID 기반 조회 (백업)
    try {
      const currentUser = storageManager.getItem('currentUser');
      if (currentUser) {
        const userKey = `inbody_${currentUser.id}`;
        const storedInbody = storageManager.getItem(userKey);
        if (storedInbody) {
          console.log('✅ sessionStorage에서 인바디 데이터 찾음:', userKey);
          return storedInbody;
        }
      }
    } catch (e) {
      console.error('❌ sessionStorage 인바디 데이터 조회 실패:', e);
    }
    
    console.log('❌ 인바디 데이터를 찾을 수 없음');
    return null;
  };

  useEffect(() => {
    const loadData = async () => {
      // 인바디 데이터 조회 (비동기)
      const inbodyData = await getInbodyData();
      setInbody(inbodyData);
      
      // 인바디 데이터만 필요 (설문 데이터는 체형분석에 불필요)
      if (!inbodyData) {
        if (!alertShownRef.current) {
          alert('인바디 데이터를 입력해 주세요!');
          alertShownRef.current = true;
        }
        navigate('/inbody-input');
        return;
      }

      // InbodyInputPage에서 분석 완료 후 넘어온 경우 또는 히스토리에서 넘어온 경우
      const { analysisResult, justCompleted, fromHistory, analysisData, forceReanalyze } = locationState || {};
      
      console.log('BodyAnalysisPage - locationState:', locationState);
      console.log('BodyAnalysisPage - analysisResult:', analysisResult);
      console.log('BodyAnalysisPage - justCompleted:', justCompleted);
      console.log('BodyAnalysisPage - fromHistory:', fromHistory);
      console.log('BodyAnalysisPage - analysisData:', analysisData);
      
      if (forceReanalyze) {
        // 강제 재분석: 인바디 데이터로 즉시 분석 수행
        try {
          setLoading(true);
          const analysisData = await fetchBodyTypeAnalysis(inbodyData);
          const details = {
            healthRisk: analysisData.healthRisk || '알 수 없음',
            muscleBalance: analysisData.muscleBalance || '근육 균형 정보가 없습니다.',
            bodyComposition: analysisData.bodyComposition || '체성분 정보가 없습니다.',
            metabolicHealth: analysisData.metabolicHealth || '대사 건강 정보가 없습니다.',
            reasoning: analysisData.reasoning || '분석 근거가 없습니다.',
            tips: analysisData.tips || '추천 사항이 없습니다.',
            bmiCategory: analysisData.bmiCategory || null,
            bodyFatCategory: analysisData.bodyFatCategory || null,
            visceralFatCategory: analysisData.visceralFatCategory || null,
            inbodyScore: analysisData.inbodyScore || null
          };
          setAnalysisDetails(details);
          setAnalysisMethod('OpenAI');
          setBodyType(analysisData.label || analysisData.bodyType || '균형형');
          setSummary(analysisData.summary || '체형 분석이 완료되었습니다.');
          setLoading(false);
          return;
        } catch (e) {
          console.error('❌ 강제 재분석 실패:', e);
          // 실패 시 기존 분기로 폴백
        }
      }

      if (justCompleted && analysisResult) {
        // 분석 결과가 함께 전달된 경우
        console.log('✅ 분석 결과 데이터 설정:', analysisResult);
        
        // analysisResult에서 필요한 정보 추출하여 analysisDetails 구성
        const details = {
          healthRisk: analysisResult.healthRisk || '알 수 없음',
          muscleBalance: analysisResult.muscleBalance || '근육 균형 정보가 없습니다.',
          bodyComposition: analysisResult.bodyComposition || '체성분 정보가 없습니다.',
          metabolicHealth: analysisResult.metabolicHealth || '대사 건강 정보가 없습니다.',
          reasoning: analysisResult.reasoning || '분석 근거가 없습니다.',
          tips: analysisResult.tips || '추천 사항이 없습니다.',
          bmiCategory: analysisResult.bmiCategory || null,
          bodyFatCategory: analysisResult.bodyFatCategory || null,
          visceralFatCategory: analysisResult.visceralFatCategory || null,
          inbodyScore: analysisResult.inbodyScore || null
        };
        setAnalysisDetails(details);
        setAnalysisMethod('OpenAI');
        setBodyType(analysisResult.label || analysisResult.bodyType || '균형형');
        setSummary(analysisResult.summary || '체형 분석이 완료되었습니다.');
        setLoading(false);
      } else if (fromHistory && analysisData) {
        // 히스토리에서 특정 분석 결과를 클릭한 경우
        console.log('✅ 히스토리에서 전달된 분석 데이터 설정:', analysisData);
        
        // 히스토리 데이터에서 필요한 정보 추출하여 analysisDetails 구성
        const details = {
          healthRisk: analysisData.healthRisk || '알 수 없음',
          muscleBalance: analysisData.muscleBalance || '근육 균형 정보가 없습니다.',
          bodyComposition: analysisData.bodyComposition || '체성분 정보가 없습니다.',
          metabolicHealth: analysisData.metabolicHealth || '대사 건강 정보가 없습니다.',
          reasoning: analysisData.reasoning || '분석 근거가 없습니다.',
          tips: analysisData.tips || '추천 사항이 없습니다.',
          bmiCategory: analysisData.bmiCategory || null,
          bodyFatCategory: analysisData.bodyFatCategory || null,
          visceralFatCategory: analysisData.visceralFatCategory || null,
          inbodyScore: analysisData.inbodyScore || null
        };
        setAnalysisDetails(details);
        setAnalysisMethod(analysisData.analysisMethod || analysisData.method === 'OpenAI ChatGPT API' ? 'OpenAI' : 'Legacy');
        setBodyType(analysisData.label || analysisData.bodyType || '균형형');
        setSummary(analysisData.summary || '체형 분석이 완료되었습니다.');
        setLoading(false);
      } else {
        // 백엔드 API로 기존 분석 결과 조회 (우선순위)
        let existingAnalysis = null;
        try {
          const userId = getCurrentUserId();
          if (userId) {
            console.log('🔍 백엔드 API로 체형 분석 데이터 조회 시작: userId=', userId);
            existingAnalysis = await getLatestBodyAnalysis(userId);
            if (existingAnalysis) {
              console.log('✅ 백엔드에서 분석 결과 찾음:', existingAnalysis);
            }
          }
        } catch (e) {
          console.error('❌ 백엔드 분석 결과 조회 실패:', e);
        }
        
        // sessionStorage에서 조회 (백업)
        if (!existingAnalysis) {
          try {
            const currentUser = storageManager.getItem('currentUser');
            if (currentUser) {
              const userKey = `bodyAnalysis_${currentUser.id}`;
              const storedAnalysis = storageManager.getItem(userKey);
              if (storedAnalysis) {
                existingAnalysis = storedAnalysis;
                console.log('✅ sessionStorage에서 분석 결과 찾음:', existingAnalysis);
              }
            }
          } catch (e) {
            console.error('❌ sessionStorage 분석 결과 조회 실패:', e);
          }
        }
        
        if (existingAnalysis) {
          console.log('✅ 기존 분석 데이터 사용:', existingAnalysis);
          
          // 기존 분석 결과도 동일하게 처리
        const details = {
          healthRisk: existingAnalysis.healthRisk || '알 수 없음',
          muscleBalance: existingAnalysis.muscleBalance || '근육 균형 정보가 없습니다.',
          bodyComposition: existingAnalysis.bodyComposition || '체성분 정보가 없습니다.',
          metabolicHealth: existingAnalysis.metabolicHealth || '대사 건강 정보가 없습니다.',
          reasoning: existingAnalysis.reasoning || '분석 근거가 없습니다.',
          tips: existingAnalysis.tips || '추천 사항이 없습니다.',
          bmiCategory: existingAnalysis.bmiCategory || null,
          bodyFatCategory: existingAnalysis.bodyFatCategory || null,
          visceralFatCategory: existingAnalysis.visceralFatCategory || null,
          inbodyScore: existingAnalysis.inbodyScore || null
        };
          setAnalysisDetails(details);
          setAnalysisMethod(existingAnalysis.method === 'OpenAI ChatGPT API' ? 'OpenAI' : 'Legacy');
          setBodyType(existingAnalysis.label || existingAnalysis.bodyType || '균형형');
          setSummary(existingAnalysis.summary || '체형 분석이 완료되었습니다.');
        } else {
          console.log('❌ 분석 데이터 없음');
        }
        setLoading(false);
      }
    };
    
    loadData();
  }, [navigate, locationState]);

  // 재분석 시도
  const handleRetryAnalysis = async () => {
    try {
      setLoading(true);
      setError('');
      
      console.log('🔄 재분석 시작:', inbody);
      
      // OpenAI ChatGPT API로 체형 분석 수행
      const analysisData = await fetchBodyTypeAnalysis(inbody);
      
      console.log('✅ 재분석 결과:', analysisData);
      
      // 재분석 결과도 동일하게 처리
      const details = {
        healthRisk: analysisData.healthRisk || '알 수 없음',
        muscleBalance: analysisData.muscleBalance || '근육 균형 정보가 없습니다.',
        bodyComposition: analysisData.bodyComposition || '체성분 정보가 없습니다.',
        metabolicHealth: analysisData.metabolicHealth || '대사 건강 정보가 없습니다.',
        reasoning: analysisData.reasoning || '분석 근거가 없습니다.',
        tips: analysisData.tips || '추천 사항이 없습니다.'
      };
      setAnalysisDetails(details);
      setAnalysisMethod('OpenAI');
      setBodyType(analysisData.label || analysisData.bodyType || '균형형');
      setSummary(analysisData.summary || '체형 분석이 완료되었습니다.');

      // 분석 결과를 사용자별 데이터로 저장
      if (isLoggedIn) {
        const bodyAnalysisResult = {
          bodyType: analysisData.bodyType,
          summary: analysisData.summary,
          method: 'OpenAI ChatGPT API',
          analyzedAt: new Date().toISOString(),
          inbodySnapshot: inbody,
          // OpenAI 추가 정보
          ...(analysisData.bmiCategory && { bmiCategory: analysisData.bmiCategory }),
          ...(analysisData.bodyFatCategory && { bodyFatCategory: analysisData.bodyFatCategory }),
          ...(analysisData.healthRisk && { healthRisk: analysisData.healthRisk }),
          ...(analysisData.medicalBasis && { medicalBasis: analysisData.medicalBasis }),
          ...(analysisData.muscleBalance && { muscleBalance: analysisData.muscleBalance }),
          ...(analysisData.bodyComposition && { bodyComposition: analysisData.bodyComposition }),
          ...(analysisData.metabolicHealth && { metabolicHealth: analysisData.metabolicHealth }),
          ...(analysisData.inbodyScore && { inbodyScore: analysisData.inbodyScore })
        };
        setUserData('bodyAnalysis', bodyAnalysisResult);
      }
      
    } catch (err) {
      console.error('재분석 실패:', err);
      setError(`분석 실패: ${err.message}`);
      setBodyType('');
      setSummary('');
    } finally {
      setLoading(false);
    }
  };



  if (loading) {
    return (
      <Layout>
        <div className="w-full max-w-4xl mx-auto mt-24 p-6 bg-white min-h-screen">
          <div className="text-center py-16 text-gray-600">로딩 중...</div>
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="w-full max-w-4xl mx-auto mt-24 p-6 bg-white min-h-screen">
          <div className="text-center py-16">
            <div className="text-lg text-red-500 mb-4">분석 중 오류가 발생했습니다.</div>
            <div className="text-sm text-gray-600 mb-8">{error}</div>
            <button onClick={() => navigate('/inbody')} className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600">
              다시 시도하기
            </button>
          </div>
        </div>
      </Layout>
    );
  }

  // 체형분석 페이지에서는 추천 데이터 표시 없음

   return (
     <Layout>
       <div className="w-full max-w-4xl mx-auto mt-24 p-6 bg-white min-h-screen">
         {bodyType ? (
           // 분석 완료 상태: 결과 표시
           <div className="text-center">
             <div className="mb-8">
               <h1 className="text-3xl font-bold text-gray-800 mb-4">체형 분석 결과</h1>
               <div className="text-2xl font-bold text-blue-600 mb-4">
                 {bodyType}
               </div>
               <span className="inline-flex items-center gap-2 text-green-600 text-sm mb-6 font-medium">
                 <CheckCircle2 className="w-4 h-4" /> 분석 완료
               </span>
             </div>
             
            <div className="bg-gray-50 rounded-lg p-6 mb-8">
               <h3 className="text-lg font-semibold text-gray-700 mb-4 text-left">분석 요약</h3>
               <p className="text-gray-600 leading-relaxed whitespace-pre-line text-left">
                 {formatMultiline(summary)}
               </p>
             </div>

             {/* OpenAI 분석 추가 정보 */}
             {analysisMethod === 'OpenAI' && analysisDetails && (
               <div className="space-y-6">
                 {/* 핵심 지표 한눈에 보기 */}
                 {(analysisDetails.bmiCategory || analysisDetails.bodyFatCategory || analysisDetails.visceralFatCategory || analysisDetails.inbodyScore) && (
                   <div className="bg-white rounded-lg p-6 border border-gray-100">
                     <h4 className="text-lg font-semibold text-gray-800 mb-4">
                       📊 핵심 지표 요약
                     </h4>
                     <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-gray-700">
                       {analysisDetails.inbodyScore && (
                         <div className="flex items-center justify-between bg-blue-50 rounded-md px-3 py-2">
                           <span className="font-medium">인바디 점수</span>
                           <span className="font-semibold text-blue-700">{analysisDetails.inbodyScore}점</span>
                         </div>
                       )}
                       {analysisDetails.bmiCategory && (
                         <div className="flex items-center justify-between bg-green-50 rounded-md px-3 py-2">
                           <span className="font-medium">BMI 분류</span>
                           <span className="font-semibold text-green-700">{analysisDetails.bmiCategory}</span>
                         </div>
                       )}
                       {analysisDetails.bodyFatCategory && (
                         <div className="flex items-center justify-between bg-purple-50 rounded-md px-3 py-2">
                           <span className="font-medium">체지방률 분류</span>
                           <span className="font-semibold text-purple-700">{analysisDetails.bodyFatCategory}</span>
                         </div>
                       )}
                       {analysisDetails.visceralFatCategory && (
                         <div className="flex items-center justify-between bg-red-50 rounded-md px-3 py-2">
                           <span className="font-medium">내장지방/복부 비만</span>
                           <span className="font-semibold text-red-700">{analysisDetails.visceralFatCategory}</span>
                         </div>
                       )}
                     </div>
                   </div>
                 )}

                 {/* 추천 사항 */}
                 {analysisDetails.tips && (
                  <div className="bg-blue-50 rounded-lg p-6">
                    <h4 className="text-lg font-semibold text-gray-800 mb-3 text-left">
                       💡 맞춤 추천 사항
                     </h4>
                     <p className="text-gray-600 leading-relaxed whitespace-pre-line text-left">
                       {formatMultiline(analysisDetails.tips)}
                     </p>
                   </div>
                 )}

                 {/* 분석 근거 */}
                 {analysisDetails.reasoning && (
                  <div className="bg-gray-50 rounded-lg p-6">
                    <h4 className="text-lg font-semibold text-gray-800 mb-3 text-left">
                       🔍 분석 근거
                     </h4>
                     <p className="text-gray-600 leading-relaxed whitespace-pre-line text-left">
                       {formatMultiline(analysisDetails.reasoning)}
                     </p>
                   </div>
                 )}

                 {/* 상세 분석 정보 - 4개 섹션 2열 배치 */}
                 <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {analysisDetails.healthRisk && (
                    <div className="bg-red-50 rounded-lg p-6">
                      <h4 className="text-lg font-semibold text-gray-800 mb-3 text-left">
                        ⚕️ 건강 위험도
                      </h4>
                      {(() => {
                        const raw = (analysisDetails.healthRisk || '').trim();
                        const simpleLevels = ['낮음', '보통', '높음'];
                        const isSimple = simpleLevels.includes(raw);

                        const level = raw.startsWith('낮음') ? 'low'
                                      : raw.startsWith('보통') ? 'medium'
                                      : raw.startsWith('높음') ? 'high'
                                      : 'unknown';
                        const colorClass =
                          level === 'low' ? 'text-green-600' :
                          level === 'medium' ? 'text-yellow-600' :
                          level === 'high' ? 'text-red-600' :
                          'text-gray-600';

                        let text = raw;
                        if (isSimple) {
                          const parts = [];
                          if (analysisDetails.bmiCategory) {
                            parts.push(`BMI 기준으로는 ${analysisDetails.bmiCategory}에 해당합니다.`);
                          }
                          if (analysisDetails.bodyFatCategory) {
                            parts.push(`체지방률은 ${analysisDetails.bodyFatCategory} 수준입니다.`);
                          }
                          if (analysisDetails.visceralFatCategory) {
                            parts.push(`내장지방/복부 비만 지표는 ${analysisDetails.visceralFatCategory} 상태입니다.`);
                          }
                          if (analysisDetails.inbodyScore) {
                            parts.push(`인바디 점수는 ${analysisDetails.inbodyScore}점으로 평가됩니다.`);
                          }
                          if (level === 'low') {
                            parts.push('전반적으로 대사 질환 위험도는 낮은 편이지만, 현재의 생활 습관을 유지하면서 복부 지방과 체중이 과도하게 증가하지 않도록 관리하는 것이 좋습니다.');
                          } else if (level === 'medium') {
                            parts.push('현재는 큰 이상은 아니지만, 복부 지방과 체중 관리에 신경 쓰지 않으면 장기적으로 대사 질환 위험이 높아질 수 있으므로 식단·운동 습관을 조금 더 개선하는 것이 좋습니다.');
                          } else if (level === 'high') {
                            parts.push('체중, 체지방률, 내장지방 지표가 모두 높아 대사 질환(당뇨, 고혈압, 심혈관 질환) 위험이 증가한 상태이므로 체중 감량과 복부 지방 감소를 목표로 적극적인 관리가 필요합니다.');
                          }
                          text = `${raw} - ${parts.join(' ')}`;
                        }

                        return (
                          <p className={`${colorClass} leading-relaxed whitespace-pre-line text-left`}>
                            {formatMultiline(text)}
                          </p>
                        );
                      })()}
                    </div>
                  )}
                   {analysisDetails.muscleBalance && (
                     <div className="bg-green-50 rounded-lg p-6">
                       <h4 className="text-lg font-semibold text-gray-800 mb-3 text-left">
                         🏋️ 근육 균형도
                       </h4>
                       <p className="text-gray-600 leading-relaxed whitespace-pre-line text-left">
                         {formatMultiline(analysisDetails.muscleBalance)}
                       </p>
                     </div>
                   )}
                   {analysisDetails.bodyComposition && (
                     <div className="bg-purple-50 rounded-lg p-6">
                       <h4 className="text-lg font-semibold text-gray-800 mb-3 text-left">
                         🧪 체성분 분석
                       </h4>
                       <p className="text-gray-600 leading-relaxed whitespace-pre-line text-left">
                         {formatMultiline(analysisDetails.bodyComposition)}
                       </p>
                     </div>
                   )}
                   {analysisDetails.metabolicHealth && (
                     <div className="bg-orange-50 rounded-lg p-6">
                       <h4 className="text-lg font-semibold text-gray-800 mb-3 text-left">
                         🔥 대사 건강도
                       </h4>
                       <p className="text-gray-600 leading-relaxed whitespace-pre-line text-left">
                         {formatMultiline(analysisDetails.metabolicHealth)}
                       </p>
                     </div>
                   )}
                 </div>
               </div>
             )}

             {/* 다음 단계 안내 */}
             <div className="mt-12 bg-blue-50 rounded-lg p-8">
               <div className="text-center mb-8">
                 <h3 className="text-2xl font-bold text-gray-800 mb-4">다음 단계: 맞춤 추천을 위한 설문조사</h3>
                 <p className="text-gray-600 max-w-2xl mx-auto">
                   체형분석이 완료되었습니다! 운동 경험, 목표, 선호도 등을 알려주시면 AI가 당신만을 위한 운동·식단을 추천해드립니다.
                 </p>
               </div>
               
               <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                 {/* 설문 후 추천받기 */}
                 <div className="bg-white rounded-lg p-6 border border-gray-200">
                   <h4 className="text-lg font-semibold text-gray-800 mb-3">개인 맞춤 추천받기</h4>
                   <p className="text-gray-600 mb-6">
                     5분 설문으로 운동 경험, 목표, 선호도를 알려주시면 AI가 당신만의 운동·식단을 설계해드립니다!
                   </p>
                   <button 
                     onClick={() => {
                       console.log('설문 페이지로 이동 시도, inbody:', inbody);
                       navigate('/survey');
                     }} 
                     className="w-full bg-blue-500 text-white py-3 px-6 rounded-lg hover:bg-blue-600 transition-colors"
                   >
                     설문하고 맞춤 추천받기
                   </button>
                 </div>
                 
                 {/* 나중에 하기 옵션 */}
                 <div className="bg-white rounded-lg p-6 border border-gray-200">
                   <h4 className="text-lg font-semibold text-gray-800 mb-3">나중에 추천받기</h4>
                   <p className="text-gray-600 mb-6">
                     지금은 체형분석 결과만 확인하고 추천은 나중에 받고 싶다면
                   </p>
                   <button 
                     onClick={() => navigate('/')} 
                     className="w-full bg-gray-500 text-white py-3 px-6 rounded-lg hover:bg-gray-600 transition-colors"
                   >
                     메인으로 돌아가기
                   </button>
                 </div>
               </div>
             </div>
           </div>
         ) : (
           // 분석 정보가 없는 경우: 재분석 옵션 제공
           <div className="text-center py-16">
             <div className="mb-8">
               <h1 className="text-3xl font-bold text-gray-800 mb-4">체형 분석</h1>
               <p className="text-gray-600 mb-8">
                 {inbody ? '체형 분석을 다시 시도해보세요.' : '체형 분석을 위해 인바디 데이터를 먼저 입력해주세요.'}
               </p>
             </div>
             
             <div className="space-y-4">
               {inbody ? (
                <button
                  onClick={() => navigate('/inbody-input')}
                  disabled={loading}
                  className="bg-blue-500 text-white px-8 py-3 rounded-lg hover:bg-blue-600 disabled:opacity-50 transition-colors"
                >
                  인바디 다시 입력하고 재분석하기
                </button>
               ) : (
                 <button
                   onClick={() => navigate('/inbody')}
                   className="bg-blue-500 text-white px-8 py-3 rounded-lg hover:bg-blue-600 transition-colors"
                 >
                   인바디 데이터 입력하기
                 </button>
               )}
             </div>
           </div>
         )}
       </div>
     </Layout>
   );
}