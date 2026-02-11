import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import Button from '../components/Button';
import { useUser, getCurrentUserId } from '../api/auth';
import { storageManager } from '../utils/storageManager';
import { fetchBodyTypeAnalysis, getLatestBodyAnalysis, getBodyAnalysisHistory } from '../api/bodyAnalysis';
import { getBodyTypeInfo, getBodyTypeDisplayName } from '../utils/bodyTypeUtils';
import { apiCall } from '../api/config';
import { getLatestInbodyRecord } from '../api/inbody';
import { 
  User, 
  Activity, 
  Target, 
  Calendar, 
  TrendingUp, 
  Settings, 
  Heart,
  Zap,
  Award,
  Clock,
  BarChart3,
  PlayCircle,
  Apple,
  History
} from 'lucide-react';

export default function MyPage() {
  const navigate = useNavigate();
  const { user: currentUser, isLoggedIn, getUserData } = useUser(); // useUser Hook 사용
  const [user, setUser] = useState(null);
  const [inbodyData, setInbodyData] = useState(null);
  const [bodyType, setBodyType] = useState('');
  const [bodyAnalysis, setBodyAnalysis] = useState(null);
  const [bodyAnalysisHistory, setBodyAnalysisHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 로그인 체크
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }

    // 사용자 정보 및 데이터 로딩
    const loadUserData = async () => {
      try {
        // 현재 사용자 정보 가져오기
        if (currentUser) {
          setUser(currentUser);
        }

        // 백엔드 API를 통해 최신 인바디 데이터 조회
        const userId = getCurrentUserId();
        if (userId) {
          console.log('🔍 MyPage에서 인바디 데이터 조회 시작, userId:', userId);
          
          try {
            const inbodyData = await getLatestInbodyRecord(userId);
            console.log('🔍 MyPage에서 조회된 인바디 데이터:', inbodyData);
            
            if (inbodyData) {
              setInbodyData(inbodyData);
            }
          } catch (error) {
            console.error('❌ MyPage 인바디 데이터 조회 실패:', error);
          }
        }

        // 체형 분석 데이터 조회 (백엔드 API 사용)
        try {
          const userId = getCurrentUserId();
          if (userId) {
            console.log('🔍 MyPage에서 백엔드 API로 체형 분석 데이터 조회 시작, userId:', userId);
            
            // 체형분석 히스토리만 조회 (최신 조회는 403 오류로 인해 제외)
            const bodyAnalysisHistoryData = await getBodyAnalysisHistory(userId, { size: 1 }).catch(err => {
              console.error('❌ 체형분석 히스토리 조회 실패:', err);
              return { content: [] };
            });
            
            // 히스토리에서 최신 데이터 추출
            const savedBodyAnalysis = bodyAnalysisHistoryData && bodyAnalysisHistoryData.content && bodyAnalysisHistoryData.content.length > 0 
              ? bodyAnalysisHistoryData.content[0] 
              : null;
            
            console.log('🔍 MyPage에서 조회된 체형 분석 데이터:', savedBodyAnalysis);
            console.log('🔍 MyPage에서 조회된 체형분석 히스토리:', bodyAnalysisHistoryData);
            
            // 히스토리 데이터 설정
            if (bodyAnalysisHistoryData && bodyAnalysisHistoryData.content) {
              setBodyAnalysisHistory(bodyAnalysisHistoryData.content);
            } else if (Array.isArray(bodyAnalysisHistoryData)) {
              setBodyAnalysisHistory(bodyAnalysisHistoryData);
            }
            
            if (savedBodyAnalysis && savedBodyAnalysis.label) {
              console.log('🔍 히스토리에서 받은 체형분석 데이터 구조:', savedBodyAnalysis);
              console.log('🔍 savedBodyAnalysis.label:', savedBodyAnalysis.label);
              console.log('🔍 savedBodyAnalysis.bodyType:', savedBodyAnalysis.bodyType);
              console.log('🔍 savedBodyAnalysis.summary:', savedBodyAnalysis.summary);
              
              // 체형 유형 결정 로직 (label이 백엔드의 기본 필드)
              let detectedBodyType = savedBodyAnalysis.label || savedBodyAnalysis.bodyType;
              
              console.log('🔍 1차 체형 유형:', detectedBodyType);
              
              // label과 bodyType이 없으면 summary에서 추출
              if (!detectedBodyType && savedBodyAnalysis.summary) {
                const summary = savedBodyAnalysis.summary;
                console.log('🔍 summary에서 체형 유형 추출 시도:', summary);
                
                // 백엔드에서 정의된 14개 체형분류 라벨 모두 처리 (순서 중요: 구체적인 것부터)
                if (summary.includes('운동선수급')) detectedBodyType = '운동선수급';
                else if (summary.includes('근육형비만')) detectedBodyType = '근육형비만';
                else if (summary.includes('근육많은씬') || summary.includes('근육형날씬')) detectedBodyType = '근육형날씬';
                else if (summary.includes('근육형')) detectedBodyType = '근육형';
                else if (summary.includes('고도비만')) detectedBodyType = '고도비만';
                else if (summary.includes('복부비만형')) detectedBodyType = '복부비만형';
                else if (summary.includes('마른비만')) detectedBodyType = '마른비만';
                else if (summary.includes('약간마름')) detectedBodyType = '약간마름';
                else if (summary.includes('마름')) detectedBodyType = '마름';
                else if (summary.includes('적정') || summary.includes('정상형') || summary.includes('정상')) detectedBodyType = '적정';
                else if (summary.includes('날씬') || summary.includes('씬')) detectedBodyType = '날씬';
                else if (summary.includes('경도비만')) detectedBodyType = '경도비만';
                else if (summary.includes('비만')) detectedBodyType = '비만';
                else if (summary.includes('과체중')) detectedBodyType = '과체중';
                else if (summary.includes('균형')) detectedBodyType = '균형';
                
                console.log('🔍 summary에서 추출된 체형 유형:', detectedBodyType);
              }
              
              console.log('🔍 최종 체형 유형:', detectedBodyType);
              
              // 체형분석 데이터에 bodyType 추가
              const enhancedBodyAnalysis = {
                ...savedBodyAnalysis,
                bodyType: detectedBodyType || savedBodyAnalysis.label || '체형 분석 결과'
              };
              
              setBodyAnalysis(enhancedBodyAnalysis);
              setBodyType(detectedBodyType || savedBodyAnalysis.label || '분석 필요');
              
              console.log('✅ 최종 설정된 체형:', detectedBodyType || savedBodyAnalysis.label || '분석 필요');
            } else {
              console.log('📝 체형분석 히스토리에서 데이터를 찾을 수 없음');
              console.log('📝 savedBodyAnalysis:', savedBodyAnalysis);
              setBodyType('분석 필요');
            }
          }
        } catch (e) {
          console.error('❌ MyPage 체형 분석 데이터 조회 실패:', e);
          setBodyType('분석 결과 없음');
        }
      } catch (error) {
        console.error('데이터 로딩 실패:', error);
      } finally {
        setLoading(false);
      }
    };

    loadUserData();
  }, [navigate, isLoggedIn, currentUser]);

  // 체형 분석: 인바디가 없으면 입력 페이지, 있으면 body-analysis로 이동하며 재분석 플래그 전달
  const handleGoToAnalysis = () => {
    if (!inbodyData) {
      navigate('/inbody-input');
    } else {
      navigate('/body-analysis', { state: { forceReanalyze: true } });
    }
  };

  const quickActions = [
    {
      title: '인바디 데이터 입력',
      description: '새로운 인바디 측정 결과를 입력하세요',
      icon: Activity,
      color: 'bg-blue-500',
      action: () => navigate('/inbody-input')
    },
    {
      title: '체형 분석하기',
      description: inbodyData ? 'AI 체형 분석을 받아보세요' : '인바디 입력 후 체형 분석을 받아보세요',
      icon: BarChart3,
      color: 'bg-purple-500',
      action: handleGoToAnalysis
    },
    {
      title: '맞춤 추천받기',
      description: '개인화된 운동·식단 추천을 받으세요',
      icon: Target,
      color: 'bg-green-500',
      action: () => navigate('/survey')
    }
  ];

  const dashboardCards = [
    {
      title: '나의 건강 현황',
      items: [
        { label: '최근 체중', value: inbodyData?.['체중'] ? `${inbodyData['체중']}kg` : '데이터 없음', icon: Activity },
        { 
          label: '체형 유형', 
          value: bodyAnalysis ? (() => {
            const bodyTypeInfo = getBodyTypeInfo(bodyType);
            return `${bodyTypeInfo.emoji} ${bodyTypeInfo.displayName}`;
          })() : 
                 (bodyAnalysisHistory && bodyAnalysisHistory.length > 0) ? '히스토리 확인' :
                 inbodyData ? '분석 필요' : '데이터 없음', 
          icon: User,
          subtitle: bodyAnalysis ? 
                   `${bodyAnalysis.method === 'OpenAI ChatGPT API' ? '🤖 OpenAI AI' : '🔍 AI'} 분석 (${new Date(bodyAnalysis.analyzedAt).toLocaleDateString()})` : 
                   (bodyAnalysisHistory && bodyAnalysisHistory.length > 0) ? '📊 체형분석 히스토리 보기' :
                   inbodyData && !bodyAnalysis ? '👆 클릭하여 AI 분석 받기' : null,
          action: (bodyAnalysisHistory && bodyAnalysisHistory.length > 0) ? 
                   () => navigate('/health-history') :
                   (inbodyData && !bodyAnalysis ? handleGoToAnalysis : null),
          valueColor: bodyAnalysis ? getBodyTypeInfo(bodyType).color : null
        },
        { label: '체지방률', value: inbodyData?.['체지방률'] ? `${inbodyData['체지방률']}%` : '데이터 없음', icon: TrendingUp },
        { label: '골격근량', value: inbodyData?.['골격근량'] ? `${inbodyData['골격근량']}kg` : '데이터 없음', icon: Zap }
      ]
    },
    {
      title: '추천 및 기록',
      items: [
        { 
          label: '추천 운동', 
          value: '운동 프로그램 보기', 
          icon: PlayCircle, 
          action: async () => {
            // 1순위: sessionStorage 캐시 확인
            let recommendations = null;
            try {
              const currentUser = storageManager.getItem('currentUser');
              if (currentUser) {
                const userKey = `recommendations_${currentUser.id}`;
                const cachedData = storageManager.getItem(userKey);
                if (cachedData) {
                  recommendations = cachedData;
                }
              }
              // fallback: 기본 키로 조회
              if (!recommendations) {
                const cachedData = storageManager.getItem('recommendations');
                if (cachedData) {
                  recommendations = cachedData;
                }
              }
            } catch (e) {
              console.error('추천 데이터 조회 실패:', e);
            }
            
            // 2순위: 백엔드에서 최신 데이터 조회
            if (!recommendations || !recommendations.workouts) {
              try {
                const currentUser = storageManager.getItem('currentUser');
                if (currentUser) {
                  console.log('🔍 백엔드에서 최신 운동 추천 조회 시도...');
                  const response = await apiCall(`/api/workout-recommendation/${currentUser.id}?page=0&size=1`);
                  
                  if (response && response.success && response.data && response.data.content && response.data.content.length > 0) {
                    const latestWorkout = response.data.content[0];
                    console.log('✅ 백엔드에서 최신 운동 추천 조회 성공:', latestWorkout);
                    console.log('🔍 latestWorkout 구조 분석:', {
                      hasWorkouts: !!latestWorkout.workouts,
                      hasProgramName: !!latestWorkout.programName,
                      keys: Object.keys(latestWorkout)
                    });
                    
                    // workouts 필드가 있으면 사용, 없으면 전체 데이터를 workouts로 전달
                    const workoutData = latestWorkout.workouts || latestWorkout;
                    
                    navigate('/recommended-workout-list', { 
                      state: { 
                        workouts: workoutData,
                        fromRecommendations: true,
                        workoutData: latestWorkout // 전체 메타데이터를 포함한 workoutData 전달
                      }
                    });
                    return;
                  } else {
                    console.log('📝 백엔드에서 운동 추천 데이터가 없음');
                  }
                }
              } catch (error) {
                console.error('❌ 백엔드 운동 추천 조회 실패:', error);
              }
            }
            
            // 3순위: 캐시된 데이터가 있으면 사용
            if (recommendations && recommendations.workouts) {
              navigate('/recommended-workout-list', { 
                state: { 
                  workouts: recommendations.workouts,
                  fromRecommendations: true,
                  workoutData: recommendations // 전체 메타데이터를 포함한 recommendations 전달
                }
              });
            } else {
              // 4순위: 데이터가 없으면 사용자에게 안내 후 설문 페이지로
              const hasInbody = !!inbodyData;
              const hasSurvey = !!getUserData('survey');
              
              if (!hasInbody && !hasSurvey) {
                alert('운동 추천을 받으려면 인바디 데이터와 설문 데이터가 필요합니다.\n\n먼저 인바디 데이터를 입력하고 설문조사를 완료해주세요.');
                navigate('/inbody-input');
              } else if (!hasInbody) {
                alert('운동 추천을 받으려면 인바디 데이터가 필요합니다.\n\n먼저 인바디 데이터를 입력해주세요.');
                navigate('/inbody-input');
              } else if (!hasSurvey) {
                alert('운동 추천을 받으려면 설문 데이터가 필요합니다.\n\n설문조사를 완료해주세요.');
                navigate('/survey');
              } else {
                alert('운동 추천 데이터가 없습니다.\n\n새로운 운동 추천을 받아보세요.');
                navigate('/survey');
              }
            }
          }
        },
        { 
          label: '추천 식단', 
          value: '식단 계획 보기', 
          icon: Apple, 
          action: async () => {
            // 1순위: sessionStorage 캐시 확인
            let recommendations = null;
            try {
              const currentUser = storageManager.getItem('currentUser');
              if (currentUser) {
                const userKey = `recommendations_${currentUser.id}`;
                const cachedData = storageManager.getItem(userKey);
                if (cachedData) {
                  recommendations = cachedData;
                }
              }
              // fallback: 기본 키로 조회
              if (!recommendations) {
                const cachedData = storageManager.getItem('recommendations');
                if (cachedData) {
                  recommendations = cachedData;
                }
              }
            } catch (e) {
              console.error('추천 데이터 조회 실패:', e);
            }
            
            // 2순위: 백엔드에서 최신 데이터 조회
            if (!recommendations || !recommendations.diets) {
              try {
                const currentUser = storageManager.getItem('currentUser');
                if (currentUser) {
                  console.log('🔍 백엔드에서 최신 식단 추천 조회 시도...');
                  const response = await apiCall(`/api/diet-recommendation/${currentUser.id}/history`);
                  
                  if (response && response.success && response.data && response.data.length > 0) {
                    const latestDiet = response.data[0]; // 가장 최신 식단 추천
                    console.log('✅ 백엔드에서 최신 식단 추천 조회 성공:', latestDiet);
                    console.log('🔍 latestDiet 구조 분석:', {
                      hasDiets: !!latestDiet.diets,
                      hasMealStyle: !!latestDiet.mealStyle,
                      keys: Object.keys(latestDiet)
                    });
                    
                    // diets 필드가 있으면 사용, 없으면 전체 데이터를 diets로 전달
                    const dietData = latestDiet.diets || latestDiet;
                    
                    // recommendations 객체 생성 (메타데이터 포함)
                    const recommendationsData = {
                      diets: dietData,
                      mealStyle: latestDiet.mealStyle,
                      dailyCalories: latestDiet.dailyCalories,
                      macroSplit: latestDiet.macroSplit,
                      sampleMenu: latestDiet.sampleMenu,
                      shoppingList: latestDiet.shoppingList,
                      precautions: latestDiet.precautions,
                      mealTiming: latestDiet.mealTiming,
                      hydration: latestDiet.hydration,
                      supplements: latestDiet.supplements,
                    };
                    
                    navigate('/recommended-diet-list', { 
                      state: { 
                        diets: dietData,
                        fromRecommendations: true,
                        dietData: latestDiet, // 전체 데이터도 dietData로 전달
                        recommendations: recommendationsData // recommendations 객체 전달
                      }
                    });
                    return;
                  } else {
                    console.log('📝 백엔드에서 식단 추천 데이터가 없음');
                  }
                }
              } catch (error) {
                console.error('❌ 백엔드 식단 추천 조회 실패:', error);
              }
            }
            
            // 3순위: 캐시된 데이터가 있으면 사용
            if (recommendations && recommendations.diets) {
              navigate('/recommended-diet-list', { 
                state: { 
                  diets: recommendations.diets,
                  fromRecommendations: true,
                  dietData: recommendations, // 전체 recommendations를 dietData로도 전달
                  recommendations: recommendations // recommendations 객체 전달
                }
              });
            } else {
              // 4순위: 데이터가 없으면 사용자에게 안내 후 설문 페이지로
              const hasInbody = !!inbodyData;
              const hasSurvey = !!getUserData('survey');
              
              if (!hasInbody && !hasSurvey) {
                alert('식단 추천을 받으려면 인바디 데이터와 설문 데이터가 필요합니다.\n\n먼저 인바디 데이터를 입력하고 설문조사를 완료해주세요.');
                navigate('/inbody-input');
              } else if (!hasInbody) {
                alert('식단 추천을 받으려면 인바디 데이터가 필요합니다.\n\n먼저 인바디 데이터를 입력해주세요.');
                navigate('/inbody-input');
              } else if (!hasSurvey) {
                alert('식단 추천을 받으려면 설문 데이터가 필요합니다.\n\n설문조사를 완료해주세요.');
                navigate('/survey');
              } else {
                alert('식단 추천 데이터가 없습니다.\n\n새로운 식단 추천을 받아보세요.');
                navigate('/survey');
              }
            }
          }
        },
        { label: '운동 기록', value: '캘린더 보기', icon: Calendar, action: () => navigate('/calendar') },
        { label: '평가하기', value: '피드백 남기기', icon: Award, action: () => navigate('/evaluation') }
      ]
    },
    {
      title: '히스토리 및 설정',
      items: [
        { label: '통합 히스토리', value: '모든 기록 보기', icon: History, action: () => navigate('/health-history') },
        { label: '인바디 히스토리', value: '과거 기록 보기', icon: BarChart3, action: () => navigate('/inbody-history') },
        { label: '설문 기록', value: '설문 히스토리', icon: Clock, action: () => navigate('/survey-history') },
        { 
          label: '개인 설정', 
          value: '프로필 관리', 
          icon: Settings, 
          action: () => {
            // 임시로 alert 표시 (나중에 설정 페이지 구현 시 교체)
            alert('개인 설정 페이지는 준비 중입니다.\n\n현재 이용 가능한 기능:\n• 통합 히스토리: 모든 데이터 조회\n• 인바디 히스토리: 인바디 기록 관리\n• 설문 기록: 설문조사 히스토리');
          }
        }
      ]
    }
  ];

  if (loading) {
    return (
      <Layout>
        <div className="text-center py-16">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-xl text-gray-600">마이페이지를 불러오는 중...</p>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      {/* 헤더 섹션 */}
      <section className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-3xl text-white p-8 mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold mb-2">안녕하세요, {user?.email?.split('@')[0] || '사용자'}님! 👋</h1>
            <p className="text-blue-100 text-lg">오늘도 건강한 하루 되세요!</p>
            <p className="text-sm text-blue-200 mt-2">
              {user?.provider === 'email' ? '이메일' : 
               user?.provider === 'google' ? 'Google' :
               user?.provider === 'kakao' ? 'Kakao' : 'Naver'} 계정으로 로그인됨
            </p>
            <p className="text-sm text-blue-200">로그인 시간: {user?.loginTime ? new Date(user.loginTime).toLocaleString() : ''}</p>
          </div>
          <div className="hidden md:block">
            <Heart className="w-24 h-24 text-red-300" />
          </div>
        </div>
      </section>

      {/* 빠른 액션 버튼 */}
      <section className="mb-8">
        <h2 className="text-2xl font-bold text-white mb-6">🚀 데이터 입력</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {quickActions.map((action, index) => (
            <div
              key={index}
              onClick={action.action}
              className="bg-white rounded-2xl p-6 shadow-lg hover:shadow-xl transform hover:scale-105 transition-all cursor-pointer"
            >
              <div className={`${action.color} w-12 h-12 rounded-xl flex items-center justify-center mb-4`}>
                <action.icon className="w-6 h-6 text-white" />
              </div>
              <h3 className="text-lg font-bold text-gray-800 mb-2">{action.title}</h3>
              <p className="text-gray-600 text-sm">{action.description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* 체형 분석 유도 섹션 (AI 분석 결과가 없고 체형분석 히스토리도 없을 때만 표시) */}
      {!bodyAnalysis && inbodyData && (!bodyAnalysisHistory || bodyAnalysisHistory.length === 0) && (
        <section className="mb-8">
          <div className="bg-gradient-to-r from-purple-100 to-blue-100 rounded-2xl p-6 border-2 border-purple-300">
            <div className="flex items-center gap-4">
              <div className="bg-purple-600 w-12 h-12 rounded-xl flex items-center justify-center">
                <BarChart3 className="w-6 h-6 text-white" />
              </div>
              <div className="flex-1">
                <h3 className="text-xl font-bold text-purple-800">🎯 OpenAI AI 체형 분석을 받아보세요!</h3>
                <p className="text-purple-600">
                  입력하신 인바디 데이터로 정확한 OpenAI AI 체형 분석을 받을 수 있습니다.
                </p>
              </div>
              <button
                onClick={handleGoToAnalysis}
                className="bg-purple-600 text-white px-6 py-3 rounded-lg hover:bg-purple-700 transition font-bold"
              >
                분석 시작하기
              </button>
            </div>
          </div>
        </section>
      )}

      {/* 체형분석 히스토리 안내 섹션 (히스토리는 있지만 최신 분석 결과가 없을 때 표시) */}
      {!bodyAnalysis && bodyAnalysisHistory && bodyAnalysisHistory.length > 0 && (
        <section className="mb-8">
          <div className="bg-gradient-to-r from-green-100 to-blue-100 rounded-2xl p-6 border-2 border-green-300">
            <div className="flex items-center gap-4">
              <div className="bg-green-600 w-12 h-12 rounded-xl flex items-center justify-center">
                <BarChart3 className="w-6 h-6 text-white" />
              </div>
              <div className="flex-1">
                <h3 className="text-xl font-bold text-green-800">📊 체형분석 히스토리가 있습니다!</h3>
                <p className="text-green-600">
                  이전에 수행한 체형분석 결과를 확인하거나 새로운 분석을 받아보세요.
                </p>
              </div>
              <div className="flex flex-col gap-2">
                <button
                  onClick={() => navigate('/health-history')}
                  className="bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700 transition font-bold"
                >
                  히스토리 보기
                </button>
                <button
                  onClick={() => navigate('/inbody-input')}
                  className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition font-bold"
                >
                  새로 분석하기
                </button>
              </div>
            </div>
          </div>
        </section>
      )}

      {/* 인바디 데이터 입력 유도 섹션 (인바디 데이터가 없을 때만 표시) */}
      {!inbodyData && (
        <section className="mb-8">
          <div className="bg-gradient-to-r from-blue-100 to-green-100 rounded-2xl p-6 border-2 border-blue-300">
            <div className="flex items-center gap-4">
              <div className="bg-blue-600 w-12 h-12 rounded-xl flex items-center justify-center">
                <Activity className="w-6 h-6 text-white" />
              </div>
              <div className="flex-1">
                <h3 className="text-xl font-bold text-blue-800">📊 인바디 데이터를 입력해주세요</h3>
                <p className="text-blue-600">
                  정확한 OpenAI AI 체형 분석을 위해 인바디 측정 데이터가 필요합니다.
                </p>
              </div>
              <button
                onClick={() => navigate('/inbody-input')}
                className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition font-bold"
              >
                데이터 입력하기
              </button>
            </div>
          </div>
        </section>
      )}

      {/* AI 체형 분석 결과 (있는 경우만 표시) */}
      {bodyAnalysis && (
        <section className="mb-8">
          <h2 className="text-2xl font-bold text-white mb-6">🤖 OpenAI AI 체형 분석 결과</h2>
          <div className="bg-gradient-to-br from-purple-50 to-blue-50 rounded-2xl p-6 border-2 border-purple-200">
            <div className="flex items-center gap-4 mb-4">
              <div className="bg-purple-600 w-12 h-12 rounded-xl flex items-center justify-center">
                <BarChart3 className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-xl font-bold text-purple-800">
                  {(() => {
                    const bodyTypeInfo = getBodyTypeInfo(bodyAnalysis.bodyType || bodyAnalysis.label);
                    return `${bodyTypeInfo.emoji} ${bodyTypeInfo.displayName}`;
                  })()}
                </h3>
                <p className="text-sm text-purple-600">
                  분석일: {bodyAnalysis.analyzedAt ? 
                    new Date(bodyAnalysis.analyzedAt).toLocaleString() : 
                    bodyAnalysis.createdAt ? 
                      new Date(bodyAnalysis.createdAt).toLocaleString() : 
                      '날짜 정보 없음'
                  }
                </p>
              </div>
            </div>
            <p className="text-gray-700 leading-relaxed mb-4">
              {bodyAnalysis.summary}
            </p>
            <div className="flex flex-col sm:flex-row gap-2">
              <button
                onClick={() => navigate('/inbody-input')}
                className="bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 transition text-sm"
              >
                재분석하기
              </button>
              <button
                onClick={() => navigate('/survey')}
                className="bg-white text-purple-600 border border-purple-600 px-4 py-2 rounded-lg hover:bg-purple-50 transition text-sm"
              >
                맞춤 추천받기
              </button>
            </div>
          </div>
        </section>
      )}

      {/* 대시보드 카드 */}
      <section className="mb-8">
        <h2 className="text-2xl font-bold text-white mb-6">📊 나의 최신 정보</h2>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">               
          {dashboardCards.map((card, cardIndex) => (
          <div key={cardIndex} className="bg-white rounded-2xl shadow-lg p-6">
            <h3 className="text-xl font-bold text-gray-800 mb-6 flex items-center gap-2">
              {cardIndex === 0 && <Activity className="w-6 h-6 text-blue-500" />}
              {cardIndex === 1 && <Target className="w-6 h-6 text-green-500" />}
              {cardIndex === 2 && <Settings className="w-6 h-6 text-gray-500" />}
              {card.title}
            </h3>
            
            <div className="space-y-4">
              {card.items.map((item, itemIndex) => (
                <div
                  key={itemIndex}
                  onClick={item.action}
                  className={`flex items-center justify-between p-3 rounded-lg ${
                    item.action ? 'hover:bg-gray-50 cursor-pointer' : 'bg-gray-50'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <item.icon className="w-5 h-5 text-gray-500" />
                    <div className="flex flex-col">
                      <span className="font-medium text-gray-700">{item.label}</span>
                      {item.subtitle && (
                        <span className={`text-xs ${
                          item.subtitle.includes('👆') ? 'text-purple-500 font-bold' : 'text-gray-500'
                        }`}>
                          {item.subtitle}
                        </span>
                      )}
                    </div>
                  </div>
                  <span className={`text-sm ${
                    item.valueColor ? item.valueColor : 
                    item.action ? 'text-blue-600 hover:text-blue-800 cursor-pointer' : 'text-gray-600'
                  }`}>
                    {item.value}
                  </span>
                </div>
              ))}
            </div>
          </div>
          ))}
        </div>
      </section>

      {/* 하단 안내 */}
      <section className="mt-12 bg-gradient-to-r from-green-100 to-blue-100 rounded-2xl p-8 text-center">
        <h3 className="text-2xl font-bold text-gray-800 mb-4">🎯 건강 관리 팁</h3>
        <p className="text-gray-600 text-lg mb-6">
          다른 사용자들의 건강 관리 팁을 확인하고<br/>
          본인만의 확고한 루틴을 만들어보세요!
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Button 
            onClick={() => navigate('/community')}
            className="bg-white text-blue-600 border-2 border-blue-600 px-6 py-3 rounded-full hover:bg-blue-50"
          >
            커뮤니티 둘러보기
          </Button>
          <Button 
            onClick={() => navigate('/evaluation')}
            className="bg-white text-blue-600 border-2 border-blue-600 px-6 py-3 rounded-full hover:bg-blue-50"
          >
            평점 남기기
          </Button>
          <Button
            onClick = {()=> navigate('/main')}
            className = "bg-white text-blue-600 border-2 border-blue-600 px-6 py-3 rounded-full hover:bg-blue-50"
            >
                메인페이지로 돌아가기
          </Button>
        </div>
      </section>
    </Layout>
  );
}
