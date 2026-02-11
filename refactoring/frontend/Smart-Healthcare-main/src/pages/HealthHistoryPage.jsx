import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import Button from '../components/Button';
import { Calendar, TrendingUp, Activity, UtensilsCrossed, BarChart3, Filter, Download, ArrowRight } from 'lucide-react';
import HeroWithBg from '../components/HeroWithBg';
import SectionWithWave from '../components/SectionWithWave';
import { getInbodyRecords } from '../api/inbody';
import { getBodyAnalysisHistory } from '../api/bodyAnalysis';
import { getCurrentUserId, useUser } from '../api/auth';
import { apiCall } from '../api/config';
import { getBodyTypeInfo } from '../utils/bodyTypeUtils';

export default function HealthHistoryPage() {
  const { user: currentUser, isLoggedIn } = useUser();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [user, setUser] = useState(null);
  const [activeTab, setActiveTab] = useState('inbody');
  const [dateRange, setDateRange] = useState({
    startDate: '',
    endDate: ''
  });
  const [viewMode, setViewMode] = useState('all'); // 'all', 'period', 'latest'
  
  // 데이터 상태
  const [inbodyHistory, setInbodyHistory] = useState([]);
  const [bodyAnalysisHistory, setBodyAnalysisHistory] = useState([]);
  const [workoutHistory, setWorkoutHistory] = useState([]);
  const [dietHistory, setDietHistory] = useState([]);
  
  const navigate = useNavigate();

  useEffect(() => {
    // 로그인 체크
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }

    if (currentUser) {
      setUser(currentUser);
    }
    
    // 초기 데이터 로드
    loadAllHistory();
  }, [navigate, isLoggedIn, currentUser]);

  const loadAllHistory = async () => {
    try {
      setLoading(true);
      setError('');
      
      const userId = getCurrentUserId();
      if (!userId) {
        setError('로그인이 필요합니다.');
        return;
      }

      // 병렬로 모든 히스토리 데이터 로드
      await Promise.all([
        loadInbodyHistory(userId),
        loadBodyAnalysisHistory(userId),
        loadWorkoutHistory(userId),
        loadDietHistory(userId)
      ]);
      
    } catch (err) {
      console.error('❌ 히스토리 데이터 로드 실패:', err);
      setError('히스토리 데이터를 불러오는 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const loadInbodyHistory = async (userId) => {
    try {
      const options = {};
      
      if (viewMode === 'period' && dateRange.startDate && dateRange.endDate) {
        options.startDate = new Date(dateRange.startDate).toISOString();
        options.endDate = new Date(dateRange.endDate).toISOString();
      } else if (viewMode === 'latest') {
        options.size = 10;
      }
      
      const response = await getInbodyRecords(userId, options);
      
      // 페이징된 응답인 경우 content 배열 추출
      if (response && response.data && response.data.content) {
        setInbodyHistory(response.data.content);
      } else if (Array.isArray(response.data)) {
        setInbodyHistory(response.data);
      } else if (Array.isArray(response)) {
        setInbodyHistory(response);
      } else {
        setInbodyHistory([]);
      }
    } catch (err) {
      console.error('❌ 인바디 히스토리 로드 실패:', err);
      setInbodyHistory([]);
    }
  };

  const loadBodyAnalysisHistory = async (userId) => {
    try {
      const options = {};
      
      if (viewMode === 'period' && dateRange.startDate && dateRange.endDate) {
        options.startDate = new Date(dateRange.startDate).toISOString();
        options.endDate = new Date(dateRange.endDate).toISOString();
      } else if (viewMode === 'latest') {
        options.limit = 10;
      }
      
      const response = await getBodyAnalysisHistory(userId, options);
      
      // 페이징된 응답인 경우 content 배열 추출
      if (response && response.content) {
        setBodyAnalysisHistory(response.content);
      } else if (Array.isArray(response)) {
        setBodyAnalysisHistory(response);
      } else {
        setBodyAnalysisHistory([]);
      }
    } catch (err) {
      console.error('❌ 체형분석 히스토리 로드 실패:', err);
      setBodyAnalysisHistory([]);
    }
  };

  const loadWorkoutHistory = async (userId) => {
    try {
      const params = new URLSearchParams({
        page: '0',
        size: viewMode === 'latest' ? '10' : '100'
      });
      
      // 운동 추천 히스토리 API 호출
      const response = await apiCall(`/api/workout-recommendation/${userId}?${params.toString()}`);
      
      console.log('🔍 운동 추천 히스토리 응답:', response);
      
      if (response && response.success && response.data) {
        // 페이징된 응답에서 content 추출
        const workoutData = response.data.content || [];
        console.log('✅ 운동 추천 히스토리 데이터:', workoutData);
        setWorkoutHistory(workoutData);
      } else {
        console.log('📝 운동 추천 히스토리가 없음');
        setWorkoutHistory([]);
      }
    } catch (err) {
      console.error('❌ 운동 히스토리 로드 실패:', err);
      setWorkoutHistory([]);
    }
  };

  const loadDietHistory = async (userId) => {
    try {
      // 식단 추천 히스토리 API 호출
      const response = await apiCall(`/api/diet-recommendation/${userId}/history`);
      
      console.log('🔍 식단 추천 히스토리 응답:', response);
      
      if (response && response.success && response.data) {
        // List 형태의 응답
        const dietData = Array.isArray(response.data) ? response.data : [];
        console.log('✅ 식단 추천 히스토리 데이터:', dietData);
        
        // 최신 N개만 필터링
        const filteredData = viewMode === 'latest' ? dietData.slice(0, 10) : dietData;
        setDietHistory(filteredData);
      } else {
        console.log('📝 식단 추천 히스토리가 없음');
        setDietHistory([]);
      }
    } catch (err) {
      console.error('❌ 식단 히스토리 로드 실패:', err);
      setDietHistory([]);
    }
  };

  const handleDateRangeChange = () => {
    if (viewMode === 'period') {
      loadAllHistory();
    }
  };

  const handleViewModeChange = (mode) => {
    setViewMode(mode);
    if (mode === 'all' || mode === 'latest') {
      loadAllHistory();
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getBMIStatus = (bmi) => {
    if (bmi < 18.5) return { status: '저체중', color: 'text-blue-600', bgColor: 'bg-blue-50' };
    if (bmi < 23) return { status: '정상', color: 'text-green-600', bgColor: 'bg-green-50' };
    if (bmi < 25) return { status: '과체중', color: 'text-yellow-600', bgColor: 'bg-yellow-50' };
    return { status: '비만', color: 'text-red-600', bgColor: 'bg-red-50' };
  };

  const getBodyFatStatus = (bodyFat) => {
    if (bodyFat < 10) return { status: '매우 낮음', color: 'text-blue-600', bgColor: 'bg-blue-50' };
    if (bodyFat < 15) return { status: '낮음', color: 'text-green-600', bgColor: 'bg-green-50' };
    if (bodyFat < 20) return { status: '정상', color: 'text-green-600', bgColor: 'bg-green-50' };
    if (bodyFat < 25) return { status: '높음', color: 'text-yellow-600', bgColor: 'bg-yellow-50' };
    return { status: '매우 높음', color: 'text-red-600', bgColor: 'bg-red-50' };
  };

  const tabs = [
    { id: 'inbody', label: '인바디 기록', icon: BarChart3, count: Array.isArray(inbodyHistory) ? inbodyHistory.length : 0 },
    { id: 'bodyAnalysis', label: '체형분석', icon: TrendingUp, count: Array.isArray(bodyAnalysisHistory) ? bodyAnalysisHistory.length : 0 },
    { id: 'workout', label: '운동 추천', icon: Activity, count: Array.isArray(workoutHistory) ? workoutHistory.length : 0 },
    { id: 'diet', label: '식단 추천', icon: UtensilsCrossed, count: Array.isArray(dietHistory) ? dietHistory.length : 0 }
  ];

  if (loading) {
    return (
      <Layout>
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-gray-600">히스토리 데이터를 불러오는 중...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 py-16">
        <div className="w-full max-w-6xl mx-auto px-6">
          {/* 헤더 */}
          <div className="mb-12 text-center">
            <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full mb-6 shadow-lg">
              <span className="text-3xl">📊</span>
            </div>
            <h1 className="text-4xl font-bold text-gray-800 mb-4">건강 히스토리</h1>
            <p className="text-gray-600 text-lg max-w-2xl mx-auto">
              {user?.email?.split('@')[0] || '사용자'}님의 건강 관리 기록을 확인하세요
            </p>
          </div>

          <div className="container mx-auto">
          {/* 필터 및 뷰 모드 */}
          <div className="bg-white rounded-xl shadow-lg p-6 mb-8">
            <div className="flex flex-col lg:flex-row gap-4 items-center justify-between">
              <div className="flex items-center gap-4">
                <Filter className="w-5 h-5 text-gray-500" />
                <span className="font-medium text-gray-700">조회 옵션:</span>
                
                <div className="flex gap-2">
                  {[
                    { mode: 'all', label: '전체' },
                    { mode: 'latest', label: '최신 10개' },
                    { mode: 'period', label: '기간 설정' }
                  ].map(({ mode, label }) => (
                    <button
                      key={mode}
                      onClick={() => handleViewModeChange(mode)}
                      className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                        viewMode === mode
                          ? 'bg-primary text-white'
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                    >
                      {label}
                    </button>
                  ))}
                </div>
              </div>

              {viewMode === 'period' && (
                <div className="flex items-center gap-4">
                  <div className="flex items-center gap-2">
                    <Calendar className="w-4 h-4 text-gray-500" />
                    <input
                      type="date"
                      value={dateRange.startDate}
                      onChange={(e) => setDateRange(prev => ({ ...prev, startDate: e.target.value }))}
                      className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    />
                    <span className="text-gray-500">~</span>
                    <input
                      type="date"
                      value={dateRange.endDate}
                      onChange={(e) => setDateRange(prev => ({ ...prev, endDate: e.target.value }))}
                      className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    />
                    <Button
                      onClick={handleDateRangeChange}
                      className="bg-primary text-white px-4 py-2 rounded-lg text-sm"
                    >
                      조회
                    </Button>
                  </div>
                </div>
              )}

              <Button
                onClick={() => window.print()}
                className="bg-gray-600 text-white px-4 py-2 rounded-lg text-sm flex items-center gap-2"
              >
                <Download className="w-4 h-4" />
                인쇄
              </Button>
            </div>
          </div>

          {/* 탭 네비게이션 */}
          <div className="bg-white rounded-xl shadow-lg mb-8">
            <div className="flex border-b border-gray-200">
              {tabs.map(({ id, label, icon: Icon, count }) => (
                <button
                  key={id}
                  onClick={() => setActiveTab(id)}
                  className={`flex-1 flex items-center justify-center gap-2 px-6 py-4 text-sm font-medium transition-colors ${
                    activeTab === id
                      ? 'text-primary border-b-2 border-primary bg-primary/5'
                      : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {label}
                  <span className="bg-gray-200 text-gray-600 px-2 py-1 rounded-full text-xs">
                    {count}
                  </span>
                </button>
              ))}
            </div>

            {/* 탭 컨텐츠 */}
            <div className="p-6">
              {activeTab === 'inbody' && (
                <div>
                  <h3 className="text-lg font-semibold mb-4">인바디 기록 히스토리</h3>
                  {!Array.isArray(inbodyHistory) || inbodyHistory.length === 0 ? (
                    <div className="text-center py-12">
                      <BarChart3 className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                      <p className="text-gray-500">인바디 기록이 없습니다.</p>
                      <Button
                        onClick={() => navigate('/inbody-input')}
                        className="mt-4 bg-primary text-white px-6 py-2 rounded-lg"
                      >
                        인바디 측정하기
                      </Button>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                      {Array.isArray(inbodyHistory) && inbodyHistory.map((record) => {
                        const bmiStatus = getBMIStatus(record.bmi);
                        const bodyFatStatus = getBodyFatStatus(record.bodyFatPercentage);
                        
                        return (
                          <div
                            key={record.id}
                            className="group bg-gray-50 rounded-xl p-6 border border-gray-200 hover:shadow-lg hover:border-blue-300 hover:bg-blue-50 transition-all duration-200 cursor-pointer transform hover:scale-[1.02]"
                            onClick={() => navigate('/inbody-history')}
                          >
                            <div className="flex justify-between items-start mb-4">
                              <div>
                                <h4 className="font-semibold text-gray-800">
                                  {formatDate(record.createdAt)}
                                </h4>
                                <p className="text-sm text-gray-500">ID: {record.id}</p>
                              </div>
                              <ArrowRight className="w-5 h-5 text-gray-400 group-hover:text-blue-500 transition-colors" />
                            </div>

                            <div className="space-y-3">
                              <div className="flex justify-between items-center">
                                <span className="text-sm text-gray-600">체중</span>
                                <span className="font-medium">{record.weight}kg</span>
                              </div>
                              <div className="flex justify-between items-center">
                                <span className="text-sm text-gray-600">BMI</span>
                                <span className={`font-medium ${bmiStatus.color}`}>
                                  {record.bmi} ({bmiStatus.status})
                                </span>
                              </div>
                              <div className="flex justify-between items-center">
                                <span className="text-sm text-gray-600">체지방률</span>
                                <span className={`font-medium ${bodyFatStatus.color}`}>
                                  {record.bodyFatPercentage}% ({bodyFatStatus.status})
                                </span>
                              </div>
                              <div className="flex justify-between items-center">
                                <span className="text-sm text-gray-600">근육량</span>
                                <span className="font-medium">{record.muscleMass}kg</span>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'bodyAnalysis' && (
                <div>
                  <h3 className="text-lg font-semibold mb-4">체형분석 히스토리</h3>
                  {!Array.isArray(bodyAnalysisHistory) || bodyAnalysisHistory.length === 0 ? (
                    <div className="text-center py-12">
                      <TrendingUp className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                      <p className="text-gray-500">체형분석 기록이 없습니다.</p>
                      <Button
                        onClick={() => navigate('/body-analysis')}
                        className="mt-4 bg-primary text-white px-6 py-2 rounded-lg"
                      >
                        체형분석 하기
                      </Button>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                      {Array.isArray(bodyAnalysisHistory) && bodyAnalysisHistory.map((analysis, index) => {
                        // 백엔드에서 받은 데이터의 필드명 처리
                        const analysisDate = analysis.analyzedAt || analysis.createdAt || analysis.timestamp;
                        const analysisId = analysis.id || index;
                        const bodyTypeLabel = analysis.label || analysis.bodyType || '체형 분석';
                        const analysisMethod = analysis.analysisMethod || analysis.method || 'OpenAI ChatGPT';
                        
                        console.log('🔍 체형분석 데이터:', analysis);
                        
                        return (
                          <div
                            key={analysisId}
                            className="group bg-gradient-to-br from-purple-50 to-blue-50 rounded-xl p-6 border-2 border-purple-200 hover:shadow-lg hover:border-purple-400 hover:from-purple-100 hover:to-blue-100 transition-all duration-200 cursor-pointer transform hover:scale-[1.02]"
                            onClick={() => navigate('/body-analysis', { 
                              state: { 
                                fromHistory: true, 
                                analysisData: analysis 
                              } 
                            })}
                          >
                            <div className="flex justify-between items-start mb-4">
                              <div>
                                <h4 className="font-semibold text-gray-800">
                                  {analysisDate ? formatDate(analysisDate) : '날짜 정보 없음'}
                                </h4>
                                <p className="text-sm text-gray-500">분석 #{analysisId}</p>
                              </div>
                              <ArrowRight className="w-5 h-5 text-gray-400 group-hover:text-purple-500 transition-colors" />
                            </div>

                            <div className="space-y-3">
                              <div className="flex justify-between items-center">
                                <span className="text-sm text-gray-600">체형 분류</span>
                                <span className={`font-bold ${getBodyTypeInfo(bodyTypeLabel).color}`}>
                                  {getBodyTypeInfo(bodyTypeLabel).emoji} {getBodyTypeInfo(bodyTypeLabel).displayName}
                                </span>
                              </div>
                              <div className="flex justify-between items-center">
                                <span className="text-sm text-gray-600">분석 방법</span>
                                <span className="font-medium text-blue-600">{analysisMethod}</span>
                              </div>
                              {analysis.summary && (
                                <div className="mt-4 p-3 bg-white rounded-lg border border-purple-100">
                                  <p className="text-sm text-gray-700 line-clamp-3">{analysis.summary}</p>
                                </div>
                              )}
                              {(analysis.accuracy || analysis.healthRisk) && (
                                <div className="flex flex-wrap gap-2 mt-3">
                                  {analysis.accuracy && (
                                    <span className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full font-medium">
                                      정확도: {analysis.accuracy}
                                    </span>
                                  )}
                                  {analysis.healthRisk && (
                                    <span className={`text-xs px-2 py-1 rounded-full font-medium ${
                                      analysis.healthRisk === '낮음' ? 'bg-green-100 text-green-700' :
                                      analysis.healthRisk === '보통' ? 'bg-yellow-100 text-yellow-700' :
                                      'bg-red-100 text-red-700'
                                    }`}>
                                      위험도: {analysis.healthRisk}
                                    </span>
                                  )}
                                </div>
                              )}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'workout' && (
                <div>
                  <h3 className="text-lg font-semibold mb-4">운동 추천 히스토리</h3>
                  {!Array.isArray(workoutHistory) || workoutHistory.length === 0 ? (
                    <div className="text-center py-12">
                      <Activity className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                      <p className="text-gray-500">운동 추천 기록이 없습니다.</p>
                      <Button
                        onClick={() => navigate('/survey')}
                        className="mt-4 bg-primary text-white px-6 py-2 rounded-lg"
                      >
                        운동 추천 받기
                      </Button>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      {Array.isArray(workoutHistory) && workoutHistory.map((workout, index) => (
                        <div
                          key={workout.id || index}
                          className="group bg-gradient-to-br from-blue-50 to-purple-50 rounded-xl p-6 border-2 border-blue-200 hover:shadow-lg hover:border-blue-400 hover:from-blue-100 hover:to-purple-100 transition-all duration-200 cursor-pointer transform hover:scale-[1.02]"
                          onClick={() => navigate('/recommended-workout-list', { 
                            state: { 
                              fromHistory: true, 
                              workoutData: workout 
                            } 
                          })}
                        >
                          <div className="flex justify-between items-start mb-4">
                            <div>
                              <h4 className="font-semibold text-gray-800">
                                {workout.createdAt ? formatDate(workout.createdAt) : '날짜 정보 없음'}
                              </h4>
                              <p className="text-sm text-gray-500">추천 #{workout.id || index + 1}</p>
                            </div>
                            <ArrowRight className="w-5 h-5 text-gray-400 group-hover:text-blue-500 transition-colors" />
                          </div>
                          
                          <div className="space-y-3">
                            <div>
                              <span className="text-sm text-blue-600 font-semibold">프로그램명</span>
                              <h3 className="font-bold text-gray-900 text-lg">{workout.programName || '운동 프로그램'}</h3>
                            </div>
                            {workout.weeklySchedule && (
                              <div>
                                <span className="text-sm text-gray-600">주간 일정</span>
                                <p className="text-sm text-gray-700">{workout.weeklySchedule}</p>
                              </div>
                            )}
                            {workout.targetMuscles && (
                              <div>
                                <span className="text-sm text-gray-600">타겟 근육</span>
                                <p className="text-sm text-gray-700">{workout.targetMuscles}</p>
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'diet' && (
                <div>
                  <h3 className="text-lg font-semibold mb-4">식단 추천 히스토리</h3>
                  {!Array.isArray(dietHistory) || dietHistory.length === 0 ? (
                    <div className="text-center py-12">
                      <UtensilsCrossed className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                      <p className="text-gray-500">식단 추천 기록이 없습니다.</p>
                      <Button
                        onClick={() => navigate('/survey')}
                        className="mt-4 bg-primary text-white px-6 py-2 rounded-lg"
                      >
                        식단 추천 받기
                      </Button>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      {Array.isArray(dietHistory) && dietHistory.map((diet, index) => (
                        <div
                          key={diet.id || index}
                          className="group bg-gradient-to-br from-green-50 to-yellow-50 rounded-xl p-6 border-2 border-green-200 hover:shadow-lg hover:border-green-400 hover:from-green-100 hover:to-yellow-100 transition-all duration-200 cursor-pointer transform hover:scale-[1.02]"
                          onClick={() => navigate('/recommended-diet-list', { 
                            state: { 
                              fromHistory: true, 
                              dietData: diet 
                            } 
                          })}
                        >
                          <div className="flex justify-between items-start mb-4">
                            <div>
                              <h4 className="font-semibold text-gray-800">
                                {diet.createdAt ? formatDate(diet.createdAt) : '날짜 정보 없음'}
                              </h4>
                              <p className="text-sm text-gray-500">추천 #{diet.id || index + 1}</p>
                            </div>
                            <ArrowRight className="w-5 h-5 text-gray-400 group-hover:text-green-500 transition-colors" />
                          </div>
                          
                          <div className="space-y-3">
                            <div>
                              <span className="text-sm text-green-600 font-semibold">식단 스타일</span>
                              <h3 className="font-bold text-gray-900 text-lg">{diet.mealStyle || '식단 계획'}</h3>
                            </div>
                            {diet.goals && (
                              <div>
                                <span className="text-sm text-gray-600">목표</span>
                                <p className="text-sm text-gray-700">{diet.goals}</p>
                              </div>
                            )}
                            {diet.calories && (
                              <div>
                                <span className="text-sm text-gray-600">일일 칼로리</span>
                                <p className="text-sm text-gray-700 font-medium">{diet.calories} kcal</p>
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          {error && (
            <div className="bg-gradient-to-r from-red-50 to-pink-50 border border-red-200 rounded-2xl p-6 text-center">
              <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-3">
                <span className="text-2xl">⚠️</span>
              </div>
              <p className="text-red-600 font-medium">{error}</p>
            </div>
          )}
        </div>
        </div>
      </div>
    </Layout>
  );
}
