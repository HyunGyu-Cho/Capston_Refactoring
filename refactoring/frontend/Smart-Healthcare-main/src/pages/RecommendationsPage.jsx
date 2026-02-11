import { useLocation, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import Button from '../components/Button';
import { Dumbbell, UtensilsCrossed, CheckCircle2, ArrowRight } from 'lucide-react';
import HeroWithBg from '../components/HeroWithBg';
import { useEffect, useState } from 'react';
import { useUser, getUserData } from '../api/auth';
import { storageManager } from '../utils/storageManager';

export default function RecommendationsPage() {
  const { user: currentUser, isLoggedIn } = useUser();
  const { inbody: locationInbody, survey: locationSurvey, recommendations } = useLocation().state || {};
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(!recommendations); // 데이터 있으면 로딩 안함
  const [workouts, setWorkouts] = useState(recommendations?.workouts || {});
  const [diets, setDiets] = useState(recommendations?.diets || {});
  const [bodyType, setBodyType] = useState('');
  const [error, setError] = useState('');

  // currentUser ID 기반으로 인바디 데이터 조회
  const getInbodyData = () => {
    // 1) location state에서 전달된 데이터
    if (locationInbody) {
      return locationInbody;
    }
    
    // 2) UserContext를 통한 조회
    return getUserData('inbody');
  };

  // currentUser ID 기반으로 설문 데이터 조회
  const getSurveyData = () => {
    // 1) location state에서 전달된 데이터
    if (locationSurvey) {
      return locationSurvey;
    }
    
    // 2) UserContext를 통한 조회
    return getUserData('survey');
  };

  useEffect(() => {
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
        if (cachedData) {
          if (cachedData.workouts && Object.keys(cachedData.workouts).length > 0) {
            hasRecommendations = true;
          }
        }
      }
      
      // 기본 키로도 확인
      if (!hasRecommendations) {
        const cachedData = storageManager.getItem('recommendations');
        if (cachedData) {
          if (cachedData.workouts && Object.keys(cachedData.workouts).length > 0) {
            hasRecommendations = true;
          }
        }
      }
    } catch (e) {
      console.error('추천 데이터 확인 실패:', e);
    }
    
    // 추천 데이터가 없고, 인바디/설문 데이터도 없을 때만 /inbody로 이동
    if (!hasRecommendations && (!inbody || !survey)) {
      navigate('/inbody-input');
      return;
    }

    // 이미 추천 데이터가 있으면 API 호출 생략
    if (recommendations) {
      console.log('이미 받은 추천 데이터 사용:', recommendations);
      setWorkouts(recommendations.workouts || {});
      setDiets(recommendations.diets || {});
      
      // 체형 분석 결과도 함께 조회
      let bodyAnalysis = null;
      try {
        const currentUser = storageManager.getItem('currentUser');
        if (currentUser) {
          const bodyAnalysisKey = `bodyAnalysis_${currentUser.id}`;
          const bodyAnalysisData = storageManager.getItem(bodyAnalysisKey);
          if (bodyAnalysisData) {
            bodyAnalysis = bodyAnalysisData;
          }
        }
        
        if (!bodyAnalysis) {
          const bodyAnalysisData = storageManager.getItem('bodyAnalysis');
          if (bodyAnalysisData) {
            bodyAnalysis = bodyAnalysisData;
          }
        }
      } catch (e) {
        console.error('체형 분석 결과 조회 실패:', e);
      }
      
      setBodyType(bodyAnalysis?.bodyType || '맞춤 추천 완료');
      setLoading(false);
      return;
    }

    // DB에서 캐시된 데이터 확인 (DB 우선, localStorage fallback)
    const checkCachedData = async () => {
      try {
        console.log('🔍 DB에서 캐시된 추천 데이터 조회 시도...');
        
        // DB 조회 대신 직접 sessionStorage 확인 (임시)
        const cachedWorkoutResult = { success: false, data: null };
        
        if (cachedWorkoutResult.success && cachedWorkoutResult.data) {
          console.log('✅ 캐시된 운동 추천 데이터 사용:', cachedWorkoutResult.source, cachedWorkoutResult.data);
          
          setWorkouts(cachedWorkoutResult.data.workouts || {});
          setBodyType(cachedWorkoutResult.data.programName || '맞춤 추천 완료');
          
          // 식단 데이터는 별도 처리 (현재는 sessionStorage에서 조회)
          try {
            const currentUser = storageManager.getItem('currentUser');
            if (currentUser) {
              const userKey = `recommendations_${currentUser.id}`;
              const cachedData = storageManager.getItem(userKey);
              if (cachedData) {
                setDiets(cachedData.diets || {});
              }
            } else {
              const cachedData = storageManager.getItem('recommendations');
              if (cachedData) {
                setDiets(cachedData.diets || {});
              }
            }
          } catch (dietError) {
            console.error('식단 캐시 조회 실패:', dietError);
            setDiets({});
          }
          
          setLoading(false);
          return true; // 데이터를 찾았음을 표시
        } else {
          console.log('📝 캐시된 추천 데이터 없음, API 호출 필요');
          return false; // 데이터를 찾지 못했음을 표시
        }
      } catch (e) {
        console.error('캐시된 추천 데이터 조회 실패:', e);
        return false;
      }
    };

    // 추천 데이터가 없으면 설문 페이지로 안내 (새 추천 생성 안함)
    const loadRecommendations = async () => {
      console.log('📝 추천 데이터가 없습니다. 설문 페이지에서 추천을 받아주세요.');
      setError('추천 데이터가 없습니다. 설문조사를 완료하여 추천을 받아주세요.');
        setLoading(false);
    };

    // 캐시 데이터 확인 실행
    checkCachedData().then(foundCachedData => {
      if (foundCachedData) {
        return; // 캐시 데이터를 찾았으므로 API 호출 생략
      }
      
      // 캐시 데이터가 없으면 API 호출 실행
      loadRecommendations();
    });
  }, [navigate, recommendations]);

  if (loading) {
    return (
      <Layout>
        <div className="text-center py-16 text-xl text-blue-500">추천 데이터 로딩 중...</div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="text-center py-16">
          <div className="text-xl text-red-500 mb-4">추천 데이터 로드 실패</div>
          <div className="text-gray-600 mb-8">{error}</div>
          <Button onClick={() => navigate('/survey')} className="bg-primary text-white px-6 py-3 rounded-full">
            다시 시도하기
          </Button>
        </div>
      </Layout>
    );
  }

  // 대표 운동/식단 추출
  const firstWorkoutDay = Object.keys(workouts)[0];
  const firstWorkout = workouts[firstWorkoutDay]?.[0];
  const firstDietDay = Object.keys(diets)[0];
  const firstDiet = firstDietDay ? Object.values(diets[firstDietDay] || {})[0] : null;
  
  // 안전한 체크를 위한 추가 로그
  console.log('🔍 firstWorkoutDay:', firstWorkoutDay);
  console.log('🔍 workouts[firstWorkoutDay]:', workouts[firstWorkoutDay]);
  console.log('🔍 firstWorkout:', firstWorkout);
  console.log('🔍 firstWorkout?.name:', firstWorkout?.name);
  console.log('🔍 firstDietDay:', firstDietDay);
  console.log('🔍 diets[firstDietDay]:', diets[firstDietDay]);
  console.log('🔍 Object.values(diets[firstDietDay]):', firstDietDay ? Object.values(diets[firstDietDay] || {}) : 'N/A');
  console.log('🔍 firstDiet:', firstDiet);
  console.log('🔍 firstDiet?.name:', firstDiet?.name);
  
  // 디버깅 로그 추가
  console.log('🔍 RecommendationsPage 디버깅:');
  console.log('🔍 workouts:', workouts);
  console.log('🔍 workouts keys:', Object.keys(workouts));
  console.log('🔍 firstWorkoutDay:', firstWorkoutDay);
  console.log('🔍 firstWorkout:', firstWorkout);
  console.log('🔍 diets:', diets);
  console.log('🔍 diets keys:', Object.keys(diets));
  console.log('🔍 firstDietDay:', firstDietDay);
  console.log('🔍 firstDiet:', firstDiet);

  return (
    <Layout>
      <HeroWithBg
        title="맞춤 추천 결과"
        subtitle={"체형분석과 설문조사를 바탕으로\n개인 맞춤 운동·식단을 추천드립니다."}
        bgImage="/assets/recommendations-bg.jpg"
      />
      
      {/* 체형 요약 */}
      <section className="w-full py-8 px-4 flex flex-col items-center">
        <div className="bg-gradient-to-r from-blue-50 to-purple-50 rounded-2xl p-6 w-full max-w-2xl text-center">
          <div className="flex items-center justify-center gap-2 mb-2">
            <CheckCircle2 className="w-6 h-6 text-green-600" />
            <span className="text-lg font-bold text-gray-800">분석 완료</span>
          </div>
          <div className="text-2xl font-bold text-primary mb-2">{bodyType}</div>
          <p className="text-gray-600">맞춤 운동·식단 프로그램을 확인해보세요!</p>
        </div>
      </section>
      
      <section className="w-full py-8 px-4 flex flex-col items-center">
        <div className="w-full max-w-6xl space-y-12">
          
          {/* 운동 추천 미리보기 */}
          <div className="bg-white rounded-2xl shadow-lg p-8">
            <div className="flex items-center gap-4 mb-6">
              <Dumbbell className="w-8 h-8 text-blue-600" />
              <h2 className="text-2xl font-bold text-gray-800">운동 추천</h2>
            </div>
            
            {firstWorkout && firstWorkout.name ? (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-center">
                <div>
                  <div className="text-lg font-semibold text-blue-700 mb-2">추천 운동 예시</div>
                  <div className="text-xl font-bold text-gray-800 mb-1">{firstWorkout.name}</div>
                  <div className="text-gray-600 mb-4">요일별 맞춤 운동 프로그램이 준비되어 있습니다.</div>
                  <div className="text-sm text-gray-500">
                    • 총 {Object.keys(workouts).length}개 요일 프로그램<br/>
                    • 체형별 맞춤 강도 조절<br/>
                    • 단계별 운동 가이드 포함
                  </div>
                </div>
                <div className="text-center">
                  <Button 
                    onClick={() => navigate('/recommended-workout-list', { 
                      state: { 
                        workouts: workouts,
                        fromRecommendations: true,
                        // 설문 직후 이동 시에는 location.state.recommendations에
                        // 전체 운동 프로그램 메타데이터가 함께 들어있음
                        workoutData: recommendations || null,
                      } 
                    })}
                    className="bg-blue-600 text-white px-8 py-4 rounded-lg hover:bg-blue-700 font-bold text-lg flex items-center gap-2 mx-auto"
                  >
                    전체 운동 프로그램 보기
                    <ArrowRight className="w-5 h-5" />
                  </Button>
                </div>
              </div>
            ) : (
              <div className="text-center text-gray-500 py-8">
                운동 추천 데이터를 불러올 수 없습니다.
              </div>
            )}
          </div>
          
          {/* 식단 추천 미리보기 */}
          <div className="bg-white rounded-2xl shadow-lg p-8">
            <div className="flex items-center gap-4 mb-6">
              <UtensilsCrossed className="w-8 h-8 text-green-600" />
              <h2 className="text-2xl font-bold text-gray-800">식단 추천</h2>
            </div>
            
            {firstDiet && firstDiet.name ? (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-center">
                <div>
                  <div className="text-lg font-semibold text-green-700 mb-2">추천 식단 예시</div>
                  <div className="text-xl font-bold text-gray-800 mb-1">{firstDiet.name}</div>
                  <div className="text-gray-600 mb-4">영양 균형을 고려한 요일별 식단표가 준비되어 있습니다.</div>
                  <div className="text-sm text-gray-500">
                    • 총 {Object.keys(diets).length}개 요일 식단표<br/>
                    • 칼로리 및 영양소 계산<br/>
                    • 체형별 맞춤 영양 비율
                  </div>
                </div>
                <div className="text-center">
                  <Button 
                    onClick={() => navigate('/recommended-diet-list', { 
                      state: { 
                        diets: diets,
                        fromRecommendations: true,
                        // 설문 직후 이동 시에는 location.state.recommendations에
                        // 전체 식단 메타데이터가 함께 들어있음
                        dietData: recommendations || null,
                        recommendations: recommendations || null, // recommendations 객체도 전달
                      } 
                    })}
                    className="bg-green-600 text-white px-8 py-4 rounded-lg hover:bg-green-700 font-bold text-lg flex items-center gap-2 mx-auto"
                  >
                    전체 식단 프로그램 보기
                    <ArrowRight className="w-5 h-5" />
                  </Button>
                </div>
              </div>
            ) : (
              <div className="text-center text-gray-500 py-8">
                식단 추천 데이터를 불러올 수 없습니다.
              </div>
            )}
          </div>
          
          {/* 기타 옵션 */}
          <div className="text-center space-y-4">
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button 
                onClick={() => navigate('/body-analysis')}
                className="bg-gray-500 text-white px-6 py-3 rounded-full hover:bg-gray-600"
              >
                체형분석 다시보기
              </Button>
              <Button 
                onClick={() => navigate('/calendar')}
                className="bg-purple-600 text-white px-6 py-3 rounded-full hover:bg-purple-700"
              >
                운동 기록 관리
              </Button>
            </div>
            <p className="text-gray-500 text-sm">
              추천 프로그램을 따라하시고 꾸준히 기록해보세요! 💪
            </p>
          </div>
        </div>
      </section>
    </Layout>
  );
}