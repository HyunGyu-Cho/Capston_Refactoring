import { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import Layout from '../components/Layout';
import Button from '../components/Button';
import HeroWithBg from '../components/HeroWithBg';
import SectionWithWave from '../components/SectionWithWave';
import { fetchWorkoutRecommendations } from '../api/workoutRecommendation';
import { fetchDietRecommendations } from '../api/dietRecommendation';
import { saveSurvey } from '../api/survey';
import { getCurrentUserId, useUser } from '../api/auth';
import { getLatestInbodyRecord } from '../api/inbody';

export default function SurveyPage() {
  const { isLoggedIn } = useUser();
  const [inbody, setInbody] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [validationError, setValidationError] = useState(''); // 검증 에러 (인바디 데이터와 무관)
  
  // 체형분석 후 설문 입력이 정상 흐름
  const locationState = useLocation().state;
  const navigate = useNavigate();

  useEffect(() => {
    loadInbodyData();
  }, []);

  const loadInbodyData = async () => {
    try {
      setLoading(true);
      setError('');

      // 1순위: locationState에서 인바디 데이터 가져오기 (체형분석 후 설문 입력)
      if (locationState?.inbody) {
        console.log('📍 locationState에서 인바디 데이터 로드:', locationState.inbody);
        setInbody(locationState.inbody);
        setLoading(false);
        return;
      }

      // 2순위: 백엔드 API에서 최신 인바디 데이터 가져오기
      if (!isLoggedIn) {
        setError('로그인이 필요합니다.');
        navigate('/login');
        return;
      }

      const userId = getCurrentUserId();
      if (!userId) {
        setError('사용자 정보를 찾을 수 없습니다.');
        return;
      }

      const latestInbody = await getLatestInbodyRecord(userId);
      if (latestInbody) {
        console.log('📍 백엔드에서 최신 인바디 데이터 조회 성공:', latestInbody);
        console.log('📍 인바디 데이터 필드들:', Object.keys(latestInbody));
        setInbody(latestInbody);
      } else {
        setError('인바디 데이터가 없습니다. 먼저 인바디 데이터를 입력해주세요.');
      }
    } catch (error) {
      console.error('인바디 데이터 로드 실패:', error);
      setError('인바디 데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };
  
  // 설문 폼 기본값 (새로운 설문 작성용)
  const defaultFormData = {
    survey: "",
    workoutFrequency: "3",
    selectedDays: ["월요일", "수요일", "금요일"],
    mealsPerDay: "3",
    selectedMeals: ["breakfast", "lunch", "dinner"],
    mealLabeling: "generic"
  };
  
  const [survey, setSurvey] = useState(defaultFormData.survey);
  const [workoutFrequency, setWorkoutFrequency] = useState(defaultFormData.workoutFrequency);
  const [selectedDays, setSelectedDays] = useState(defaultFormData.selectedDays);
  const [mealsPerDay, setMealsPerDay] = useState(defaultFormData.mealsPerDay);
  const [selectedMeals, setSelectedMeals] = useState(defaultFormData.selectedMeals);
  const [mealLabeling, setMealLabeling] = useState(defaultFormData.mealLabeling);
  const [submitting, setSubmitting] = useState(false);
  const [progress, setProgress] = useState(0);
  const [progressMessage, setProgressMessage] = useState("");



  // 로딩 상태
  if (loading) {
    return (
      <Layout>
        <div className="text-center py-16">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-xl text-gray-600">인바디 데이터를 불러오는 중...</p>
        </div>
      </Layout>
    );
  }

  // 인바디 데이터가 없는 경우만 에러 화면 표시 (검증 에러는 설문 입력 화면에서 표시)
  if (!inbody) {
    return (
      <Layout>
        <div className="text-center py-16">
          <div className="text-xl text-red-500 mb-4">
            {error || '먼저 인바디 데이터를 입력해 주세요.'}
          </div>
          <Button onClick={() => navigate('/inbody-input')} variant="primary">
            인바디 입력 페이지로 이동
          </Button>
        </div>
      </Layout>
    );
  }

  const handleAnalysis = async (e) => {
    e.preventDefault();
    if (!survey) {
      setError('설문 내용을 입력하세요.');
      return;
    }
    
    // 인바디 데이터 확인
    if (!inbody) {
      setError('인바디 데이터가 없습니다. 먼저 인바디 데이터를 입력해주세요.');
      return;
    }
    
    console.log('🔍 handleAnalysis에서 사용할 인바디 데이터:', inbody);
    
    // 운동 빈도와 선택한 요일 수 검증 (불일치 시 alert 표시 후 제출 차단, 현재 페이지 유지)
    if (selectedDays.length !== parseInt(workoutFrequency)) {
      alert(`주 ${workoutFrequency}일로 설정했는데 ${selectedDays.length}일을 선택했습니다.\n빈도를 조정하거나 요일을 조정해주세요.`);
      setValidationError(`주 ${workoutFrequency}일로 설정했는데 ${selectedDays.length}일을 선택했습니다. 빈도를 조정하거나 요일을 조정해주세요.`);
      return; // 제출 차단 - alert 확인 후에도 현재 페이지에 머무름
    }
    
    // 끼니 수와 선택한 끼니 수 검증 (불일치 시 alert 표시 후 제출 차단, 현재 페이지 유지)
    if (selectedMeals.length > 0 && selectedMeals.length !== parseInt(mealsPerDay)) {
      alert(`하루 ${mealsPerDay}끼로 설정했는데 ${selectedMeals.length}끼를 선택했습니다.\n끼니 수를 조정하거나 선택한 끼니를 조정해주세요.`);
      setValidationError(`하루 ${mealsPerDay}끼로 설정했는데 ${selectedMeals.length}끼를 선택했습니다. 끼니 수를 조정하거나 선택한 끼니를 조정해주세요.`);
      return; // 제출 차단 - alert 확인 후에도 현재 페이지에 머무름
    }
    
    setError("");
    setValidationError(""); // 검증 통과 시 검증 에러 초기화
    setSubmitting(true);
    setProgress(0);
    
    try {
      // 1. 백엔드에 설문조사 저장
      setProgress(5);
      setProgressMessage("설문조사 저장 중...");
      
      const userId = getCurrentUserId();
      if (!userId) {
        setError('로그인이 필요합니다.');
        return;
      }
      
      // 체형분석에서 생성된 인바디 기록 ID 가져오기
      const inbodyRecordId = locationState?.analysisResult?.inbodyRecordId || null;
      
      // 2. 추천 생성용 설문 데이터 준비 (먼저 생성)
      // 한글 요일을 영문으로 매핑
      const dayMap = {
        "월요일": "Monday", "화요일": "Tuesday", "수요일": "Wednesday",
        "목요일": "Thursday", "금요일": "Friday", "토요일": "Saturday", "일요일": "Sunday"
      };
      const selectedDaysEn = selectedDays.map(d => dayMap[d] || d);
      
      // 끼니 생성 명시적 처리
      // selectedMeals가 있으면 항상 우선 사용 (사용자가 선택한 끼니가 최우선)
      const mpd = Number(mealsPerDay);
      const mealsToGenerate = (selectedMeals && selectedMeals.length > 0) ? selectedMeals
        : (mealLabeling === 'byType' ? selectedMeals
          : (mpd === 1 ? ['dinner']
            : mpd === 2 ? ['lunch', 'dinner']
            : mpd === 3 ? ['breakfast', 'lunch', 'dinner']
            : ['breakfast', 'lunch', 'dinner', 'snack']));
      
      // 🔍 디버깅: 선택된 끼니 확인
      console.log('🔍 끼니 선택 정보:', {
        mealLabeling,
        mealsPerDay: mpd,
        selectedMeals,
        mealsToGenerate
      });
      
      const surveyData = {
        text: survey,
        workoutFrequency: Number(workoutFrequency),  // ✅ 숫자화
        selectedDays: selectedDays,                  // 한글 원본 유지
        selectedDaysEn,                              // ✅ 영문 배열 추가
        preferredDays: selectedDaysEn.join(", "),    // ✅ 영문 문장으로 (회수 제거)
        mealsPerDay: mpd,                           // ✅ 숫자화
        mealLabeling: mealLabeling,
        mealsToGenerate,                            // ✅ 항상 끼니 배열 명시
        // generic 모드면 끼니 유형을 보내지 않고, byType일 때만 보냄
        ...(mealLabeling === 'byType' ? {
          selectedMeals: selectedMeals,
          selectedMealsLabel: getSelectedMealsLabel(selectedMeals) + ` (하루 ${selectedMeals.length}끼)`
        } : {})
      };
      
      console.log('추천 생성용 설문 데이터:', surveyData);
      
      // 1. 백엔드에 설문조사 저장 (상세 데이터 포함)
      const saveResult = await saveSurvey(survey, inbodyRecordId, surveyData);
      console.log('✅ 설문조사 저장 완료:', saveResult);
      
      // 운동/식단 추천을 동시에 호출하여 전체 대기 시간을 단축
      setProgress(10);
      setProgressMessage("운동·식단 AI 맞춤 추천 생성 중...");

      // 운동/식단 추천 병렬 요청
      const [workoutData, dietData] = await Promise.all([
        fetchWorkoutRecommendations(inbody, surveyData),
        fetchDietRecommendations(inbody, surveyData),
      ]);
      
      setProgress(80);
      setProgressMessage("결과 준비 중...");
      
      // 3단계: 데이터 통합 (API 응답을 올바른 구조로 변환)
      const workoutResponse = workoutData.data || workoutData;
      const dietResponse = dietData.data || dietData;
      
      // API 응답이 요일별 구조가 아닌 경우, 더미 데이터로 변환
      const processedWorkouts = workoutResponse.workouts || {
        Monday: [{
          name: workoutResponse.programName || "맞춤형 운동",
          description: workoutResponse.mainSets || "개인 체력에 맞는 강도로 운동하세요.",
          duration: 30,
          intensity: "medium",
          calories: 200,
          type: "strength"
        }]
      };
      
      const processedDiets = dietResponse.diets || {
        Monday: {
          breakfast: {
            name: "맞춤형 아침식사",
            description: dietResponse.sampleMenu || "균형잡힌 식단을 권장합니다.",
            calories: 400,
            type: "아침"
          }
        }
      };
      
      const combinedData = {
        workouts: processedWorkouts,
        diets: processedDiets,
        // 운동 프로그램 메타데이터 (백엔드 응답 그대로 최대한 보존)
        programName: workoutResponse.programName || workoutData.programName || '',
        weeklySchedule: workoutResponse.weeklySchedule || workoutData.weeklySchedule || '',
        warmup: workoutResponse.warmup || workoutData.warmup || '',
        mainSets: workoutResponse.mainSets || workoutData.mainSets || '',
        cooldown: workoutResponse.cooldown || workoutData.cooldown || '',
        equipment: workoutResponse.equipment || workoutData.equipment || '',
        targetMuscles: workoutResponse.targetMuscles || workoutData.targetMuscles || '',
        expectedResults: workoutResponse.expectedResults || workoutData.expectedResults || '',
        // 운동/식단 요약 정보
        workoutSummary: workoutResponse.summary || workoutData.summary || '',
        dietSummary: dietResponse.summary || dietData.summary || '',
        fitnessGoals: workoutResponse.fitnessGoals || workoutData.fitnessGoals || [],
        nutritionGoals: dietResponse.nutritionGoals || dietData.nutritionGoals || {},
        weeklyGoals: workoutResponse.weeklyGoals || workoutData.weeklyGoals || {},
        progressTracking: workoutResponse.progressTracking || workoutData.progressTracking || [],
        safetyTips: workoutResponse.safetyTips || workoutData.safetyTips || [],
        // 식단 상위 메타데이터 (백엔드 응답 그대로 최대한 보존)
        mealStyle: dietResponse.mealStyle || dietData.mealStyle || '',
        dailyCalories: dietResponse.dailyCalories || dietData.dailyCalories || null,
        macroSplit: dietResponse.macroSplit || dietData.macroSplit || null,
        sampleMenu: dietResponse.sampleMenu || dietData.sampleMenu || '',
        shoppingList: dietResponse.shoppingList || dietData.shoppingList || [],
        precautions: dietResponse.precautions || dietData.precautions || '',
        mealTiming: dietResponse.mealTiming || dietData.mealTiming || '',
        hydration: dietResponse.hydration || dietData.hydration || '',
        supplements: dietResponse.supplements || dietData.supplements || '',
        // 과거 필드와의 호환성을 위해 그대로 유지
        cookingTips: dietResponse.cookingTips || dietData.cookingTips || [],
        substitutions: dietResponse.substitutions || dietData.substitutions || [],
        hydrationGuide: dietResponse.hydrationGuide || dietData.hydrationGuide || (dietResponse.hydration || dietData.hydration || ''),
        generatedAt: new Date().toISOString()
      };
      
      // sessionStorage에 통합 추천 결과 저장 (다른 페이지에서 재사용)
      // 실제 로그인된 사용자 ID 기반으로 추천 데이터 저장
      const userKey2 = `recommendations_${userId}`;
      sessionStorage.setItem(userKey2, JSON.stringify(combinedData));
      console.log('실제 사용자 ID 기반 추천 데이터 저장:', userKey2, combinedData);
      
      setProgress(100);
      setProgressMessage("완료!");
      
      // 추천 완료된 상태로 페이지 이동
      setTimeout(() => {
        navigate("/recommendations", {
          state: { 
            inbody, 
            survey, 
            recommendations: combinedData
          }
        });
      }, 500); // 완료 메시지를 잠깐 보여준 후 이동
      
    } catch (error) {
      console.error('추천 생성 실패:', error);
      setError('추천 생성 실패: ' + error.message);
      setSubmitting(false);
      setProgress(0);
      setProgressMessage("");
    }
  };

  const handleInbody = () => {
    navigate("/inbody-input");
  };

  // 운동 빈도 변경 시 기본 요일 설정
  const handleFrequencyChange = (frequency) => {
    setWorkoutFrequency(frequency);
    
    // 빈도에 따른 기본 권장 요일 설정
    const defaultDays = {
      "1": ["월요일"],
      "2": ["월요일", "금요일"],
      "3": ["월요일", "수요일", "금요일"],
      "4": ["월요일", "화요일", "목요일", "금요일"],
      "5": ["월요일", "화요일", "수요일", "목요일", "금요일"],
      "6": ["월요일", "화요일", "수요일", "목요일", "금요일", "토요일"],
      "7": ["월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"]
    };
    
    const newSelectedDays = defaultDays[frequency] || defaultDays["3"];
    setSelectedDays(newSelectedDays);
    
    // 빈도 변경 시 요일 수가 일치하면 검증 에러 초기화
    if (newSelectedDays.length === parseInt(frequency)) {
      setValidationError("");
    }
  };

  // 요일 선택/해제 토글
  const toggleDay = (day) => {
    const newSelectedDays = selectedDays.includes(day)
      ? selectedDays.filter(d => d !== day)
      : [...selectedDays, day];
    
    // 요일 선택은 항상 허용 (빈도와 맞지 않아도 경고만 표시)
    if (newSelectedDays.length > parseInt(workoutFrequency)) {
      console.log(`⚠️ 빈도 불일치: 설정 ${workoutFrequency}일, 선택 ${newSelectedDays.length}일 - 선택한 요일을 그대로 사용합니다.`);
    }
    
    // 요일 수가 빈도와 일치하면 검증 에러 초기화
    if (newSelectedDays.length === parseInt(workoutFrequency)) {
      setValidationError("");
    }
    
    setError(""); // 에러 메시지 초기화 (경고는 UI에서 표시)
    setSelectedDays(newSelectedDays);
  };

  // 전체 요일 목록
  const allDays = ["월요일", "화요일", "수요일", "목요일", "금요일"];

  // 끼니 선택 토글
  const toggleMeal = (mealKey) => {
    const newMeals = selectedMeals.includes(mealKey)
      ? selectedMeals.filter(m => m !== mealKey)
      : [...selectedMeals, mealKey];
    
    // 끼니 수가 일치하면 검증 에러 초기화
    if (newMeals.length === parseInt(mealsPerDay)) {
      setValidationError("");
    }
    
    setSelectedMeals(newMeals);
  };

  // 끼니 라벨 매핑
  const mealLabels = {
    breakfast: "아침",
    lunch: "점심",
    dinner: "저녁",
    snack: "간식"
  };

  const getSelectedMealsLabel = (meals) => {
    return meals.map(m => mealLabels[m] || m).join(", ");
  };

  // 운동 빈도별 설명
  const getFrequencyDescription = (frequency) => {
    const descriptions = {
      "1": "전신운동 위주, 60-90분 집중 운동",
      "2": "상체/하체 분할, 45-60분 효율적 운동",
      "3": "Push/Pull/Legs 분할, 45분 균형적 운동",
      "4": "상하체 분할, 40-45분 체계적 운동",
      "5": "부위별 세분화, 35-45분 전문적 운동",
      "6": "고빈도 분할, 30-40분 집중적 운동",
      "7": "매일 다른 부위, 20-35분 지속적 운동"
    };
    return descriptions[frequency] || descriptions["3"];
  };

  return (
    <Layout>
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-purple-50 to-pink-50 py-16">
        <div className="w-full max-w-4xl mx-auto px-6">
          {/* 헤더 */}
          <div className="mb-12 text-center">
            <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full mb-6 shadow-lg">
              <span className="text-3xl">📋</span>
            </div>
            <h1 className="text-4xl font-bold text-gray-800 mb-4">설문 입력</h1>
            <p className="text-gray-600 text-lg max-w-2xl mx-auto">
              운동/식단 목표, 통증, 선호 등 자유롭게 의견을 입력해 주세요
            </p>
          </div>

          {/* 설문 폼 */}
          <div className="bg-white rounded-3xl shadow-xl border border-gray-100 p-8">
            {/* 검증 에러 메시지 표시 */}
            {validationError && (
              <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                <div className="flex items-center gap-2">
                  <span className="text-red-500 text-lg">⚠️</span>
                  <p className="text-red-700 text-sm">{validationError}</p>
                </div>
              </div>
            )}
            <form className="space-y-8">
          {/* 운동 빈도 선택 */}
          <div>
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-gradient-to-r from-orange-400 to-red-500 rounded-full flex items-center justify-center">
                <span className="text-white text-lg">🏋️</span>
              </div>
              <label className="text-xl font-semibold text-gray-800">
                주간 운동 가능 일수
              </label>
            </div>
            <div className="flex flex-wrap gap-2 mb-4">
              {[1, 2, 3, 4, 5].map(num => (
                <button
                  key={num}
                  type="button"
                  onClick={() => handleFrequencyChange(num.toString())}
                  className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                    workoutFrequency === num.toString()
                      ? 'bg-blue-500 text-white'
                      : 'bg-white text-gray-700 border border-gray-300 hover:border-blue-400'
                  }`}
                  disabled={loading}
                >
                  주 {num}일
                </button>
              ))}
            </div>
            
            {/* 요일 선택 영역 */}
            <div className="mb-4">
              <div className="flex items-center gap-2 mb-4 text-gray-600">
                <span className="text-base">📅</span>
                <span className="text-sm">운동할 요일을 선택하세요 ({selectedDays.length}/{workoutFrequency}일 선택됨)</span>
              </div>
              <div className="flex flex-wrap gap-2">
                {allDays.map(day => (
                  <button
                    key={day}
                    type="button"
                    onClick={() => toggleDay(day)}
                    className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                      selectedDays.includes(day)
                        ? 'bg-green-500 text-white'
                        : 'bg-white text-gray-700 border border-gray-300 hover:border-green-400'
                    }`}
                    disabled={loading}
                  >
                    {day}
                  </button>
                ))}
              </div>
            </div>

            <div className="bg-gradient-to-r from-blue-50 to-indigo-50 p-5 rounded-2xl border border-blue-200">
              <div className="flex items-center gap-2 mb-2">
                <div className="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center">
                  <span className="text-white text-xs">📅</span>
                </div>
                <div className="text-sm text-blue-800 font-medium">
                  선택된 운동 일정: {selectedDays.join(", ")} (주 {selectedDays.length}회)
                </div>
              </div>
              <div className="text-sm text-blue-600 ml-8">
                💡 {getFrequencyDescription(workoutFrequency)}
              </div>
              {selectedDays.length !== parseInt(workoutFrequency) && (
                <div className="flex items-center gap-2 mt-2 ml-8">
                  <span className="text-orange-500 text-sm">⚠️</span>
                  <span className="text-sm text-orange-600">
                    설정한 빈도({workoutFrequency}일)와 선택한 요일({selectedDays.length}일)이 다릅니다.
                  </span>
                </div>
              )}
            </div>
          </div>

          {/* 끼니 수/종류 선택 */}
          <div className="mt-8">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-gradient-to-r from-green-400 to-teal-500 rounded-full flex items-center justify-center">
                <span className="text-white text-lg">🍽️</span>
              </div>
              <label className="text-xl font-semibold text-gray-800">
                하루 끼니 수 및 끼니 선택
              </label>
            </div>
            <div className="flex flex-wrap gap-2 mb-4">
              {[1,2,3,4].map(n => (
                <button
                  key={n}
                  type="button"
                  onClick={() => setMealsPerDay(n.toString())}
                  className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                    mealsPerDay === n.toString()
                      ? 'bg-purple-500 text-white'
                      : 'bg-white text-gray-700 border border-gray-300 hover:border-purple-400'
                  }`}
                  disabled={loading}
                >
                  하루 {n}끼
                </button>
              ))}
            </div>

            <div className="flex items-center gap-2 mb-4 text-gray-600">
              <span className="text-base">✅</span>
              <span className="text-sm">생성할 끼니를 선택하세요 ({selectedMeals.length}/{mealsPerDay}끼 선택됨)</span>
            </div>
            <div className="flex flex-wrap gap-2 mb-4">
              {Object.keys(mealLabels).map(key => (
                <button
                  key={key}
                  type="button"
                  onClick={() => toggleMeal(key)}
                  className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                    selectedMeals.includes(key)
                      ? 'bg-purple-500 text-white'
                      : 'bg-white text-gray-700 border border-gray-300 hover:border-purple-400'
                  }`}
                  disabled={loading}
                >
                  {mealLabels[key]}
                </button>
              ))}
            </div>
            <div className="bg-gradient-to-r from-purple-50 to-pink-50 p-5 rounded-2xl border border-purple-200">
              <div className="flex items-center gap-2 mb-2">
                <div className="w-6 h-6 bg-purple-500 rounded-full flex items-center justify-center">
                  <span className="text-white text-xs">🍽️</span>
                </div>
                <div className="text-sm text-purple-800 font-medium">
                  선택된 끼니: {getSelectedMealsLabel(selectedMeals)} (하루 {selectedMeals.length}끼)
                </div>
              </div>
              {selectedMeals.length !== parseInt(mealsPerDay) && (
                <div className="flex items-center gap-2 mt-2 ml-8">
                  <span className="text-orange-500 text-sm">⚠️</span>
                  <span className="text-sm text-orange-600">
                    설정한 끼니 수({mealsPerDay})와 선택된 끼니({selectedMeals.length})가 다릅니다.
                  </span>
                </div>
              )}
            </div>
          </div>

          {/* 설문 입력 */}
          <div>
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 bg-gradient-to-r from-blue-400 to-indigo-500 rounded-full flex items-center justify-center">
                <span className="text-white text-lg">📝</span>
              </div>
              <label className="text-xl font-semibold text-gray-800">
                운동/식단 목표 및 선호사항
              </label>
            </div>
            <textarea
              className="w-full border-2 border-gray-300 rounded-2xl p-5 text-base min-h-[140px] shadow-sm focus:border-blue-500 focus:ring-4 focus:ring-blue-100 transition-all"
              placeholder="예: 허리 통증이 있어요. 하체 위주로 운동하고 싶어요. 매운 음식을 좋아합니다."
              value={survey}
              onChange={e => setSurvey(e.target.value)}
              disabled={loading}
              required
            />
            <div className="flex items-center gap-2 text-gray-500 mt-3">
              <span className="text-base">💬</span>
              <span className="text-sm">원하는 목표, 고민, 선호하는 운동/식단, 알레르기 등을 자유롭게 작성해 주세요.</span>
            </div>
          </div>
          {/* 제출 중일 때 진행상황 표시 */}
          {submitting && (
            <div className="mt-8 p-6 bg-gradient-to-r from-blue-50 to-purple-50 rounded-2xl border border-blue-200">
              <div className="w-full bg-gray-200 rounded-full h-4 mb-4 overflow-hidden">
                <div 
                  className="bg-gradient-to-r from-blue-500 to-purple-600 h-4 rounded-full transition-all duration-1000 ease-out shadow-lg" 
                  style={{ width: `${progress}%` }}
                ></div>
              </div>
              <div className="text-center">
                <div className="flex items-center justify-center gap-3 mb-3">
                  <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
                  <p className="text-lg font-semibold text-blue-700">{progressMessage}</p>
                </div>
                <p className="text-gray-600 mb-2">
                  {progress < 50 ? '🏋️ AI가 당신의 체형에 맞는 운동을 분석하고 있습니다...' : 
                   progress < 80 ? '🍽️ AI가 당신의 목표에 맞는 식단을 생성하고 있습니다...' : 
                   progress < 100 ? '✨ 개인맞춤 프로그램을 완성하고 있습니다...' :
                   '🎉 맞춤 추천이 완료되었습니다!'}
                </p>
                <div className="inline-flex items-center gap-2 bg-blue-100 px-4 py-1 rounded-full">
                  <span className="text-sm font-semibold text-blue-700">{progress}% 완료</span>
                </div>
              </div>
            </div>
          )}

          {/* 버튼들 */}
          <div className="flex flex-col md:flex-row gap-4 mt-10">
            <button
              type="button"
              className="flex-1 flex items-center justify-center gap-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white py-4 rounded-xl text-lg font-semibold shadow-lg hover:from-blue-600 hover:to-purple-700 disabled:opacity-50 transition-all transform hover:scale-[1.02]"
              disabled={submitting}
              onClick={handleAnalysis}
            >
              {submitting ? (
                <>
                  <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  추천 생성 중...
                </>
              ) : (
                <>
                  <span className="text-2xl">🎯</span>
                  맞춤 추천 받기
                </>
              )}
            </button>
            <button
              type="button"
              className="flex-1 flex items-center justify-center gap-3 bg-white text-gray-700 py-4 rounded-xl text-lg font-semibold border border-gray-300 hover:bg-gray-50 disabled:opacity-50 transition-all transform hover:scale-[1.02]"
              onClick={handleInbody}
              disabled={submitting}
            >
              <span className="text-2xl">📏</span>
              인바디 다시 입력
            </button>
          </div>
          {error && (
            <div className="mt-6 p-4 bg-gradient-to-r from-red-50 to-pink-50 border border-red-200 rounded-xl">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 bg-red-500 rounded-full flex items-center justify-center">
                  <span className="text-white text-sm">⚠️</span>
                </div>
                <p className="text-red-700 font-medium">{error}</p>
              </div>
            </div>
          )}
        </form>
          </div>
        </div>
      </div>
    </Layout>
  );
} 