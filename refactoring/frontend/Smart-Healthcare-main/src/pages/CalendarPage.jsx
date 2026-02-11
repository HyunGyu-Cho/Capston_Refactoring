import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Calendar from 'react-calendar';
import 'react-calendar/dist/Calendar.css';
import Layout from '../components/Layout';
import { apiCall } from '../api/config';
import { getCurrentUserId, useUser } from '../api/auth';
import { storageManager } from '../utils/storageManager';
import { getLatestInbodyRecord } from '../api/inbody';
import { getLatestSurveyByUserId } from '../api/survey';

async function getInbodySurvey() {
  // 백엔드 API를 통해 최신 인바디/설문 데이터를 불러옴
  try {
    const userId = getCurrentUserId();
    if (!userId) {
      console.log('❌ 사용자 ID가 없습니다.');
      return { inbody: null, survey: null };
    }
    
    console.log('🔍 CalendarPage에서 인바디/설문 데이터 조회 시작, userId:', userId);
    
    // 1. 최신 인바디 데이터 조회
    const inbody = await getLatestInbodyRecord(userId);
    console.log('🔍 조회된 인바디 데이터:', inbody);
    
    // 2. 최신 설문 데이터 조회
    const survey = await getLatestSurveyByUserId(userId);
    console.log('🔍 조회된 설문 데이터:', survey);
    
    if (inbody && survey) {
      console.log('✅ 인바디/설문 데이터 조회 성공');
      return { inbody, survey };
    } else {
      console.log('⚠️ 인바디 또는 설문 데이터가 없습니다:', { inbody: !!inbody, survey: !!survey });
      return { inbody: null, survey: null };
    }
  } catch (error) {
    console.error('❌ 인바디/설문 데이터 조회 실패:', error);
    return { inbody: null, survey: null };
  }
}

export default function CalendarPage() {
  const navigate = useNavigate();
  const { getUserData } = useUser();
  const [checked, setChecked] = useState({});
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [workoutList, setWorkoutList] = useState([]);
  const [workoutCache, setWorkoutCache] = useState({}); // 요일별 캐시
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [historyLoading, setHistoryLoading] = useState(false);
  const selectedDateKey = selectedDate.toISOString().slice(0, 10);
  const dayNames = ['일요일','월요일','화요일','수요일','목요일','금요일','토요일'];
  const dayNamesEnglish = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];

  // 히스토리 fetch
  const fetchHistory = async () => {
    setHistoryLoading(true);
    try {
      const year = selectedDate.getFullYear();
      const month = selectedDate.getMonth() + 1;
      const from = `${year}-${String(month).padStart(2, '0')}-01`;
      const to = new Date(year, month, 0).toISOString().slice(0, 10);
      
      const userId = getCurrentUserId();
      console.log('🔍 히스토리 조회 요청:', { userId, from, to });
      
      const data = await apiCall(`/api/users/history?userId=${userId}&from=${from}&to=${to}`);
      
      console.log('🔍 히스토리 조회 응답:', data);
      
      if (data.success && data.histories) {
        console.log('🔍 조회된 히스토리 데이터:', data.histories);
        setChecked(data.histories);
      } else {
        console.log('⚠️ 히스토리 데이터가 없거나 실패:', data);
        setChecked({});
      }
    } catch (error) {
      console.error('❌ 히스토리 조회 실패:', error);
      setChecked({});
    } finally {
      setHistoryLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, [selectedDate]);

  // 초기 로딩 시 운동 추천 데이터 미리 로드
  useEffect(() => {
    const initializeWorkoutData = async () => {
      try {
        console.log('🔍 캘린더 페이지 초기화 - 운동 데이터 미리 로드');
        const { inbody, survey } = await getInbodySurvey();
        
        if (inbody && survey) {
          // 오늘 날짜의 운동 추천 데이터 미리 로드
          const today = new Date();
          const dayOfWeek = dayNames[today.getDay()];
          const dayOfWeekEnglish = dayNamesEnglish[today.getDay()];
          
          // 캐시에 이미 있으면 스킵
          if (!workoutCache[dayOfWeek]) {
            console.log('🔍 오늘 날짜 운동 데이터 미리 로드:', dayOfWeekEnglish);
            await handleDayClick(today);
          }
        }
      } catch (error) {
        console.error('❌ 초기 운동 데이터 로드 실패:', error);
      }
    };

    initializeWorkoutData();
  }, []); // 컴포넌트 마운트 시 한 번만 실행

  // 날짜 클릭 시 해당 요일의 추천운동 fetch
  const handleDayClick = async (date) => {
    setSelectedDate(date);
    const dayOfWeek = dayNames[date.getDay()];
    const dayOfWeekEnglish = dayNamesEnglish[date.getDay()];
    console.log('🔍 선택된 날짜:', date);
    console.log('🔍 dayOfWeek (한국어):', dayOfWeek);
    console.log('🔍 dayOfWeekEnglish (영어):', dayOfWeekEnglish);
    
    // 캐시된 데이터가 있으면 사용
    if (workoutCache[dayOfWeek]) {
      console.log('🔍 캐시된 운동 데이터 사용:', dayOfWeek, workoutCache[dayOfWeek]);
      setWorkoutList(workoutCache[dayOfWeek]);
      return;
    }
    
    setLoading(true);
    setError('');
    
    try {
      // 먼저 설문 데이터를 가져옴 (선택된 요일 확인용)
      const { inbody, survey } = await getInbodySurvey();
    
    // recommendations 데이터가 있는지 먼저 확인
    let hasRecommendations = false;
    let recommendations = null;
    try {
      const currentUser = storageManager.getItem('currentUser');
      console.log('🔍 currentUser:', currentUser);
      
      if (currentUser) {
        const userKey = `recommendations_${currentUser.id}`;
        console.log('🔍 userKey:', userKey);
        const cachedData = storageManager.getItem(userKey);
        console.log('🔍 사용자별 캐시 데이터:', cachedData);
        
        if (cachedData) {
          if (cachedData.workouts && Object.keys(cachedData.workouts).length > 0) {
            hasRecommendations = true;
            recommendations = cachedData;
            console.log('✅ 사용자별 추천 데이터 발견:', cachedData.workouts);
          }
        }
      }
      
      // 기본 키로도 확인
      if (!hasRecommendations) {
        const cachedData = storageManager.getItem('recommendations');
        console.log('🔍 기본 캐시 데이터:', cachedData);
        
        if (cachedData) {
          if (cachedData.workouts && Object.keys(cachedData.workouts).length > 0) {
            hasRecommendations = true;
            recommendations = cachedData;
            console.log('✅ 기본 추천 데이터 발견:', cachedData.workouts);
          }
        }
      }
      
      // getUserData로도 확인
      if (!hasRecommendations) {
        const userRecommendations = getUserData('recommendations');
        console.log('🔍 getUserData 추천 데이터:', userRecommendations);
        
        if (userRecommendations && userRecommendations.workouts && Object.keys(userRecommendations.workouts).length > 0) {
          hasRecommendations = true;
          recommendations = userRecommendations;
          console.log('✅ getUserData 추천 데이터 발견:', userRecommendations.workouts);
        }
      }
    } catch (e) {
      console.error('추천 데이터 확인 실패:', e);
    }
    
    // 추천 데이터가 있으면 캐시된 데이터 사용
    if (hasRecommendations && recommendations) {
      let workouts = recommendations.workouts[dayOfWeekEnglish] || [];
      
      console.log('🔍 추천 데이터 발견! recommendations:', recommendations);
      console.log('🔍 dayOfWeekEnglish:', dayOfWeekEnglish);
      console.log('🔍 해당 요일 운동 데이터:', workouts);
      
      // 설문에서 선택된 요일 확인
      let selectedDays = survey?.selectedDaysEn || [];
      
      console.log('🔍 설문 데이터 전체 구조:', survey);
      console.log('🔍 survey.selectedDaysEn:', survey?.selectedDaysEn);
      console.log('🔍 survey.selectedDays:', survey?.selectedDays);
      console.log('🔍 survey.workoutDays:', survey?.workoutDays);
      
      // selectedDaysEn이 없으면 한글 요일을 영문으로 변환
      if (!selectedDays || selectedDays.length === 0) {
        const dayMap = {
          "월요일": "Monday", "화요일": "Tuesday", "수요일": "Wednesday",
          "목요일": "Thursday", "금요일": "Friday", "토요일": "Saturday", "일요일": "Sunday",
          "월": "Monday", "화": "Tuesday", "수": "Wednesday",
          "목": "Thursday", "금": "Friday", "토": "Saturday", "일": "Sunday"
        };
        const koreanDays = survey?.selectedDays || survey?.workoutDays || [];
        selectedDays = koreanDays.map(day => dayMap[day] || day);
        console.log('🔍 한글 요일을 영문으로 변환:', koreanDays, '->', selectedDays);
      }
      
      const isSelectedDay = selectedDays.includes(dayOfWeekEnglish);
      
      console.log('🔍 설문 데이터 전체:', survey);
      console.log('🔍 설문 선택된 요일:', selectedDays);
      console.log('🔍 현재 요일이 선택된 요일인가?', isSelectedDay);
      console.log('🔍 요일 매칭 확인:', { dayOfWeekEnglish, selectedDays, isSelectedDay });
      
      // 이미 해당 요일의 추천 운동이 존재하면 설문 요일과 무관하게 표시
      // 추천이 비어있는 경우에만 설문 요일을 기준으로 빈 처리
      if (!workouts || workouts.length === 0) {
        // 설문에서 선택된 요일이 없는 경우 기본 운동 요일 사용 (월, 수, 금)
        if (!selectedDays || selectedDays.length === 0) {
          console.log('🔍 설문에서 선택된 요일이 없음. 기본 운동 요일 사용 (월, 수, 금)');
          const defaultWorkoutDays = ['Monday', 'Wednesday', 'Friday'];
          const isDefaultWorkoutDay = defaultWorkoutDays.includes(dayOfWeekEnglish);
          
          if (!isDefaultWorkoutDay) {
            console.log('🔍 기본 운동 요일에도 포함되지 않음:', dayOfWeekEnglish);
            setWorkoutCache(prev => ({ ...prev, [dayOfWeek]: [] }));
            setWorkoutList([]);
            setLoading(false);
            return;
          }
        } else if (!isSelectedDay) {
          // 설문에서 선택된 요일이 있지만 현재 요일이 포함되지 않은 경우
          console.log('🔍 설문에서 선택되지 않은 요일:', dayOfWeekEnglish);
          setWorkoutCache(prev => ({ ...prev, [dayOfWeek]: [] }));
          setWorkoutList([]);
          setLoading(false);
          return;
        }
      }
      
      console.log('🔍 캐시된 추천 데이터 사용, dayOfWeekEnglish:', dayOfWeekEnglish);
      console.log('🔍 캐시된 workouts:', workouts);
      setWorkoutCache(prev => ({ ...prev, [dayOfWeek]: workouts }));
      setWorkoutList(workouts);
      setLoading(false);
      return;
    } else {
      console.log('❌ 추천 데이터를 찾을 수 없음. hasRecommendations:', hasRecommendations, 'recommendations:', !!recommendations);
    }
    
    // 추천 데이터가 없으면 백엔드에서 기존 추천 데이터 먼저 확인
    if (!hasRecommendations) {
      try {
        console.log('🔍 백엔드에서 기존 운동 추천 데이터 확인 시도...');
        const currentUser = storageManager.getItem('currentUser');
        if (currentUser) {
          const response = await apiCall(`/api/workout-recommendation/${currentUser.id}?page=0&size=1`);
          
          if (response && response.success && response.data && response.data.content && response.data.content.length > 0) {
            const latestWorkout = response.data.content[0];
            console.log('✅ 백엔드에서 기존 운동 추천 데이터 발견:', latestWorkout);
            
            // 백엔드 데이터를 캐시된 데이터와 동일한 구조로 변환
            const workoutData = latestWorkout.workouts || latestWorkout;
            const processedWorkouts = workoutData || {
              Sunday: [], Monday: [], Tuesday: [], Wednesday: [], Thursday: [], Friday: [], Saturday: []
            };
            
            const dayOfWeekEnglish = dayNamesEnglish[selectedDate.getDay()];
            let workouts = processedWorkouts[dayOfWeekEnglish] || [];
            
            // 설문에서 선택된 요일 확인
            let selectedDays = survey?.selectedDaysEn || [];
            if (!selectedDays || selectedDays.length === 0) {
              const dayMap = {
                "월요일": "Monday", "화요일": "Tuesday", "수요일": "Wednesday",
                "목요일": "Thursday", "금요일": "Friday", "토요일": "Saturday", "일요일": "Sunday",
                "월": "Monday", "화": "Tuesday", "수": "Wednesday",
                "목": "Thursday", "금": "Friday", "토": "Saturday", "일": "Sunday"
              };
              const koreanDays = survey?.selectedDays || survey?.workoutDays || [];
              selectedDays = koreanDays.map(day => dayMap[day] || day);
            }
            
            const isSelectedDay = selectedDays.includes(dayOfWeekEnglish);
            
            // 이미 해당 요일의 추천 운동이 존재하면 설문 요일과 무관하게 표시
            // 추천이 비어있는 경우에만 설문 요일을 기준으로 빈 처리
            if (!workouts || workouts.length === 0) {
              if (!selectedDays || selectedDays.length === 0) {
                const defaultWorkoutDays = ['Monday', 'Wednesday', 'Friday'];
                const isDefaultWorkoutDay = defaultWorkoutDays.includes(dayOfWeekEnglish);
                if (!isDefaultWorkoutDay) {
                  workouts = [];
                }
              } else if (!isSelectedDay) {
                workouts = [];
              }
            }
            
            setWorkoutCache(prev => ({ ...prev, [dayOfWeek]: workouts }));
            setWorkoutList(workouts);
            setLoading(false);
            
            // 백엔드 데이터를 sessionStorage에 저장
            try {
              const userKey = `recommendations_${currentUser.id}`;
              const dataToSave = {
                workouts: processedWorkouts,
                inbody: inbody,
                survey: survey,
                createdAt: new Date().toISOString()
              };
              storageManager.setItem(userKey, dataToSave);
              console.log('✅ 백엔드 데이터를 sessionStorage에 저장:', userKey);
            } catch (e) {
              console.error('❌ sessionStorage 저장 실패:', e);
            }
            
            return;
          }
        }
      } catch (error) {
        console.error('❌ 백엔드 기존 추천 데이터 조회 실패:', error);
      }
    }
    
    // 추천 데이터가 없으면 설문 페이지로 안내 (새 추천 생성 안함)
    console.log('📝 운동 추천 데이터가 없습니다. 설문 페이지에서 추천을 받아주세요.');
    setError('운동 추천 데이터가 없습니다. 설문조사를 완료하여 추천을 받아주세요.');
    setWorkoutList([]);
    setLoading(false);
    return;
    } catch (error) {
      console.error('❌ 날짜 클릭 처리 중 오류:', error);
      setError('운동 데이터를 불러오는 중 오류가 발생했습니다: ' + error.message);
      setWorkoutList([]);
      setLoading(false);
    }
  };

  // 운동별 체크박스
  const handleWorkoutCheck = async (dateKey, workoutName, value) => {
    try {
      // GPT API 추천 결과에서 해당 운동의 상세 정보 찾기
      const workoutDetails = workoutList.find(workout => workout.name === workoutName);
      
      const requestData = {
        userId: getCurrentUserId(),
        date: dateKey,
        type: 'workout',
        workoutName: workoutName,
        completed: value,
        // GPT API 추천 결과의 상세 정보 추가
        ...(workoutDetails && {
          workoutDetails: {
            name: workoutDetails.name,
            description: workoutDetails.description,
            duration: workoutDetails.duration,
            intensity: workoutDetails.intensity,
            type: workoutDetails.type,
            // GPT API에서 추천된 운동임을 표시
            source: 'GPT_API_RECOMMENDATION',
            recommendedAt: new Date().toISOString()
          }
        })
      };
      
      console.log('🔍 운동 완료 상태 저장 요청 (GPT 추천 결과 포함):', requestData);
      
      // 백엔드에 히스토리 저장
      const response = await apiCall('/api/users/history', {
        method: 'POST',
        body: JSON.stringify(requestData)
      });
      
      console.log('🔍 운동 완료 상태 저장 응답:', response);

      // 로컬 상태 업데이트
      setChecked(prev => ({
        ...prev,
        [dateKey]: {
          ...(prev[dateKey] || {}),
          [workoutName]: value
        }
      }));
      
      console.log('✅ 운동 완료 상태 저장 성공 (GPT 추천 결과 포함):', { dateKey, workoutName, value, workoutDetails });
    } catch (error) {
      console.error('❌ 운동 완료 상태 저장 실패:', error);
      alert('운동 완료 상태 저장에 실패했습니다: ' + error.message);
    }
  };

  // 커스텀 캘린더 타일 클래스
  const tileClassName = ({ date, view }) => {
    if (view === 'month') {
      const key = date.toISOString().slice(0, 10);
      const isToday = key === new Date().toISOString().slice(0, 10);
      const dayChecked = checked[key];
      
      // 해당 날짜에 운동이 있고 모두 완료된 경우
      if (dayChecked && Object.keys(dayChecked).length > 0) {
        const completedCount = Object.values(dayChecked).filter(Boolean).length;
        const totalCount = Object.keys(dayChecked).length;
        
        console.log(`🔍 날짜 ${key} 스타일링: completedCount=${completedCount}, totalCount=${totalCount}`);
        
        if (completedCount === totalCount && totalCount > 0) {
          // 모든 운동 완료: 초록색 배경, 검은색 글자로 가독성 향상
          const className = 'bg-green-500 text-gray-900 font-bold rounded border-2 border-green-600 relative shadow-lg hover:bg-green-600 transition-colors duration-200';
          console.log(`✅ 모든 운동 완료 스타일 적용: ${key}`, className);
          return className;
        } else if (completedCount > 0) {
          // 일부 운동 완료: 노란색 배경, 검은색 글자
          const className = 'bg-yellow-400 text-gray-900 font-bold rounded border-2 border-yellow-500 relative shadow-lg hover:bg-yellow-500 transition-colors duration-200';
          console.log(`🟡 일부 운동 완료 스타일 적용: ${key}`, className);
          return className;
        }
      }
      
      if (isToday) {
        return 'bg-blue-100 text-blue-700 font-bold rounded border-2 border-blue-400';
      }
    }
    return '';
  };

  // 완료율 계산
  const doneCount = workoutList.filter(w => checked[selectedDateKey]?.[w.name]).length;
  const totalCount = workoutList.length;
  
  // 디버깅용 로그
  console.log('🔍 캘린더 페이지 상태:', {
    selectedDate: selectedDate,
    selectedDateKey: selectedDateKey,
    workoutListLength: workoutList.length,
    workoutList: workoutList,
    checkedData: checked,
    loading: loading,
    error: error,
    workoutCache: workoutCache
  });

  return (
    <Layout>
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-purple-50 to-pink-50 py-16">
        <div className="w-full max-w-5xl mx-auto px-6">
          {/* 헤더 */}
          <div className="mb-12 text-center">
            <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-r from-blue-500 to-indigo-600 rounded-full mb-6 shadow-lg">
              <span className="text-3xl">📅</span>
            </div>
            <h1 className="text-4xl font-bold text-gray-800 mb-4">운동기록 캘린더</h1>
            <p className="text-gray-600 text-lg max-w-2xl mx-auto">
              날짜를 클릭하면 해당 날짜의 운동 계획과 완료 여부를 확인할 수 있습니다
            </p>
          </div>

          {/* 캘린더 카드 */}
          <div className="bg-white rounded-3xl shadow-xl border border-gray-100 p-8 mb-8">
        
        {historyLoading && (
          <div className="text-center mb-4 text-blue-500">히스토리를 불러오는 중...</div>
        )}
        
        <div className="flex flex-col md:flex-row gap-10 items-start justify-center">
          <div className="flex-1 flex justify-center">
            <Calendar
              onClickDay={handleDayClick}
              value={selectedDate}
              onChange={setSelectedDate}
              tileClassName={tileClassName}
              calendarType="gregory"
              className="w-full border-0 shadow-lg rounded-2xl p-4 bg-gradient-to-br from-blue-50 to-pink-50"
              style={{
                '--react-calendar__tile': 'display: flex; align-items: center; justify-content: center; height: 40px; min-height: 40px; color: #374151 !important;',
                '--react-calendar__tile--active': 'background-color: #3b82f6 !important; color: white !important;',
                '--react-calendar__tile--now': 'background-color: #dbeafe !important; color: #1e40af !important;',
                '--react-calendar__tile--hasActive': 'background-color: #dbeafe !important; color: #1e40af !important;',
              }}
              tileDisabled={({ date }) => {
                // 모든 날짜를 활성화 (비활성화 없음)
                return false;
              }}
              tileContent={({ date, view }) => {
                if (view === 'month') {
                  const key = date.toISOString().slice(0, 10);
                  const dayChecked = checked[key];
                  
                  if (dayChecked && Object.keys(dayChecked).length > 0) {
                    const completedCount = Object.values(dayChecked).filter(Boolean).length;
                    const totalCount = Object.keys(dayChecked).length;
                    
                    if (completedCount === totalCount && totalCount > 0) {
                      // 모든 운동 완료: 체크 아이콘 표시
                      return (
                        <div className="absolute top-0 right-0 w-4 h-4 bg-green-600 rounded-full flex items-center justify-center shadow-md">
                          <span className="text-white text-xs font-bold">✓</span>
                        </div>
                      );
                    } else if (completedCount > 0) {
                      // 일부 운동 완료: 진행률 표시
                      return (
                        <div className="absolute top-0 right-0 w-4 h-4 bg-yellow-500 rounded-full flex items-center justify-center shadow-md">
                          <span className="text-gray-900 text-xs font-bold">{completedCount}</span>
                        </div>
                      );
                    }
                  }
                }
                return null;
              }}
            />
          </div>
          <div className="flex-1">
            {/* 선택된 날짜 정보 */}
            <div className="mb-6 p-6 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-2xl border border-blue-200">
              <div className="flex items-center gap-3 mb-2">
                <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full flex items-center justify-center">
                  <span className="text-white text-lg">📅</span>
                </div>
                <h3 className="text-xl font-bold text-gray-800">
                  {selectedDate.getFullYear()}년 {selectedDate.getMonth() + 1}월 {selectedDate.getDate()}일 ({dayNames[selectedDate.getDay()]})
                </h3>
              </div>
              <p className="text-gray-600 text-sm ml-13">선택한 날짜의 운동 계획을 확인하고 완료 여부를 체크하세요</p>
            </div>

            {loading && (
              <div className="text-center mt-6 p-8 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-2xl border border-blue-200">
                <div className="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                <div className="text-blue-600 font-semibold mb-2">운동 데이터를 불러오는 중...</div>
                <div className="text-blue-500 text-sm">
                  AI가 맞춤형 운동을 추천하고 있습니다. 최대 2-3분 정도 소요될 수 있습니다.
                </div>
                <div className="mt-3 text-xs text-gray-500">
                  💡 팁: 이 과정은 한 번만 수행되며, 이후에는 빠르게 로드됩니다.
                </div>
              </div>
            )}
            
            {error && (
              <div className="text-center mt-6 p-6 bg-gradient-to-r from-red-50 to-pink-50 rounded-2xl border border-red-200">
                <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-2xl">⚠️</span>
                </div>
                <div className="text-red-600 font-semibold mb-2">오류 발생</div>
                <div className="text-red-500 text-sm mb-4">{error}</div>
                <div className="flex gap-2 justify-center">
                  <button
                    onClick={() => {
                      console.log('🔍 재시도 버튼 클릭');
                      setError('');
                      handleDayClick(selectedDate);
                    }}
                    className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition text-sm"
                  >
                    🔄 재시도
                  </button>
                  <button
                    onClick={() => {
                      console.log('🔍 운동 추천 받기 버튼 클릭');
                      navigate('/survey');
                    }}
                    className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition text-sm"
                  >
                    📝 운동 추천 받기
                  </button>
                </div>
              </div>
            )}
            
            {workoutList.length > 0 && !loading && !error && (
              <div className="mt-2">
                <div className="flex items-center justify-between mb-4">
                  <h4 className="text-lg font-bold text-gray-800">🏋️ 추천 운동</h4>
                  <div className="bg-white px-4 py-2 rounded-full border border-gray-200 shadow-sm">
                    <span className="text-sm font-semibold text-gray-600">
                      완료: <span className="text-green-600">{doneCount}</span> / <span className="text-gray-800">{totalCount}</span>
                      <span className="ml-2 text-blue-600">({totalCount > 0 ? Math.round(doneCount/totalCount*100) : 0}%)</span>
                    </span>
                  </div>
                </div>
                
                <ul className="space-y-3">
                  {workoutList.map(w => (
                    <li key={w.name} className={`flex items-center gap-4 bg-white rounded-xl p-4 shadow-md border-2 transition-all duration-200 ${
                      checked[selectedDateKey]?.[w.name] 
                        ? 'border-green-400 bg-green-50 shadow-green-100' 
                        : 'border-gray-200 hover:border-blue-300 hover:bg-blue-50 hover:shadow-blue-100'
                    }`}>
                      <input
                        type="checkbox"
                        checked={!!checked[selectedDateKey]?.[w.name]}
                        onChange={e => handleWorkoutCheck(selectedDateKey, w.name, e.target.checked)}
                        className="accent-green-500 w-6 h-6 cursor-pointer"
                      />
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <span className="font-bold text-lg text-gray-800">{w.name}</span>
                          {checked[selectedDateKey]?.[w.name] && (
                            <span className="text-green-600 text-sm font-semibold">✅ 완료</span>
                          )}
                        </div>
                        <p className="text-gray-600 text-sm">{w.description}</p>
                        {w.duration && (
                          <div className="flex items-center gap-4 mt-2 text-xs text-gray-500">
                            <span>⏱️ {w.duration}분</span>
                            {w.intensity && <span>💪 {w.intensity}</span>}
                            {w.type && <span>🏃 {w.type}</span>}
                          </div>
                        )}
                      </div>
                    </li>
                  ))}
                </ul>
                
                {doneCount === totalCount && totalCount > 0 && (
                  <div className="mt-6 p-4 bg-green-100 rounded-xl border border-green-300 text-center">
                    <div className="text-green-700 font-bold text-lg">🎉 모든 운동을 완료했습니다!</div>
                    <div className="text-green-600 text-sm mt-1">훌륭한 하루였네요!</div>
                  </div>
                )}
              </div>
            )}
            
            {workoutList.length === 0 && !loading && !error && (
              <div className="text-center mt-6 p-8 bg-gray-50 rounded-xl border border-gray-200">
                <div className="text-gray-500 text-lg mb-2">📝 운동 계획이 없습니다</div>
                <div className="text-gray-400 text-sm mb-4">
                  {(() => {
                    const dayOfWeek = dayNames[selectedDate.getDay()];
                    const dayOfWeekEnglish = dayNamesEnglish[selectedDate.getDay()];
                    const defaultWorkoutDays = ['Monday', 'Wednesday', 'Friday'];
                    
                    if (defaultWorkoutDays.includes(dayOfWeekEnglish)) {
                      return `${dayOfWeek}은 기본 운동 요일입니다. 운동 추천을 받아보세요.`;
                    } else {
                      return `이 날짜(${dayOfWeek})는 운동 요일이 아닙니다.`;
                    }
                  })()}
                </div>
                <div className="flex gap-2 justify-center">
                  <button
                    onClick={() => {
                      console.log('🔍 운동 추천 받기 버튼 클릭');
                      navigate('/survey');
                    }}
                    className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition text-sm"
                  >
                    운동 추천 받기
                  </button>
                  <button
                    onClick={() => {
                      console.log('🔍 운동 목록 보기 버튼 클릭');
                      navigate('/recommended-workout-list');
                    }}
                    className="bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition text-sm"
                  >
                    운동 목록 보기
                  </button>
                  <button
                    onClick={() => {
                      console.log('🔍 운동 데이터 새로고침 버튼 클릭');
                      handleDayClick(selectedDate);
                    }}
                    className="bg-purple-500 text-white px-4 py-2 rounded-lg hover:bg-purple-600 transition text-sm"
                  >
                    새로고침
                  </button>
                </div>
              </div>
            )}
          </div>
          </div>
          
          {/* 범례 */}
          <div className="bg-white rounded-3xl shadow-xl border border-gray-100 p-6 mt-8">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-8 h-8 bg-gradient-to-r from-blue-400 to-indigo-500 rounded-full flex items-center justify-center">
                <span className="text-white text-sm">ℹ️</span>
              </div>
              <h3 className="text-lg font-semibold text-gray-800">범례</h3>
            </div>
            <div className="flex justify-center gap-4 flex-wrap">
              <span className="inline-flex items-center gap-2 bg-gradient-to-r from-green-100 to-emerald-100 text-green-700 px-4 py-2 rounded-full font-medium shadow border border-green-200">
                <span className="w-3 h-3 bg-green-500 rounded-full"></span>
                완료: 모든 운동 완료
              </span>
              <span className="inline-flex items-center gap-2 bg-gradient-to-r from-yellow-100 to-amber-100 text-yellow-700 px-4 py-2 rounded-full font-medium shadow border border-yellow-200">
                <span className="w-3 h-3 bg-yellow-400 rounded-full"></span>
                진행: 일부 운동 완료
              </span>
              <span className="inline-flex items-center gap-2 bg-gradient-to-r from-blue-100 to-indigo-100 text-blue-700 px-4 py-2 rounded-full font-medium shadow border border-blue-200">
                <span className="w-3 h-3 bg-blue-500 rounded-full"></span>
                오늘: 오늘 날짜
              </span>
            </div>
          </div>
        </div>
        </div>
      </div>
    </Layout>
  );
} 