import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import Card from '../components/Card';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import HeroWithBg from '../components/HeroWithBg';
import SectionWithWave from '../components/SectionWithWave';
import WorkoutIllustration from '../components/WorkoutIllustration';
import { useUser, getUserData } from '../api/auth';
import { storageManager } from '../utils/storageManager';
import { getExerciseInfo, EXERCISE_TYPES } from '../utils/exerciseUtils';

const days = [
  { key: 'Monday', label: '월요일' },
  { key: 'Tuesday', label: '화요일' },
  { key: 'Wednesday', label: '수요일' },
  { key: 'Thursday', label: '목요일' },
  { key: 'Friday', label: '금요일' }
];

// 온점(.) 기준으로 줄바꿈하는 유틸리티 함수
const formatTextWithLineBreaks = (text) => {
  if (!text) return '';
  // 온점과 공백으로 문장을 분리
  const sentences = text.split(/\.\s+/).filter(s => s.trim().length > 0);
  
  if (sentences.length === 0) return text;
  
  return sentences.map((sentence, index) => {
    const trimmed = sentence.trim();
    // 마지막 문장이 아니면 온점을 추가하고 줄바꿈
    if (index < sentences.length - 1) {
      return <React.Fragment key={index}>{trimmed}.<br /></React.Fragment>;
    }
    // 마지막 문장은 원본 텍스트가 온점으로 끝나는지 확인
    const originalEndsWithDot = text.trim().endsWith('.');
    return <React.Fragment key={index}>{trimmed}{originalEndsWithDot ? '.' : ''}</React.Fragment>;
  });
};

export default function RecommendedWorkoutListPage() {
  const { inbody: locationInbody, survey: locationSurvey, workouts: passedWorkouts, fromRecommendations, fromHistory, workoutData } = useLocation().state || {};
  const [selectedDay, setSelectedDay] = useState(days[0].key);

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
  const [workouts, setWorkouts] = useState(passedWorkouts || {});
  const [loading, setLoading] = useState(!passedWorkouts); // 데이터 있으면 로딩 안함
  const [error, setError] = useState('');
  const [bodyType, setBodyType] = useState("");
  const [summary, setSummary] = useState("");
  const [workoutMeta, setWorkoutMeta] = useState(null); // 프로그램 요약 메타데이터
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [selectedDays, setSelectedDays] = useState([]);

  // currentUser ID 기반으로 인바디 데이터 조회
  const getInbodyData = () => {
    if (locationInbody) return locationInbody;
    return getUserData('inbody');
  };

  // currentUser ID 기반으로 설문 데이터 조회
  const getSurveyData = () => {
    if (locationSurvey) return locationSurvey;
    return getUserData('survey');
  };

  useEffect(() => {
    const inbody = getInbodyData();
    const survey = getSurveyData();
    
    // 설문 데이터에서 선택된 요일 추출
    if (survey && survey.selectedDaysEn) {
      setSelectedDays(survey.selectedDaysEn);
    } else if (survey && survey.selectedDays) {
      // 한글 요일을 영문으로 변환
      const dayMap = {
        "월요일": "Monday", "화요일": "Tuesday", "수요일": "Wednesday",
        "목요일": "Thursday", "금요일": "Friday", "토요일": "Saturday", "일요일": "Sunday",
        "월": "Monday", "화": "Tuesday", "수": "Wednesday",
        "목": "Thursday", "금": "Friday", "토": "Saturday", "일": "Sunday"
      };
      const englishDays = survey.selectedDays.map(day => dayMap[day] || day);
      setSelectedDays(englishDays);
    }
    
    // 1순위: 히스토리에서 전달받은 운동 데이터가 있으면 사용
    if (fromHistory && workoutData) {
      console.log('히스토리에서 전달받은 운동 데이터 사용:', workoutData);
      
      // 히스토리 데이터를 캐시된 데이터와 동일한 구조로 변환
      let historyWorkouts = {};
      
      // workouts 필드가 있으면 그대로 사용 (캐시된 데이터와 동일한 구조)
      if (workoutData.workouts && typeof workoutData.workouts === 'object') {
        historyWorkouts = workoutData.workouts;
      } else {
        // 단일 프로그램 구조인 경우 요일별로 변환
        const workoutProgram = {
          name: workoutData.programName || '운동 프로그램',
          description: workoutData.mainSets || workoutData.description || '개인 체력에 맞는 강도로 운동하세요.',
          duration: 30,
          intensity: 'medium',
          calories: 200,
          type: 'strength',
          weeklySchedule: workoutData.weeklySchedule,
          targetMuscles: workoutData.targetMuscles,
          equipment: workoutData.equipment,
          warmup: workoutData.warmup,
          cooldown: workoutData.cooldown,
          caution: workoutData.caution,
          expectedResults: workoutData.expectedResults
        };
        
        // 모든 요일에 동일한 프로그램 할당
        days.forEach(day => {
          historyWorkouts[day.key] = [workoutProgram];
        });
      }
      
      setWorkouts(historyWorkouts);
      setBodyType(workoutData.bodyType || '체형 분석 완료');
      setSummary(workoutData.summary || '개인 맞춤 운동 추천이 준비되었습니다.');
      setWorkoutMeta({
        programName: workoutData.programName,
        weeklySchedule: workoutData.weeklySchedule,
        warmup: workoutData.warmup,
        mainSets: workoutData.mainSets,
        cooldown: workoutData.cooldown,
        equipment: workoutData.equipment,
        targetMuscles: workoutData.targetMuscles,
        expectedResults: workoutData.expectedResults,
      });
      
      // 이미지 로딩
      loadWorkoutImages(historyWorkouts);
      setLoading(false);
      return;
    }

    // 2순위: 추천에서 전달받은 운동 데이터가 있으면 사용 (인바디/설문 체크보다 우선)
    if (fromRecommendations && passedWorkouts) {
      console.log('추천에서 전달받은 운동 데이터 사용:', passedWorkouts);
      console.log('🔍 fromRecommendations 데이터 구조 분석:', {
        hasWorkouts: !!passedWorkouts,
        isObject: typeof passedWorkouts === 'object',
        keys: Object.keys(passedWorkouts || {})
      });
      
      // workouts가 요일별 구조가 아닌 경우 변환
      let workoutsPayload = passedWorkouts;
      if (!workoutsPayload.Monday && !workoutsPayload.Tuesday && !workoutsPayload.Wednesday && !workoutsPayload.Thursday && !workoutsPayload.Friday) {
        // 단일 프로그램 구조인 경우 요일별로 변환
        const workoutProgram = {
          name: workoutData?.programName || '운동 프로그램',
          description: workoutData?.mainSets || workoutData?.description || '개인 체력에 맞는 강도로 운동하세요.',
          duration: 30,
          intensity: 'medium',
          calories: 200,
          type: 'strength',
          weeklySchedule: workoutData?.weeklySchedule,
          targetMuscles: workoutData?.targetMuscles,
          equipment: workoutData?.equipment,
          warmup: workoutData?.warmup,
          cooldown: workoutData?.cooldown,
          caution: workoutData?.caution,
          expectedResults: workoutData?.expectedResults
        };
        
        // 모든 요일에 동일한 프로그램 할당
        workoutsPayload = {};
        days.forEach(day => {
          workoutsPayload[day.key] = [workoutProgram];
        });
      }
      
      setWorkouts(workoutsPayload);
      setBodyType('체형 분석 완료');
      setSummary('개인 맞춤 운동 추천이 준비되었습니다.');
      if (workoutData) {
        setWorkoutMeta({
          programName: workoutData.programName,
          weeklySchedule: workoutData.weeklySchedule,
          warmup: workoutData.warmup,
          mainSets: workoutData.mainSets,
          cooldown: workoutData.cooldown,
          equipment: workoutData.equipment,
          targetMuscles: workoutData.targetMuscles,
          expectedResults: workoutData.expectedResults,
        });
      }
      
      // 이미지 로딩
      loadWorkoutImages(workoutData);
      setLoading(false);
      return;
    }

    // 3순위: 이미 전달받은 운동 데이터가 있으면 사용
    if (passedWorkouts && Object.keys(passedWorkouts).length > 0) {
      console.log('전달받은 운동 데이터 사용:', passedWorkouts);
      setWorkouts(passedWorkouts);
      setBodyType('');
      setSummary('');
      
      // 이미지 로딩
      loadWorkoutImages(passedWorkouts);
      setLoading(false);
      return;
    }

    // inbody와 survey 데이터가 없으면 리턴 (추천 데이터가 없는 경우에만)
    if (!inbody || !survey) return;

    // 3순위: sessionStorage에서 캐시된 데이터 확인 (currentUser ID 기반)
    try {
      // currentUser ID 기반으로 조회
      const currentUser = storageManager.getItem('currentUser');
      if (currentUser) {
        const userKey = `recommendations_${currentUser.id}`;
        const cachedData = storageManager.getItem(userKey);
        if (cachedData) {
          if (cachedData.workouts && Object.keys(cachedData.workouts).length > 0) {
            console.log('currentUser ID 기반 캐시된 운동 데이터 사용:', userKey, cachedData.workouts);
            setWorkouts(cachedData.workouts);
            setWorkoutMeta({
              programName: cachedData.programName,
              weeklySchedule: cachedData.weeklySchedule,
              warmup: cachedData.warmup,
              mainSets: cachedData.mainSets,
              cooldown: cachedData.cooldown,
              equipment: cachedData.equipment,
              targetMuscles: cachedData.targetMuscles,
              expectedResults: cachedData.expectedResults,
            });
            
            // 체형 분석 결과도 함께 조회
            const bodyAnalysisKey = `bodyAnalysis_${currentUser.id}`;
            const bodyAnalysisData = storageManager.getItem(bodyAnalysisKey);
            if (bodyAnalysisData) {
              setBodyType(bodyAnalysisData.bodyType || '체형 분석 완료');
              setSummary(bodyAnalysisData.summary || '개인 맞춤 운동 추천이 준비되었습니다.');
            } else {
              setBodyType('체형 분석 완료');
              setSummary('개인 맞춤 운동 추천이 준비되었습니다.');
            }
            
            setLoading(false);
            return;
          }
        }
      }
      
      // 기본 키로 fallback
      const cachedData = storageManager.getItem('recommendations');
      if (cachedData) {
        if (cachedData.workouts && Object.keys(cachedData.workouts).length > 0) {
          console.log('기본 키 캐시된 운동 데이터 사용:', cachedData.workouts);
          setWorkouts(cachedData.workouts);
          setWorkoutMeta({
            programName: cachedData.programName,
            weeklySchedule: cachedData.weeklySchedule,
            warmup: cachedData.warmup,
            mainSets: cachedData.mainSets,
            cooldown: cachedData.cooldown,
            equipment: cachedData.equipment,
            targetMuscles: cachedData.targetMuscles,
            expectedResults: cachedData.expectedResults,
          });
          
          // 체형 분석 결과도 함께 조회 (기본 키)
          const bodyAnalysisData = storageManager.getItem('bodyAnalysis');
          if (bodyAnalysisData) {
            setBodyType(bodyAnalysisData.bodyType || '체형 분석 완료');
            setSummary(bodyAnalysisData.summary || '개인 맞춤 운동 추천이 준비되었습니다.');
          } else {
            setBodyType('체형 분석 완료');
            setSummary('개인 맞춤 운동 추천이 준비되었습니다.');
          }
          
          setLoading(false);
          return;
        }
      }
    } catch (e) {
      console.error('캐시된 운동 데이터 로드 실패:', e);
    }

    // 추천 데이터가 없으면 설문 페이지로 안내 (새 추천 생성 안함)
    console.log('📝 운동 추천 데이터가 없습니다. 설문 페이지에서 추천을 받아주세요.');
    setError('운동 추천 데이터가 없습니다. 설문조사를 완료하여 추천을 받아주세요.');
    setLoading(false);
  }, [navigate, passedWorkouts]);

  // 운동별 이미지는 외부 사진 대신 일러스트로 표시하므로
  // 기존 Unsplash 기반 이미지 로딩 함수는 더 이상 사용하지 않음
  const loadWorkoutImages = () => {};


  // 데이터 조회
  const inbody = getInbodyData();
  const survey = getSurveyData();
  
  // recommendations 데이터가 있는지 먼저 확인
  let hasRecommendations = false;
  try {
    // currentUser ID 기반으로 조회
    const currentUser = storageManager.getItem('currentUser');
    if (currentUser) {
      const userKey = `recommendations_${currentUser.id}`;
      const cachedData = storageManager.getItem(userKey);
      if (cachedData && cachedData.workouts && Object.keys(cachedData.workouts).length > 0) {
        hasRecommendations = true;
      }
    }
    
    // 기본 키로도 확인
    if (!hasRecommendations) {
      const cachedData = storageManager.getItem('recommendations');
      if (cachedData && cachedData.workouts && Object.keys(cachedData.workouts).length > 0) {
        hasRecommendations = true;
      }
    }
  } catch (e) {
    console.error('추천 데이터 확인 실패:', e);
  }
  
  // 네비게이션으로 직접 전달된 추천 데이터도 인정 (설문/마이페이지에서 바로 온 경우)
  if (!hasRecommendations && fromRecommendations && passedWorkouts) {
    hasRecommendations = true;
  }
  
  // 통합 히스토리에서 특정 추천을 클릭해 들어온 경우도 추천 데이터가 있다고 간주
  if (!hasRecommendations && fromHistory && workoutData) {
    hasRecommendations = true;
  }
  
  // 추천 데이터가 없고, 인바디/설문 데이터도 없을 때만 에러 표시
  if (!hasRecommendations && (!inbody || !survey)) {
    return (
      <Layout>
        <div className="min-h-screen bg-gradient-to-br from-orange-50 via-red-50 to-pink-50 py-16">
          <div className="w-full max-w-4xl mx-auto px-6">
            <div className="text-center py-16">
              <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-r from-orange-500 to-red-600 rounded-full mb-6">
                <span className="text-3xl">🏋️</span>
              </div>
              <h2 className="text-2xl font-bold text-gray-800 mb-4">운동 추천 데이터가 없습니다</h2>
              <p className="text-gray-600 mb-8 max-w-md mx-auto">
                맞춤형 운동 추천을 받으려면 인바디 데이터와 설문 데이터가 필요합니다.
              </p>
              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                {!inbody && (
                  <button 
                    onClick={() => navigate('/inbody-input')} 
                    className="bg-gradient-to-r from-blue-500 to-blue-600 text-white px-6 py-3 rounded-lg hover:from-blue-600 hover:to-blue-700 transition font-medium"
                  >
                    📊 인바디 입력하기
                  </button>
                )}
                {!survey && (
                  <button 
                    onClick={() => navigate('/survey')} 
                    className="bg-gradient-to-r from-green-500 to-green-600 text-white px-6 py-3 rounded-lg hover:from-green-600 hover:to-green-700 transition font-medium"
                  >
                    📝 설문조사 하기
                  </button>
                )}
                <button 
                  onClick={() => navigate('/')} 
                  className="bg-white text-gray-600 border border-gray-300 px-6 py-3 rounded-lg hover:bg-gray-50 transition font-medium"
                >
                  🏠 메인으로 돌아가기
                </button>
              </div>
            </div>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="min-h-screen bg-gradient-to-br from-orange-50 via-red-50 to-pink-50 py-16">
        <div className="w-full max-w-4xl mx-auto px-6">
        {/* 헤더 */}
        <div className="mb-12 text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-orange-500 to-red-600 rounded-full mb-4">
            <span className="text-2xl">🏋️</span>
          </div>
          <h1 className="text-3xl font-bold text-gray-800 mb-3">추천 운동 루틴</h1>
          <p className="text-gray-500 mb-6">AI가 분석한 체형·목표를 바탕으로 맞춤형 운동 루틴을 제안합니다</p>
          <div className="flex items-center justify-center gap-4">
            <button 
              onClick={() => navigate(-1)} 
              className="inline-flex items-center gap-2 text-sm text-blue-600 hover:text-blue-700 transition-colors"
            >
              <span className="text-lg">←</span>
              뒤로가기
            </button>
            {bodyType && (
              <div className="inline-flex items-center gap-2 bg-gradient-to-r from-blue-100 to-indigo-100 text-blue-800 px-4 py-2 rounded-full text-sm font-medium border border-blue-200">
                <span className="text-lg">📊</span>
                체형 분류: {bodyType}
              </div>
            )}
          </div>
        </div>

        {/* 운동 프로그램 요약 메타데이터 */}
        {workoutMeta && (
          <div className="mb-10 space-y-4">
            <div className="grid md:grid-cols-3 gap-4">
              <div className="bg-white/80 rounded-2xl shadow-sm border border-gray-100 p-4 text-left">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-lg">🏋️</span>
                  <span className="text-sm font-semibold text-gray-700">프로그램 명</span>
                </div>
                <p className="text-sm text-gray-700 leading-relaxed">
                  {workoutMeta.programName || '맞춤형 운동 프로그램'}
                </p>
                {workoutMeta.expectedResults && (
                  <p className="mt-2 text-xs text-gray-500 leading-relaxed">
                    {formatTextWithLineBreaks(workoutMeta.expectedResults)}
                  </p>
                )}
              </div>
              <div className="bg-white/80 rounded-2xl shadow-sm border border-gray-100 p-4 text-left">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-lg">📅</span>
                  <span className="text-sm font-semibold text-gray-700">주간 스케줄</span>
                </div>
                <p className="text-sm text-gray-700 leading-relaxed">
                  {formatTextWithLineBreaks(workoutMeta.weeklySchedule || '주 2~4회, 무리되지 않는 범위에서 진행하세요.')}
                </p>
              </div>
              <div className="bg-white/80 rounded-2xl shadow-sm border border-gray-100 p-4 text-left">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-lg">🎯</span>
                  <span className="text-sm font-semibold text-gray-700">주요 타겟 근육</span>
                </div>
                <p className="text-sm text-gray-700 leading-relaxed">
                  {Array.isArray(workoutMeta.targetMuscles)
                    ? workoutMeta.targetMuscles.join(', ')
                    : workoutMeta.targetMuscles || '상체, 하체, 코어를 균형 있게 자극합니다.'}
                </p>
                {workoutMeta.equipment && (
                  <p className="mt-2 text-xs text-gray-500 leading-relaxed">
                    사용 기구: {formatTextWithLineBreaks(
                      Array.isArray(workoutMeta.equipment)
                        ? workoutMeta.equipment.join(', ')
                        : workoutMeta.equipment
                    )}
                  </p>
                )}
              </div>
            </div>

            {(workoutMeta.warmup || workoutMeta.cooldown || workoutMeta.mainSets) && (
              <div className="grid md:grid-cols-3 gap-4">
                {workoutMeta.warmup && (
                  <div className="bg-blue-50 rounded-2xl border border-blue-100 p-4 text-left">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-lg">🔥</span>
                      <span className="text-sm font-semibold text-blue-800">워밍업</span>
                    </div>
                    <p className="text-xs text-blue-800 leading-relaxed">
                      {formatTextWithLineBreaks(workoutMeta.warmup)}
                    </p>
                  </div>
                )}
                {workoutMeta.mainSets && (
                  <div className="bg-emerald-50 rounded-2xl border border-emerald-100 p-4 text-left">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-lg">💪</span>
                      <span className="text-sm font-semibold text-emerald-800">메인 운동 구성</span>
                    </div>
                    <p className="text-xs text-emerald-800 leading-relaxed">
                      {formatTextWithLineBreaks(workoutMeta.mainSets)}
                    </p>
                  </div>
                )}
                {workoutMeta.cooldown && (
                  <div className="bg-purple-50 rounded-2xl border border-purple-100 p-4 text-left">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-lg">🧘‍♂️</span>
                      <span className="text-sm font-semibold text-purple-800">마무리 스트레칭</span>
                    </div>
                    <p className="text-xs text-purple-800 leading-relaxed">
                      {formatTextWithLineBreaks(workoutMeta.cooldown)}
                    </p>
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {/* 요일 선택 버튼 */}
        <div className="flex justify-center mb-8">
          <div className="flex gap-3 bg-white rounded-2xl shadow-lg border border-gray-100 p-2">
            {days.map(d => (
              <button
                key={d.key}
                onClick={() => setSelectedDay(d.key)}
                className={`flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-xl transition-all ${
                  d.key === selectedDay 
                    ? 'bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-lg transform scale-105' 
                    : 'text-gray-700 hover:bg-gray-100 hover:scale-105'
                }`}
              >
                <span className="text-lg">
                  {d.key === 'Monday' ? '🏃' : 
                   d.key === 'Tuesday' ? '💪' : 
                   d.key === 'Wednesday' ? '🏋️' : 
                   d.key === 'Thursday' ? '🚴' : 
                   d.key === 'Friday' ? '🏊' : '🎯'}
                </span>
                {d.label}
              </button>
            ))}
          </div>
        </div>

        <div className="grid md:grid-cols-2 gap-6">
          {loading ? (
            <div className="col-span-2 flex items-center justify-center py-16">
              <div className="text-center">
                <div className="w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                <p className="text-gray-600 font-medium">운동 루틴을 불러오는 중...</p>
              </div>
            </div>
          ) : error ? (
            <div className="col-span-2 flex items-center justify-center py-16">
              <div className="text-center">
                <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <span className="text-2xl">⚠️</span>
                </div>
                <p className="text-red-600 font-medium">{error}</p>
              </div>
            </div>
          ) : (
            <>
              {/* 운동 부족 경고 메시지 */}
              {(() => {
                const dayLabels = {
                  'Monday': '월요일', 'Tuesday': '화요일', 'Wednesday': '수요일',
                  'Thursday': '목요일', 'Friday': '금요일'
                };
                const currentDayLabel = dayLabels[selectedDay] || selectedDay;
                
                // 선택되지 않은 날짜인 경우
                if (selectedDays.length > 0 && !selectedDays.includes(selectedDay)) {
                  return (
                    <div className="col-span-2 bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
                      <div className="flex items-center">
                        <div className="flex-shrink-0">
                          <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                          </svg>
                        </div>
                        <div className="ml-3">
                          <p className="text-sm text-blue-800">
                            <strong>{currentDayLabel}은 선택된 운동 요일이 아닙니다.</strong> 
                            설문조사에서 선택한 요일만 운동 추천을 받을 수 있습니다.
                          </p>
                        </div>
                      </div>
                    </div>
                  );
                }
                
                // 선택된 날짜이지만 운동이 부족한 경우
                if ((workouts[selectedDay]?.length || 0) < 3) {
                  return (
                    <div className="col-span-2 bg-amber-50 border border-amber-200 rounded-lg p-4 mb-4">
                      <div className="flex items-center">
                        <div className="flex-shrink-0">
                          <svg className="h-5 w-5 text-amber-400" viewBox="0 0 20 20" fill="currentColor">
                            <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                          </svg>
                        </div>
                        <div className="ml-3">
                          <p className="text-sm text-amber-800">
                            <strong>운동이 부족합니다.</strong> {currentDayLabel}에 운동이 {(workouts[selectedDay]?.length || 0)}개만 있습니다. 
                            새로고침하거나 재추천을 받아보세요.
                          </p>
                        </div>
                      </div>
                    </div>
                  );
                }
                
                return null;
              })()}
              
                   {(workouts[selectedDay] || []).map((w, idx) => {
                   // 고유 ID 생성 (운동 이름 + 인덱스)
                   const workoutId = `${w.name.replace(/\s+/g, '-').toLowerCase()}-${idx}`;
                   
                   return (
                     <div key={workoutId} className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden hover:shadow-xl transition-all transform hover:scale-[1.02]">
                       <div className="relative">
                         <WorkoutIllustration
                           category={w.exerciseCategory || w.type}
                           index={idx}
                           label={getExerciseInfo(w.exerciseCategory || w.type)?.displayName || w.name}
                         />
                         <div className="absolute top-3 right-3 bg-white bg-opacity-90 rounded-full px-3 py-1">
                           <span className="text-xs font-medium text-gray-700">#{idx + 1}</span>
                         </div>
                       </div>
                       
                       <div className="p-6">
                         <div className="flex items-center gap-2 mb-3">
                           <div className="w-8 h-8 bg-gradient-to-r from-orange-400 to-red-500 rounded-full flex items-center justify-center">
                             <span className="text-white text-sm">🏋️</span>
                           </div>
                           <h3 className="text-lg font-semibold text-gray-800">{w.name}</h3>
                         </div>
                         
                         {/* 세트/횟수 정보 표시 */}
                         {(w.sets || w.reps) && (
                           <div className="flex flex-wrap gap-2 mb-4">
                             {w.sets && (
                               <span className="inline-flex items-center gap-1 bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-xs font-medium">
                                 <span>📊</span>
                                 {w.sets}세트
                               </span>
                             )}
                             {w.reps && (
                               <span className="inline-flex items-center gap-1 bg-green-100 text-green-800 px-3 py-1 rounded-full text-xs font-medium">
                                 <span>🔢</span>
                                 {w.reps}
                               </span>
                             )}
                             {w.restTime && (
                               <span className="inline-flex items-center gap-1 bg-purple-100 text-purple-800 px-3 py-1 rounded-full text-xs font-medium">
                                 <span>⏰</span>
                                 휴식 {w.restTime}
                               </span>
                             )}
                           </div>
                         )}
                           
                           {/* 운동 타입 및 종류 표시 */}
                           <div className="mb-4 flex flex-wrap gap-2">
                             {w.type && (
                               <span className="inline-flex items-center gap-1 bg-gradient-to-r from-yellow-100 to-orange-100 text-orange-800 px-3 py-1 rounded-full text-xs font-medium border border-orange-200">
                                 <span>🏷️</span>
                                 {getWorkoutTypeInKorean(w.type)}
                               </span>
                             )}
                             {w.exerciseCategory && (() => {
                               const exerciseInfo = getExerciseInfo(w.exerciseCategory);
                               return (
                                 <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium ${exerciseInfo.color}`}>
                                   <span>{exerciseInfo.emoji}</span>
                                   {exerciseInfo.displayName}
                                 </span>
                               );
                             })()}
                           </div>
                         
                         <Link 
                           to={`/workout-detail/${workoutId}`} 
                           state={{ workout: w, workoutId }} 
                           className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-blue-500 to-purple-600 text-white py-3 rounded-xl text-sm font-medium hover:from-blue-600 hover:to-purple-700 transition-all transform hover:scale-[1.02] shadow-lg"
                         >
                           <span className="text-lg">👁️</span>
                           상세보기
                         </Link>
                       </div>
                     </div>
                   );
                 })}
            </>
          )}
        </div>
        </div>
      </div>
    </Layout>
  );
}