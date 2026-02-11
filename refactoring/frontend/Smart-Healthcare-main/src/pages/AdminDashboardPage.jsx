import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AdminLayout from '../components/AdminLayout';
import Button from '../components/Button';
import AuthManager from '../utils/authManager';
import { 
  Shield, 
  Users, 
  Activity, 
  BarChart3, 
  Settings, 
  AlertTriangle,
  TrendingUp,
  Database,
  Heart,
  Calendar,
  LogOut,
  RefreshCw
} from 'lucide-react';

export default function AdminDashboardPage() {
  const navigate = useNavigate();
  const [admin, setAdmin] = useState(null);
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    // 관리자 권한 체크
    const adminToken = sessionStorage.getItem('adminToken');
    const currentAdmin = sessionStorage.getItem('currentAdmin');
    
    console.log('🔐 AdminDashboardPage 권한 체크:');
    console.log('  - adminToken:', adminToken ? '존재함' : '없음');
    console.log('  - currentAdmin:', currentAdmin);
    
    if (!adminToken || !currentAdmin) {
      console.log('❌ 관리자 토큰 또는 정보가 없음 - 로그인 페이지로 이동');
      navigate('/admin/login');
      return;
    }

    try {
      const adminData = JSON.parse(currentAdmin);
      console.log('🔐 파싱된 관리자 데이터:', adminData);
      console.log('🔐 role 체크:', adminData.role, '===', 'ADMIN', '?', adminData.role === 'ADMIN');
      
      if (adminData.role !== 'ADMIN') {
        console.log('❌ 관리자 권한이 아님 - 로그인 페이지로 이동');
        alert('관리자 권한이 필요합니다.');
        navigate('/admin/login');
        return;
      }
      setAdmin(adminData);
      console.log('✅ 관리자 권한 확인 완료');
    } catch (error) {
      console.error('❌ 관리자 정보 파싱 오류:', error);
      navigate('/admin/login');
      return;
    }

    loadDashboardData();
  }, [navigate]);

  const loadDashboardData = async () => {
    try {
      const adminToken = sessionStorage.getItem('adminToken');
      const response = await fetch('/api/admin/dashboard', {
        headers: {
          'Authorization': `Bearer ${adminToken}`,
          'Content-Type': 'application/json'
        }
      });

      const data = await response.json();
      
      if (data.success) {
        setDashboardData(data.data);
      } else {
        console.error('대시보드 데이터 로딩 실패:', data.error);
      }
    } catch (error) {
      console.error('대시보드 데이터 로딩 오류:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleRefresh = () => {
    setRefreshing(true);
    loadDashboardData();
  };

  const handleLogout = () => {
    if (window.confirm('관리자 로그아웃하시겠습니까?')) {
      AuthManager.logoutAdmin();
      navigate('/admin/login');
    }
  };

  if (loading) {
    return (
      <AdminLayout>
        <div className="text-center py-16">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-red-500 mx-auto mb-4"></div>
          <p className="text-xl text-gray-600">관리자 대시보드를 불러오는 중...</p>
        </div>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      <div className="max-w-7xl mx-auto p-6">
        {/* 대시보드 환영 메시지 */}
        <div className="bg-gradient-to-r from-red-600 to-red-800 rounded-3xl text-white p-8 mb-8">
        <div className="flex items-center justify-between">
          <div>
            <div className="flex items-center gap-3 mb-2">
              <Shield className="w-8 h-8" />
              <h1 className="text-3xl font-bold">관리자 대시보드</h1>
            </div>
            <p className="text-red-100 text-lg">
              안녕하세요, {admin?.email?.split('@')[0] || '관리자'}님!
            </p>
            <p className="text-sm text-red-200 mt-1">
              로그인 시간: {admin?.loginTime ? new Date(admin.loginTime).toLocaleString() : ''}
            </p>
            {dashboardData?.lastUpdated && (
              <p className="text-sm text-red-200">
                마지막 업데이트: {dashboardData.lastUpdated}
              </p>
            )}
          </div>
          <div className="flex items-center gap-4">
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

      {/* 통계 카드 */}
      {dashboardData && (
        <>
          {/* 사용자 통계 */}
          <section className="mb-8">
            <h2 className="text-2xl font-bold text-white mb-6 flex items-center gap-2">
              <Users className="w-6 h-6" />
              사용자 통계
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="bg-white rounded-2xl p-6 shadow-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-gray-600 text-sm">전체 사용자</p>
                    <p className="text-3xl font-bold text-gray-800">
                      {dashboardData.userStats.total.toLocaleString()}
                    </p>
                  </div>
                  <Users className="w-12 h-12 text-blue-500" />
                </div>
              </div>
              
              <div className="bg-white rounded-2xl p-6 shadow-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-gray-600 text-sm">활성 사용자</p>
                    <p className="text-3xl font-bold text-green-600">
                      {dashboardData.userStats.active.toLocaleString()}
                    </p>
                  </div>
                  <Activity className="w-12 h-12 text-green-500" />
                </div>
              </div>

              <div className="bg-white rounded-2xl p-6 shadow-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-gray-600 text-sm">관리자</p>
                    <p className="text-3xl font-bold text-red-600">
                      {dashboardData.userStats.admin.toLocaleString()}
                    </p>
                  </div>
                  <Shield className="w-12 h-12 text-red-500" />
                </div>
              </div>

              <div className="bg-white rounded-2xl p-6 shadow-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-gray-600 text-sm">이번 달 신규</p>
                    <p className="text-3xl font-bold text-purple-600">
                      {dashboardData.userStats.newThisMonth.toLocaleString()}
                    </p>
                  </div>
                  <TrendingUp className="w-12 h-12 text-purple-500" />
                </div>
              </div>
            </div>
          </section>

          {/* 데이터 통계 */}
          <section className="mb-8">
            <h2 className="text-2xl font-bold text-white mb-6 flex items-center gap-2">
              <Database className="w-6 h-6" />
              데이터 통계
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
              <div className="bg-white rounded-2xl p-6 shadow-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-gray-600 text-sm">인바디 기록</p>
                    <p className="text-2xl font-bold text-blue-600">
                      {dashboardData.dataStats.inbodyRecords.toLocaleString()}
                    </p>
                  </div>
                  <BarChart3 className="w-10 h-10 text-blue-500" />
                </div>
              </div>

              <div className="bg-white rounded-2xl p-6 shadow-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-gray-600 text-sm">체형 분석</p>
                    <p className="text-2xl font-bold text-green-600">
                      {dashboardData.dataStats.bodyAnalyses.toLocaleString()}
                    </p>
                  </div>
                  <Heart className="w-10 h-10 text-green-500" />
                </div>
              </div>

              <div className="bg-white rounded-2xl p-6 shadow-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-gray-600 text-sm">운동 추천</p>
                    <p className="text-2xl font-bold text-orange-600">
                      {dashboardData.dataStats.workoutRecommendations.toLocaleString()}
                    </p>
                  </div>
                  <Activity className="w-10 h-10 text-orange-500" />
                </div>
              </div>

              <div className="bg-white rounded-2xl p-6 shadow-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-gray-600 text-sm">식단 추천</p>
                    <p className="text-2xl font-bold text-purple-600">
                      {dashboardData.dataStats.dietRecommendations.toLocaleString()}
                    </p>
                  </div>
                  <Calendar className="w-10 h-10 text-purple-500" />
                </div>
              </div>
            </div>
          </section>

          {/* 오늘의 활동 */}
          <section className="mb-8">
            <h2 className="text-2xl font-bold text-white mb-6 flex items-center gap-2">
              <Calendar className="w-6 h-6" />
              오늘의 활동
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-2xl p-6 border-2 border-blue-200">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-blue-800 text-sm font-medium">오늘 인바디 기록</p>
                    <p className="text-3xl font-bold text-blue-900">
                      {dashboardData.todayActivity.inbodyRecords.toLocaleString()}
                    </p>
                  </div>
                  <BarChart3 className="w-12 h-12 text-blue-600" />
                </div>
              </div>

              <div className="bg-gradient-to-br from-green-50 to-green-100 rounded-2xl p-6 border-2 border-green-200">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-green-800 text-sm font-medium">오늘 체형 분석</p>
                    <p className="text-3xl font-bold text-green-900">
                      {dashboardData.todayActivity.bodyAnalyses.toLocaleString()}
                    </p>
                  </div>
                  <Heart className="w-12 h-12 text-green-600" />
                </div>
              </div>
            </div>
          </section>
        </>
      )}

      {/* 관리 메뉴 */}
      <section className="mb-8">
        <h2 className="text-2xl font-bold text-white mb-6 flex items-center gap-2">
          <Settings className="w-6 h-6" />
          관리 메뉴
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <button
            onClick={() => navigate('/admin/users')}
            className="bg-white rounded-2xl p-6 shadow-lg hover:shadow-xl transition-shadow text-left"
          >
            <div className="flex items-center gap-4">
              <div className="bg-blue-500 p-3 rounded-lg">
                <Users className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-gray-800">사용자 관리</h3>
                <p className="text-gray-600 text-sm">사용자 계정 및 권한 관리</p>
              </div>
            </div>
          </button>

          <button
            onClick={() => navigate('/admin/stats')}
            className="bg-white rounded-2xl p-6 shadow-lg hover:shadow-xl transition-shadow text-left"
          >
            <div className="flex items-center gap-4">
              <div className="bg-green-500 p-3 rounded-lg">
                <BarChart3 className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-gray-800">상세 통계</h3>
                <p className="text-gray-600 text-sm">시스템 전체 데이터 분석</p>
              </div>
            </div>
          </button>

          <button
            onClick={() => navigate('/admin/system')}
            className="bg-white rounded-2xl p-6 shadow-lg hover:shadow-xl transition-shadow text-left"
          >
            <div className="flex items-center gap-4">
              <div className="bg-red-500 p-3 rounded-lg">
                <Settings className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-gray-800">시스템 관리</h3>
                <p className="text-gray-600 text-sm">백업, 로그, 시스템 상태</p>
              </div>
            </div>
          </button>
        </div>
      </section>

      {/* 하단 안내 */}
      <section className="bg-gradient-to-r from-gray-100 to-gray-200 rounded-2xl p-8 text-center">
        <h3 className="text-2xl font-bold text-gray-800 mb-4">⚠️ 관리자 유의사항</h3>
        <div className="text-gray-600 space-y-2">
          <p>• 사용자 데이터 처리 시 개인정보보호법을 준수해주세요</p>
          <p>• 시스템 변경 전 반드시 백업을 실행해주세요</p>
          <p>• 관리자 권한은 업무 목적으로만 사용해주세요</p>
        </div>
      </section>
      </div>
    </AdminLayout>
  );
}
