import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  BarChart3, 
  TrendingUp, 
  Users, 
  Activity, 
  Database,
  Calendar,
  RefreshCw,
  ChevronLeft,
  AlertCircle,
  Target,
  Heart,
  Zap,
  Award
} from 'lucide-react';
import AdminLayout from '../components/AdminLayout';

const AdminStatsPage = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refreshing, setRefreshing] = useState(false);
  const navigate = useNavigate();

  const admin = JSON.parse(sessionStorage.getItem('currentAdmin'));
  const token = sessionStorage.getItem('adminToken');

  useEffect(() => {
    if (!admin || !token || admin.role !== 'ADMIN') {
      navigate('/admin/login');
      return;
    }
    loadStats();
  }, []);

  const loadStats = async () => {
    setLoading(true);
    setError('');
    try {
      // 상세 통계 API 호출 (대시보드 + 시스템 통계)
      const [dashboardResponse, systemResponse] = await Promise.all([
        fetch('/api/admin/dashboard', {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch('/api/admin/statistics', {
          headers: { 'Authorization': `Bearer ${token}` }
        })
      ]);
      
      const dashboardData = await dashboardResponse.json();
      const systemData = await systemResponse.json();
      
      if (dashboardData.success && systemData.success) {
        setStats({
          ...dashboardData.data,
          ...systemData.data,
          lastUpdated: new Date().toLocaleString()
        });
        console.log('✅ 상세 통계 데이터 로드 완료:', {
          dashboard: dashboardData.data,
          system: systemData.data
        });
      } else {
        const errorMsg = dashboardData.error || systemData.error || '통계 데이터를 불러오는데 실패했습니다.';
        setError(errorMsg);
        console.error('❌ 통계 API 오류:', { dashboardData, systemData });
      }
    } catch (err) {
      console.error('❌ 통계 로딩 오류:', err);
      setError('서버와 통신 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleRefresh = () => {
    setRefreshing(true);
    loadStats();
  };

  const formatNumber = (num) => {
    if (num === undefined || num === null) return '0';
    return num.toLocaleString('ko-KR');
  };

  const formatPercentage = (current, total) => {
    if (!total || total === 0) return '0%';
    return `${((current / total) * 100).toFixed(1)}%`;
  };

  if (loading) {
    return (
      <AdminLayout>
        <div className="text-center py-16">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-red-500 mx-auto mb-4"></div>
          <p className="text-xl text-gray-600">통계 데이터를 불러오는 중...</p>
        </div>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      <div className="max-w-7xl mx-auto p-6">
        {/* Header */}
        <div className="bg-gradient-to-r from-red-600 to-red-800 rounded-3xl text-white p-8 mb-8">
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <BarChart3 className="w-8 h-8" />
                <h1 className="text-3xl font-bold">상세 통계</h1>
              </div>
              <p className="text-red-100 text-lg">
                시스템 전체 통계 및 분석
              </p>
              {stats?.lastUpdated && (
                <p className="text-sm text-red-200 mt-1">
                  마지막 업데이트: {stats.lastUpdated}
                </p>
              )}
            </div>
            <div className="flex items-center gap-4">
              <button
                onClick={() => navigate('/admin/dashboard')}
                className="bg-red-500 hover:bg-red-400 p-3 rounded-lg transition-colors"
              >
                <ChevronLeft className="w-6 h-6" />
              </button>
              <button
                onClick={handleRefresh}
                disabled={refreshing}
                className="bg-red-500 hover:bg-red-400 p-3 rounded-lg transition-colors"
              >
                <RefreshCw className={`w-6 h-6 ${refreshing ? 'animate-spin' : ''}`} />
              </button>
            </div>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
            <div className="flex items-center gap-2 text-red-800">
              <AlertCircle className="w-5 h-5" />
              <p>{error}</p>
            </div>
          </div>
        )}

        {stats && (
          <>
            {/* 사용자 통계 */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
                <Users className="w-6 h-6" />
                사용자 현황
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="font-medium text-gray-900">전체 사용자</h3>
                    <Users className="w-5 h-5 text-blue-500" />
                  </div>
                  <p className="text-3xl font-bold text-gray-900">{formatNumber(stats.userStats?.total)}</p>
                  <p className="text-sm text-gray-500 mt-1">등록된 총 사용자 수</p>
                </div>

                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="font-medium text-gray-900">활성 사용자</h3>
                    <Users className="w-5 h-5 text-green-500" />
                  </div>
                  <p className="text-3xl font-bold text-gray-900">{formatNumber(stats.userStats?.active)}</p>
                  <p className="text-sm text-gray-500 mt-1">
                    활성화율: {formatPercentage(stats.userStats?.active, stats.userStats?.total)}
                  </p>
                </div>

                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="font-medium text-gray-900">관리자</h3>
                    <Award className="w-5 h-5 text-yellow-500" />
                  </div>
                  <p className="text-3xl font-bold text-gray-900">{formatNumber(stats.userStats?.admin)}</p>
                  <p className="text-sm text-gray-500 mt-1">시스템 관리자 수</p>
                </div>

                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="font-medium text-gray-900">오늘 신규</h3>
                    <TrendingUp className="w-5 h-5 text-purple-500" />
                  </div>
                  <p className="text-3xl font-bold text-gray-900">{formatNumber(stats.userStats?.newThisMonth)}</p>
                  <p className="text-sm text-gray-500 mt-1">오늘 가입한 사용자</p>
                </div>
              </div>
            </section>

            {/* 데이터 통계 */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
                <Database className="w-6 h-6" />
                데이터 현황
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="font-medium text-gray-900">인바디 기록</h3>
                    <Target className="w-5 h-5 text-blue-500" />
                  </div>
                  <p className="text-3xl font-bold text-gray-900">{formatNumber(stats.dataStats?.inbodyRecords)}</p>
                  <p className="text-sm text-gray-500 mt-1">총 인바디 측정 수</p>
                </div>

                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="font-medium text-gray-900">체형 분석</h3>
                    <Heart className="w-5 h-5 text-red-500" />
                  </div>
                  <p className="text-3xl font-bold text-gray-900">{formatNumber(stats.dataStats?.bodyAnalyses)}</p>
                  <p className="text-sm text-gray-500 mt-1">AI 체형 분석 수</p>
                </div>

                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="font-medium text-gray-900">운동 추천</h3>
                    <Zap className="w-5 h-5 text-orange-500" />
                  </div>
                  <p className="text-3xl font-bold text-gray-900">{formatNumber(stats.dataStats?.workoutRecommendations)}</p>
                  <p className="text-sm text-gray-500 mt-1">맞춤 운동 추천 수</p>
                </div>

                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="font-medium text-gray-900">설문 응답</h3>
                    <Activity className="w-5 h-5 text-green-500" />
                  </div>
                  <p className="text-3xl font-bold text-gray-900">{formatNumber(stats.dataStats?.surveys)}</p>
                  <p className="text-sm text-gray-500 mt-1">완료된 설문 수</p>
                </div>
              </div>
            </section>

            {/* 일일 활동 통계 */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
                <Calendar className="w-6 h-6" />
                오늘의 활동
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="font-medium text-gray-900">오늘 인바디</h3>
                    <Target className="w-5 h-5 text-blue-500" />
                  </div>
                  <p className="text-3xl font-bold text-gray-900">{formatNumber(stats.todayActivity?.inbodyRecords)}</p>
                  <p className="text-sm text-gray-500 mt-1">오늘 등록된 인바디</p>
                </div>

                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="font-medium text-gray-900">오늘 분석</h3>
                    <Heart className="w-5 h-5 text-red-500" />
                  </div>
                  <p className="text-3xl font-bold text-gray-900">{formatNumber(stats.todayActivity?.bodyAnalyses)}</p>
                  <p className="text-sm text-gray-500 mt-1">오늘 체형 분석 수</p>
                </div>
              </div>
            </section>

            {/* 추가 통계 정보 */}
            <section>
              <h2 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
                <TrendingUp className="w-6 h-6" />
                시스템 분석
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <h3 className="font-medium text-gray-900 mb-4">사용자 참여도</h3>
                  <div className="space-y-3">
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">인바디 등록률</span>
                      <span className="font-medium">
                        {formatPercentage(stats.dataStats?.inbodyRecords, stats.userStats?.total)}
                      </span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">체형 분석률</span>
                      <span className="font-medium">
                        {formatPercentage(stats.dataStats?.bodyAnalyses, stats.userStats?.total)}
                      </span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">설문 응답률</span>
                      <span className="font-medium">
                        {formatPercentage(stats.dataStats?.surveys, stats.userStats?.total)}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="bg-white rounded-2xl p-6 shadow-lg">
                  <h3 className="font-medium text-gray-900 mb-4">시스템 활용도</h3>
                  <div className="space-y-3">
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">평균 인바디/사용자</span>
                      <span className="font-medium">
                        {stats.userStats?.total > 0 ? (stats.dataStats?.inbodyRecords / stats.userStats?.total).toFixed(1) : '0.0'}
                      </span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">평균 분석/사용자</span>
                      <span className="font-medium">
                        {stats.userStats?.total > 0 ? (stats.dataStats?.bodyAnalyses / stats.userStats?.total).toFixed(1) : '0.0'}
                      </span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">평균 추천/사용자</span>
                      <span className="font-medium">
                        {stats.userStats?.total > 0 ? (stats.dataStats?.workoutRecommendations / stats.userStats?.total).toFixed(1) : '0.0'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </section>
          </>
        )}

        {/* Empty State */}
        {!stats && !loading && (
          <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
            <BarChart3 className="w-16 h-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">통계 데이터 없음</h3>
            <p className="text-gray-500">시스템 통계를 불러올 수 없습니다.</p>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default AdminStatsPage;
