import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import Card from '../components/Card';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import HeroWithBg from '../components/HeroWithBg';
import SectionWithWave from '../components/SectionWithWave';
import DietIllustration from '../components/DietIllustration';
import { useUser, getUserData } from '../api/auth';
import { storageManager } from '../utils/storageManager';
import { getDietInfo, DIET_TYPES } from '../utils/dietUtils';

const days = [
  { key: 'Monday', label: '월요일' },
  { key: 'Tuesday', label: '화요일' },
  { key: 'Wednesday', label: '수요일' },
  { key: 'Thursday', label: '목요일' },
  { key: 'Friday', label: '금요일' }
];

export default function RecommendedDietListPage() {
  const location = useLocation();
  const { inbody: locationInbody, survey: locationSurvey, diets: passedDiets, fromRecommendations, fromHistory, dietData, recommendations } = location.state || {};
  const [selectedDay, setSelectedDay] = useState(days[0].key);
  const [diets, setDiets] = useState(passedDiets || {});
  const [loading, setLoading] = useState(!passedDiets); // 데이터 있으면 로딩 안함
  const [error, setError] = useState('');
  const [bodyType, setBodyType] = useState("");
  const [summary, setSummary] = useState("");
  const [dietMeta, setDietMeta] = useState(null); // 하루 전체 식단 요약 메타데이터
  const navigate = useNavigate();
  const [user, setUser] = useState(null);

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
    
    // 1순위: 히스토리에서 전달받은 식단 데이터가 있으면 사용
    if (fromHistory && dietData) {
      console.log('히스토리에서 전달받은 식단 데이터 사용:', dietData);
      console.log('🔍 dietData 구조:', {
        hasDiets: !!dietData.diets,
        hasMealStyle: !!dietData.mealStyle,
        hasSampleMenu: !!dietData.sampleMenu,
        keys: Object.keys(dietData)
      });
      
      // 히스토리 데이터를 캐시된 데이터와 동일한 구조로 변환
      let historyDiets = {};
      
      // diets 필드가 있고 비어있지 않으면 그대로 사용 (캐시된 데이터와 동일한 구조)
      if (dietData.diets && typeof dietData.diets === 'object' && Object.keys(dietData.diets).length > 0) {
        console.log('✅ diets 필드 발견, 정리 중...');
        
        // diets 데이터 정리 함수: 요일별로 제대로 분리
        const normalizeDiets = (rawDiets) => {
          const normalized = {};
          const dayKeys = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];
          
          // 각 요일별로 데이터 정리
          dayKeys.forEach(dayKey => {
            if (rawDiets[dayKey]) {
              // 요일 데이터가 있으면 그대로 사용
              normalized[dayKey] = rawDiets[dayKey];
            } else {
              // 요일 데이터가 없으면 빈 객체로 초기화
              normalized[dayKey] = {};
            }
          });
          
          // 잘못된 구조 수정: 다른 요일 안에 포함된 요일 데이터를 찾아서 분리
          dayKeys.forEach(dayKey => {
            dayKeys.forEach(otherDayKey => {
              if (dayKey !== otherDayKey && rawDiets[otherDayKey] && rawDiets[otherDayKey][dayKey]) {
                console.log(`⚠️ ${otherDayKey} 안에 ${dayKey} 발견, 분리 중...`);
                normalized[dayKey] = rawDiets[otherDayKey][dayKey];
                // 원본에서 제거
                delete rawDiets[otherDayKey][dayKey];
              }
            });
          });
          
          return normalized;
        };
        
        historyDiets = normalizeDiets(dietData.diets);
      } else if (dietData.sampleMenu || dietData.mealStyle || dietData.dailyCalories) {
        console.log('✅ 단일 식단 구조 발견, 요일별로 변환');
        
        // sampleMenu를 meals 배열로 변환
        let meals = [];
        let sampleMenuData = dietData.sampleMenu;
        
        // sampleMenu가 JSON 문자열인 경우 파싱
        if (typeof sampleMenuData === 'string') {
          try {
            sampleMenuData = JSON.parse(sampleMenuData);
            console.log('✅ sampleMenu JSON 파싱 성공:', sampleMenuData);
          } catch (e) {
            console.log('⚠️ sampleMenu는 일반 문자열:', sampleMenuData);
          }
        }
        
        if (sampleMenuData && typeof sampleMenuData === 'object') {
          // sampleMenu가 객체 형태인 경우 (breakfast, lunch, dinner 등)
          const mealTypes = ['breakfast', 'lunch', 'dinner', 'snack'];
          meals = mealTypes.filter(type => sampleMenuData[type]).map(type => ({
            name: type === 'breakfast' ? '아침' : 
                  type === 'lunch' ? '점심' : 
                  type === 'dinner' ? '저녁' : '간식',
            description: sampleMenuData[type],
            calories: Math.floor((parseFloat(dietData.dailyCalories) || 2000) / mealTypes.filter(t => sampleMenuData[t]).length),
            nutrients: {
              carbs: 50,
              protein: 30,
              fat: 20
            }
          }));
        } else if (typeof sampleMenuData === 'string') {
          // sampleMenu가 문자열인 경우
          meals = [{
            name: '식단',
            description: sampleMenuData,
            calories: parseFloat(dietData.dailyCalories) || 2000,
            nutrients: {
              carbs: 250,
              protein: 120,
              fat: 80
            }
          }];
        }
        
        // 모든 요일에 동일한 식단 할당
        days.forEach(day => {
          historyDiets[day.key] = meals.length > 0 ? meals : [{
            name: dietData.mealStyle || '식단 계획',
            description: '개인 맞춤 식단을 추천합니다.',
            calories: dietData.dailyCalories || dietData.calories || 2000,
            nutrients: {
              carbs: dietData.carbs || 250,
              protein: dietData.protein || 120,
              fat: dietData.fat || 80
            }
          }];
        });
      } else {
        console.warn('⚠️ 식단 데이터 구조를 파악할 수 없음, 기본 구조 사용');
        // 기본 구조 생성
        const defaultDiet = [{
          name: '식단 계획',
          description: '식단 정보를 확인할 수 없습니다.',
          calories: 2000,
          nutrients: {
            carbs: 250,
            protein: 120,
            fat: 80
          }
        }];
        
        days.forEach(day => {
          historyDiets[day.key] = defaultDiet;
        });
      }
      
      console.log('🔍 변환된 historyDiets:', historyDiets);
      
      setDiets(historyDiets);
      setBodyType(dietData.bodyType || '체형 분석 완료');
      setSummary(dietData.summary || '개인 맞춤 식단 추천이 준비되었습니다.');
      // 하루 전체 식단 요약 메타데이터 저장
      setDietMeta({
        mealStyle: dietData.mealStyle,
        dailyCalories: dietData.dailyCalories,
        macroSplit: dietData.macroSplit,
        sampleMenu: dietData.sampleMenu,
        shoppingList: dietData.shoppingList,
        precautions: dietData.precautions,
        mealTiming: dietData.mealTiming,
        hydration: dietData.hydration,
        supplements: dietData.supplements,
      });
      
      // 이미지 로딩
      loadDietImages(historyDiets);
      setLoading(false);
      return;
    }

    // 2순위: 추천에서 전달받은 식단 데이터가 있으면 사용 (인바디/설문 체크보다 우선)
    if (fromRecommendations && passedDiets) {
      console.log('추천에서 전달받은 식단 데이터 사용:', passedDiets);
      console.log('🔍 fromRecommendations 데이터 구조 분석:', {
        hasDiets: !!passedDiets,
        isObject: typeof passedDiets === 'object',
        keys: Object.keys(passedDiets || {})
      });
      console.log('🔍 location.state 전체:', location.state);
      console.log('🔍 dietData 확인:', dietData);
      console.log('🔍 recommendations 확인:', recommendations);
      console.log('🔍 dietData 타입:', typeof dietData);
      console.log('🔍 dietData 키:', dietData ? Object.keys(dietData) : 'null');
      console.log('🔍 recommendations 키:', recommendations ? Object.keys(recommendations) : 'null');
      
      // recommendations 객체에서 메타데이터 확인
      if (recommendations) {
        console.log('🔍 recommendations 메타데이터:', {
          hasMealStyle: !!recommendations.mealStyle,
          hasShoppingList: !!recommendations.shoppingList,
          hasMealTiming: !!recommendations.mealTiming,
          hasHydration: !!recommendations.hydration,
          hasSupplements: !!recommendations.supplements,
          mealStyle: recommendations.mealStyle,
          shoppingList: recommendations.shoppingList
        });
      }
      
      // diets 데이터 정리 함수: 요일별로 제대로 분리
      const normalizeDiets = (rawDiets) => {
        const normalized = {};
        const dayKeys = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];
        
        // 각 요일별로 데이터 정리
        dayKeys.forEach(dayKey => {
          if (rawDiets[dayKey]) {
            // 요일 데이터가 있으면 그대로 사용
            normalized[dayKey] = rawDiets[dayKey];
          } else {
            // 요일 데이터가 없으면 빈 객체로 초기화
            normalized[dayKey] = {};
          }
        });
        
        // 잘못된 구조 수정: 다른 요일 안에 포함된 요일 데이터를 찾아서 분리
        dayKeys.forEach(dayKey => {
          dayKeys.forEach(otherDayKey => {
            if (dayKey !== otherDayKey && rawDiets[otherDayKey] && rawDiets[otherDayKey][dayKey]) {
              console.log(`⚠️ ${otherDayKey} 안에 ${dayKey} 발견, 분리 중...`);
              normalized[dayKey] = rawDiets[otherDayKey][dayKey];
              // 원본에서 제거
              delete rawDiets[otherDayKey][dayKey];
            }
          });
        });
        
        return normalized;
      };
      
      // diets가 요일별 구조가 아닌 경우 변환
      let normalizedDiets = passedDiets;
      if (!normalizedDiets.Monday && !normalizedDiets.Tuesday && !normalizedDiets.Wednesday && !normalizedDiets.Thursday && !normalizedDiets.Friday) {
        // 단일 식단 구조인 경우 요일별로 변환
        // 메타데이터는 location.state.dietData에서 가져오기
        const metaSource = dietData || passedDiets;
        const dietProgram = {
          name: metaSource.mealStyle || '식단 계획',
          description: metaSource.sampleMenu || metaSource.description || '개인 맞춤 식단을 추천합니다.',
          calories: metaSource.dailyCalories || metaSource.calories || 2000,
          nutrients: {
            carbs: metaSource.carbs || 250,
            protein: metaSource.protein || 120,
            fat: metaSource.fat || 80
          },
          type: 'balanced',
          mealTiming: metaSource.mealTiming,
          hydration: metaSource.hydration,
          precautions: metaSource.precautions,
          supplements: metaSource.supplements,
          shoppingList: metaSource.shoppingList,
          macroSplit: metaSource.macroSplit
        };
        
        // 모든 요일에 동일한 식단 할당
        normalizedDiets = {};
        days.forEach(day => {
          normalizedDiets[day.key] = [dietProgram];
        });
      } else {
        // 요일별 구조가 있으면 정리
        normalizedDiets = normalizeDiets(passedDiets);
      }
      
      setDiets(normalizedDiets);
      setBodyType('체형 분석 완료');
      setSummary('개인 맞춤 식단 추천이 준비되었습니다.');
      
      // 메타데이터는 location.state.recommendations 또는 dietData에서 가져오기
      // recommendations 객체가 최우선 (전체 메타데이터 포함)
      // dietData가 두 번째 우선순위
      // passedDiets는 마지막 fallback (메타데이터 없을 가능성 높음)
      // 각 필드별로 우선순위를 적용하여 최대한 많은 메타데이터를 수집
      const getMetaValue = (key) => {
        if (recommendations && recommendations[key] !== undefined && recommendations[key] !== null) {
          return recommendations[key];
        }
        if (dietData && dietData[key] !== undefined && dietData[key] !== null) {
          return dietData[key];
        }
        if (passedDiets && passedDiets[key] !== undefined && passedDiets[key] !== null) {
          return passedDiets[key];
        }
        return undefined;
      };
      
      console.log('🔍 메타데이터 소스:', { 
        hasRecommendations: !!recommendations,
        hasDietData: !!dietData, 
        hasPassedDiets: !!passedDiets,
        recommendationsKeys: recommendations ? Object.keys(recommendations) : [],
        dietDataKeys: dietData ? Object.keys(dietData) : [],
        mealStyle: getMetaValue('mealStyle'),
        shoppingList: getMetaValue('shoppingList'),
        mealTiming: getMetaValue('mealTiming'),
        hydration: getMetaValue('hydration'),
        supplements: getMetaValue('supplements')
      });
      
      setDietMeta({
        mealStyle: getMetaValue('mealStyle'),
        dailyCalories: getMetaValue('dailyCalories'),
        macroSplit: getMetaValue('macroSplit'),
        sampleMenu: getMetaValue('sampleMenu'),
        shoppingList: getMetaValue('shoppingList'),
        precautions: getMetaValue('precautions'),
        mealTiming: getMetaValue('mealTiming'),
        hydration: getMetaValue('hydration'),
        supplements: getMetaValue('supplements'),
      });
      
      // 이미지 로딩
      loadDietImages(dietData);
      setLoading(false);
      return;
    }

    // 3순위: 이미 전달받은 식단 데이터가 있으면 사용
    if (passedDiets && Object.keys(passedDiets).length > 0) {
      console.log('전달받은 식단 데이터 사용:', passedDiets);
      
      // diets 데이터 정리 함수: 요일별로 제대로 분리
      const normalizeDiets = (rawDiets) => {
        const normalized = {};
        const dayKeys = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];
        
        // 각 요일별로 데이터 정리
        dayKeys.forEach(dayKey => {
          if (rawDiets[dayKey]) {
            // 요일 데이터가 있으면 그대로 사용
            normalized[dayKey] = rawDiets[dayKey];
          } else {
            // 요일 데이터가 없으면 빈 객체로 초기화
            normalized[dayKey] = {};
          }
        });
        
        // 잘못된 구조 수정: 다른 요일 안에 포함된 요일 데이터를 찾아서 분리
        dayKeys.forEach(dayKey => {
          dayKeys.forEach(otherDayKey => {
            if (dayKey !== otherDayKey && rawDiets[otherDayKey] && rawDiets[otherDayKey][dayKey]) {
              console.log(`⚠️ ${otherDayKey} 안에 ${dayKey} 발견, 분리 중...`);
              normalized[dayKey] = rawDiets[otherDayKey][dayKey];
              // 원본에서 제거
              delete rawDiets[otherDayKey][dayKey];
            }
          });
        });
        
        return normalized;
      };
      
      const normalizedDiets = normalizeDiets(passedDiets);
      setDiets(normalizedDiets);
      setBodyType('');
      setSummary('');
      
      // 이미지 로딩
      loadDietImages(normalizedDiets);
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
          if (cachedData.diets && Object.keys(cachedData.diets).length > 0) {
            console.log('currentUser ID 기반 캐시된 식단 데이터 사용:', userKey, cachedData.diets);
            setDiets(cachedData.diets);
            setDietMeta({
              mealStyle: cachedData.mealStyle,
              dailyCalories: cachedData.dailyCalories,
              macroSplit: cachedData.macroSplit,
              sampleMenu: cachedData.sampleMenu,
              shoppingList: cachedData.shoppingList,
              precautions: cachedData.precautions,
              mealTiming: cachedData.mealTiming,
              hydration: cachedData.hydration || cachedData.hydrationGuide,
              supplements: cachedData.supplements,
            });
            
            // 체형 분석 결과도 함께 조회
            const bodyAnalysisKey = `bodyAnalysis_${currentUser.id}`;
            const bodyAnalysisData = storageManager.getItem(bodyAnalysisKey);
            if (bodyAnalysisData) {
              setBodyType(bodyAnalysisData.bodyType || '체형 분석 완료');
              setSummary(bodyAnalysisData.summary || '개인 맞춤 식단 추천이 준비되었습니다.');
            } else {
              setBodyType('체형 분석 완료');
              setSummary('개인 맞춤 식단 추천이 준비되었습니다.');
            }
            
            // 이미지 로딩 (현재는 SVG 일러스트 사용)
            loadDietImages(cachedData.diets);
            setLoading(false);
            return;
          }
        }
      }
      
      // 기본 키로 fallback
      const cachedData = storageManager.getItem('recommendations');
      if (cachedData) {
        if (cachedData.diets && Object.keys(cachedData.diets).length > 0) {
          console.log('기본 키 캐시된 식단 데이터 사용:', cachedData.diets);
          setDiets(cachedData.diets);
          setDietMeta({
            mealStyle: cachedData.mealStyle,
            dailyCalories: cachedData.dailyCalories,
            macroSplit: cachedData.macroSplit,
            sampleMenu: cachedData.sampleMenu,
            shoppingList: cachedData.shoppingList,
            precautions: cachedData.precautions,
            mealTiming: cachedData.mealTiming,
            hydration: cachedData.hydration || cachedData.hydrationGuide,
            supplements: cachedData.supplements,
          });
          
          // 체형 분석 결과도 함께 조회 (기본 키)
          const bodyAnalysisData = storageManager.getItem('bodyAnalysis');
          if (bodyAnalysisData) {
            setBodyType(bodyAnalysisData.bodyType || '체형 분석 완료');
            setSummary(bodyAnalysisData.summary || '개인 맞춤 식단 추천이 준비되었습니다.');
          } else {
            setBodyType('체형 분석 완료');
            setSummary('개인 맞춤 식단 추천이 준비되었습니다.');
          }
          
          // 이미지 로딩 (현재는 SVG 일러스트 사용)
          loadDietImages(cachedData.diets);
          setLoading(false);
          return;
        }
      }
    } catch (e) {
      console.error('캐시된 식단 데이터 로드 실패:', e);
    }

    // 추천 데이터가 없으면 설문 페이지로 안내 (새 추천 생성 안함)
    console.log('📝 식단 추천 데이터가 없습니다. 설문 페이지에서 추천을 받아주세요.');
    setError('식단 추천 데이터가 없습니다. 설문조사를 완료하여 추천을 받아주세요.');
    setLoading(false);
  }, [navigate, passedDiets]);

  // 식단 이미지는 외부 사진 대신 일러스트로 표시하므로
  // 기존 Unsplash 기반 이미지 로딩 함수는 더 이상 사용하지 않음
  const loadDietImages = () => {};

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
      if (cachedData) {
        if (cachedData.diets && Object.keys(cachedData.diets).length > 0) {
          hasRecommendations = true;
        }
      }
    }
    
    // 기본 키로도 확인
    if (!hasRecommendations) {
      const cachedData = storageManager.getItem('recommendations');
      if (cachedData) {
        if (cachedData.diets && Object.keys(cachedData.diets).length > 0) {
          hasRecommendations = true;
        }
      }
    }
  } catch (e) {
    console.error('추천 데이터 확인 실패:', e);
  }
  
  // 네비게이션으로 직접 전달된 추천 데이터도 인정
  if (!hasRecommendations && fromRecommendations && passedDiets) {
    hasRecommendations = true;
  }
  // 히스토리에서 직접 전달된 추천 데이터도 인정
  if (!hasRecommendations && fromHistory && dietData) {
    hasRecommendations = true;
  }
  // 이미 state에 세팅된 식단 데이터가 있으면 추천 존재로 간주
  if (!hasRecommendations && diets && Object.keys(diets || {}).length > 0) {
    hasRecommendations = true;
  }
  
  // 추천 데이터가 없고, 인바디/설문 데이터도 없을 때만 에러 표시
  if (!hasRecommendations && (!inbody || !survey)) {
    return (
      <Layout>
        <div className="min-h-screen bg-gradient-to-br from-green-50 via-emerald-50 to-teal-50 py-16">
          <div className="w-full max-w-4xl mx-auto px-6">
            <div className="text-center py-16">
              <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-r from-green-500 to-emerald-600 rounded-full mb-6">
                <span className="text-3xl">🍽️</span>
              </div>
              <h2 className="text-2xl font-bold text-gray-800 mb-4">식단 추천 데이터가 없습니다</h2>
              <p className="text-gray-600 mb-8 max-w-md mx-auto">
                맞춤형 식단 추천을 받으려면 인바디 데이터와 설문 데이터가 필요합니다.
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

  // shoppingList는 백엔드/히스토리 구조에 따라 문자열 또는 배열로 올 수 있으므로 안전하게 배열로 변환
  const shoppingList = dietMeta && dietMeta.shoppingList
    ? (Array.isArray(dietMeta.shoppingList)
        ? dietMeta.shoppingList
        : String(dietMeta.shoppingList)
            .split(/[\n,]/)
            .map(item => item.trim())
            .filter(Boolean))
    : [];

  return (
    <Layout>
      <div className="min-h-screen bg-gradient-to-br from-green-50 via-teal-50 to-blue-50 py-16">
        <div className="w-full max-w-4xl mx-auto px-6">
        {/* 헤더 */}
        <div className="mb-12 text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-green-500 to-teal-600 rounded-full mb-4">
            <span className="text-2xl">🍽️</span>
          </div>
          <h1 className="text-3xl font-bold text-gray-800 mb-3">추천 식단</h1>
          <p className="text-gray-500 mb-6">AI가 분석한 체형·목표를 바탕으로 맞춤형 식단을 제안합니다</p>
          <div className="flex items-center justify-center gap-4">
            <button 
              onClick={() => navigate(-1)} 
              className="inline-flex items-center gap-2 text-sm text-blue-600 hover:text-blue-700 transition-colors"
            >
              <span className="text-lg">←</span>
              뒤로가기
            </button>
            {bodyType && (
              <div className="inline-flex items-center gap-2 bg-gradient-to-r from-green-100 to-emerald-100 text-green-800 px-4 py-2 rounded-full text-sm font-medium border border-green-200">
                <span className="text-lg">📊</span>
                체형 분류: {bodyType}
              </div>
            )}
          </div>
        </div>
        
        <div className="max-w-2xl mx-auto text-center mb-8">
          <p className="text-gray-600">
            하루 권장 칼로리, 영양소 비율을 고려한 맞춤 식단을 확인하고 건강하게 식사하세요
          </p>
        </div>

        {/* 하루 전체 식단 요약 메타데이터 */}
        {dietMeta && (
          <div className="mb-10 space-y-4">
            <div className="grid md:grid-cols-3 gap-4">
              <div className="bg-white/80 rounded-2xl shadow-sm border border-gray-100 p-4 text-left">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-lg">🥗</span>
                  <span className="text-sm font-semibold text-gray-700">식단 스타일</span>
                </div>
                <p className="text-sm text-gray-700 leading-relaxed">
                  {dietMeta.mealStyle || '체형과 목표에 맞춘 건강 식단입니다.'}
                </p>
              </div>
              <div className="bg-white/80 rounded-2xl shadow-sm border border-gray-100 p-4 text-left">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-lg">🔥</span>
                  <span className="text-sm font-semibold text-gray-700">하루 칼로리 · 비율</span>
                </div>
                <p className="text-sm text-gray-700">
                  {dietMeta.dailyCalories ? `${dietMeta.dailyCalories} kcal` : '칼로리 정보 없음'}
                </p>
                {dietMeta.macroSplit && (
                  <p className="text-xs text-gray-500 mt-1">
                    탄수화물 {dietMeta.macroSplit.carbs}% · 단백질 {dietMeta.macroSplit.protein}% · 지방 {dietMeta.macroSplit.fat}%
                  </p>
                )}
              </div>
              <div className="bg-white/80 rounded-2xl shadow-sm border border-gray-100 p-4 text-left">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-lg">💧</span>
                  <span className="text-sm font-semibold text-gray-700">섭취 타이밍 · 수분</span>
                </div>
                <p className="text-xs text-gray-600 mb-1">
                  {dietMeta.mealTiming || '아침·점심·저녁 규칙적인 식사를 권장합니다.'}
                </p>
                <p className="text-xs text-gray-600">
                  {dietMeta.hydration || '하루 1.5~2L 정도의 수분 섭취를 권장합니다.'}
                </p>
              </div>
            </div>

            {shoppingList.length > 0 && (
              <div className="bg-white/80 rounded-2xl shadow-sm border border-gray-100 p-4 text-left">
                <div className="flex items-center gap-2 mb-3">
                  <span className="text-lg">🛒</span>
                  <span className="text-sm font-semibold text-gray-700">추천 장보기 리스트</span>
                </div>
                <p className="text-xs text-gray-600 mb-2">
                  일주일 식단을 준비할 때 아래 식재료를 우선적으로 준비해 보세요.
                </p>
                <div className="flex flex-wrap gap-2">
                  {shoppingList.map((item, idx) => (
                    <span
                      key={idx}
                      className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 text-xs border border-emerald-100"
                    >
                      <span>✔️</span>
                      {item}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {(dietMeta.precautions || dietMeta.supplements) && (
              <div className="grid md:grid-cols-2 gap-4">
                {dietMeta.precautions && (
                  <div className="bg-red-50 rounded-2xl border border-red-100 p-4 text-left">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-lg">⚠️</span>
                      <span className="text-sm font-semibold text-red-800">주의사항</span>
                    </div>
                    <p className="text-xs text-red-700 leading-relaxed">
                      {dietMeta.precautions}
                    </p>
                  </div>
                )}
                {dietMeta.supplements && (
                  <div className="bg-blue-50 rounded-2xl border border-blue-100 p-4 text-left">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-lg">💊</span>
                      <span className="text-sm font-semibold text-blue-800">보충제 가이드</span>
                    </div>
                    <p className="text-xs text-blue-700 leading-relaxed">
                      {dietMeta.supplements}
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
                    ? 'bg-gradient-to-r from-green-500 to-teal-600 text-white shadow-lg transform scale-105' 
                    : 'text-gray-700 hover:bg-gray-100 hover:scale-105'
                }`}
              >
                <span className="text-lg">
                  {d.key === 'Monday' ? '🌅' : 
                   d.key === 'Tuesday' ? '☀️' : 
                   d.key === 'Wednesday' ? '🌤️' : 
                   d.key === 'Thursday' ? '🌇' : 
                   d.key === 'Friday' ? '🌙' : '🍽️'}
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
                <div className="w-16 h-16 border-4 border-green-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                <p className="text-gray-600 font-medium">식단을 불러오는 중...</p>
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
          ) : diets[selectedDay] && Object.keys(diets[selectedDay]).length === 0 ? (
            <div className="col-span-2 flex items-center justify-center py-16">
              <div className="text-center">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <span className="text-2xl">🍽️</span>
                </div>
                <p className="text-gray-500 font-medium">추천 식단이 없습니다</p>
              </div>
            </div>
          ) : (
            Object.entries(diets[selectedDay] || {}).map(([meal, diet], idx) => {
              // 끼니 이름 매핑
              const mealLabels = {
                breakfast: "아침",
                lunch: "점심", 
                dinner: "저녁",
                snack: "간식"
              };
              const mealLabel = mealLabels[meal] || meal;
              
              return (
                <div key={meal} className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden hover:shadow-xl transition-all transform hover:scale-[1.02]">
                  <div className="relative">
                    <DietIllustration
                      meal={meal}
                      category={diet.dietCategory}
                      index={idx}
                      label={diet.dietCategory || mealLabel}
                    />
                    <div className="absolute top-3 right-3 bg-white bg-opacity-90 rounded-full px-3 py-1">
                      <span className="text-xs font-medium text-gray-700">
                        {meal === 'breakfast' ? '🌅' : 
                         meal === 'lunch' ? '☀️' : 
                         meal === 'dinner' ? '🌙' : '🍪'}
                      </span>
                    </div>
                  </div>
                  
                  <div className="p-6">
                    <div className="flex items-center gap-2 mb-3">
                      <div className="w-8 h-8 bg-gradient-to-r from-green-400 to-teal-500 rounded-full flex items-center justify-center">
                        <span className="text-white text-sm">🍽️</span>
                      </div>
                      <h3 className="text-lg font-semibold text-gray-800">{mealLabel} - {diet.name}</h3>
                    </div>
                    
                    {diet.description && (
                      <p className="text-gray-600 text-sm mb-4 leading-relaxed">
                        {(diet.description || '').length > 90 ? `${diet.description.slice(0, 90)}…` : diet.description}
                      </p>
                    )}
                    
                    {diet.nutrients && (
                      <div className="flex flex-wrap gap-2 mb-4">
                        {typeof diet.nutrients.calories !== 'undefined' && (
                          <span className="inline-flex items-center gap-1 bg-red-100 text-red-800 px-3 py-1 rounded-full text-xs font-medium">
                            <span>🔥</span>
                            {diet.nutrients.calories}kcal
                          </span>
                        )}
                        {typeof diet.nutrients.protein !== 'undefined' && (
                          <span className="inline-flex items-center gap-1 bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-xs font-medium">
                            <span>💪</span>
                            단백질 {diet.nutrients.protein}g
                          </span>
                        )}
                        {typeof diet.nutrients.carbs !== 'undefined' && (
                          <span className="inline-flex items-center gap-1 bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-xs font-medium">
                            <span>🍞</span>
                            탄수 {diet.nutrients.carbs}g
                          </span>
                        )}
                        {typeof diet.nutrients.fat !== 'undefined' && (
                          <span className="inline-flex items-center gap-1 bg-purple-100 text-purple-800 px-3 py-1 rounded-full text-xs font-medium">
                            <span>🥑</span>
                            지방 {diet.nutrients.fat}g
                          </span>
                        )}
                      </div>
                    )}
                    
                    {/* 식단 종류 표시 */}
                    {diet.dietCategory && (() => {
                      const dietInfo = getDietInfo(diet.dietCategory);
                      return (
                        <div className="mb-4">
                          <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium ${dietInfo.color}`}>
                            <span>{dietInfo.emoji}</span>
                            {dietInfo.displayName}
                          </span>
                        </div>
                      );
                    })()}
                    
                    {diet.reason && (
                      <div className="mb-4 p-3 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl">
                        <p className="text-gray-600 text-xs leading-relaxed whitespace-pre-line">
                          <span className="text-blue-600 font-medium">💡 추천 이유:</span>{' '}
                          {diet.reason.length > 80 ? (
                            diet.reason
                              .slice(0, 80)
                              .split(/\.\s+/)
                              .filter(s => s.trim().length > 0)
                              .map((sentence, idx, arr) => (
                                <span key={idx}>
                                  {sentence.trim()}
                                  {idx < arr.length - 1 ? '.' : ''}
                                  {idx < arr.length - 1 && <br />}
                                </span>
                              ))
                          ) : (
                            diet.reason
                              .split(/\.\s+/)
                              .filter(s => s.trim().length > 0)
                              .map((sentence, idx, arr) => (
                                <span key={idx}>
                                  {sentence.trim()}
                                  {idx < arr.length - 1 ? '.' : diet.reason.trim().endsWith('.') ? '' : '.'}
                                  {idx < arr.length - 1 && <br />}
                                </span>
                              ))
                          )}
                          {diet.reason.length > 80 && '...'}
                        </p>
                      </div>
                    )}
                    
                    {diet.ingredients && (
                      <div className="mb-4 p-3 bg-gradient-to-r from-green-50 to-emerald-50 rounded-xl">
                        <p className="text-gray-600 text-xs leading-relaxed">
                          <span className="text-green-600 font-medium">🥘 재료:</span> {Array.isArray(diet.ingredients) ? diet.ingredients.join(', ') : diet.ingredients}
                        </p>
                      </div>
                    )}
                    
                    <div className="flex gap-2">
                      <Link 
                        to={`/diet-detail/${meal}`} 
                        state={{ diet, mealIndex: idx + 1, mealKey: meal }} 
                        className="flex-1 flex items-center justify-center gap-2 bg-gradient-to-r from-green-500 to-teal-600 text-white py-3 rounded-xl text-sm font-medium hover:from-green-600 hover:to-teal-700 transition-all transform hover:scale-[1.02] shadow-lg"
                      >
                        <span className="text-lg">👁️</span>
                        상세보기
                      </Link>
                      {diet.infoUrl && (
                        <a 
                          href={diet.infoUrl} 
                          target="_blank" 
                          rel="noopener noreferrer" 
                          className="flex items-center justify-center gap-2 bg-white text-gray-700 py-3 px-4 rounded-xl text-sm font-medium hover:bg-gray-50 border border-gray-300 transition-all transform hover:scale-[1.02]"
                        >
                          <span className="text-lg">🔗</span>
                          정보
                        </a>
                      )}
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>
        </div>
      </div>
    </Layout>
  );
}