import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  FileText, 
  Search, 
  Filter, 
  Eye,
  Trash2,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
  AlertCircle,
  CheckCircle,
  XCircle,
  Activity,
  Heart,
  Target,
  Calendar,
  Users,
  BarChart3
} from 'lucide-react';
import AdminLayout from '../components/AdminLayout';

const AdminContentPage = () => {
  const [activeTab, setActiveTab] = useState('inbody');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [dateFilter, setDateFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [refreshing, setRefreshing] = useState(false);
  const navigate = useNavigate();

  const admin = JSON.parse(sessionStorage.getItem('currentAdmin'));
  const token = sessionStorage.getItem('adminToken');

  const tabs = [
    { id: 'inbody', label: '인바디 데이터', icon: Target },
    { id: 'analysis', label: '체형 분석', icon: Heart },
    { id: 'workout', label: '운동 추천', icon: Activity },
    { id: 'diet', label: '식단 추천', icon: Calendar },
    { id: 'survey', label: '설문 응답', icon: FileText }
  ];

  useEffect(() => {
    if (!admin || !token || admin.role !== 'ADMIN') {
      navigate('/admin/login');
      return;
    }
    loadData();
  }, [currentPage, searchTerm, dateFilter, activeTab]);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const params = new URLSearchParams({
        page: currentPage,
        size: 20,
        sort: 'createdAt,desc'
      });
      
      if (searchTerm.trim()) {
        params.append('search', searchTerm.trim());
      }
      if (dateFilter) {
        params.append('dateFilter', dateFilter);
      }

      const endpoints = {
        inbody: '/api/admin/content/inbody',
        analysis: '/api/admin/content/analysis',
        workout: '/api/admin/content/workout-recommendations',
        diet: '/api/admin/content/diet-recommendations',
        survey: '/api/admin/content/surveys'
      };

      const response = await fetch(`${endpoints[activeTab]}?${params}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      const result = await response.json();
      
      if (result.success) {
        setData(result.data.content || []);
        setTotalPages(result.data.totalPages || 0);
        setTotalElements(result.data.totalElements || 0);
      } else {
        setError(result.error || '데이터를 불러오는데 실패했습니다.');
      }
    } catch (err) {
      console.error('데이터 로딩 오류:', err);
      setError('서버와 통신 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleRefresh = () => {
    setRefreshing(true);
    loadData();
  };

  const handleDelete = async (id) => {
    if (!window.confirm('정말로 이 데이터를 삭제하시겠습니까?')) {
      return;
    }

    try {
      const endpoints = {
        inbody: `/api/admin/content/inbody/${id}`,
        analysis: `/api/admin/content/analysis/${id}`,
        workout: `/api/admin/content/workout-recommendations/${id}`,
        diet: `/api/admin/content/diet-recommendations/${id}`,
        survey: `/api/admin/content/surveys/${id}`
      };

      const response = await fetch(endpoints[activeTab], {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      // 응답이 없는 경우 처리
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `HTTP ${response.status}: ${response.statusText}`);
      }

      // 응답 body가 있는 경우에만 JSON 파싱
      const contentType = response.headers.get('content-type');
      let result = null;
      if (contentType && contentType.includes('application/json')) {
        const text = await response.text();
        result = text ? JSON.parse(text) : null;
      }

      if (result && result.success) {
        alert('데이터가 삭제되었습니다.');
        loadData();
      } else if (result && result.error) {
        alert('삭제에 실패했습니다: ' + result.error);
      } else {
        // 응답이 없거나 성공 플래그가 없는 경우에도 삭제가 성공했을 수 있음
        alert('데이터가 삭제되었습니다.');
        loadData();
      }
    } catch (err) {
      console.error('삭제 오류:', err);
      alert('삭제에 실패했습니다: ' + (err.message || '알 수 없는 오류'));
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('ko-KR');
  };

  const renderContent = () => {
    switch (activeTab) {
      case 'inbody':
        return data.map((item) => (
          <tr key={item.id} className="hover:bg-gray-50">
            <td className="px-6 py-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-br from-blue-400 to-blue-600 rounded-full flex items-center justify-center text-white font-bold">
                  {item.userEmail?.charAt(0).toUpperCase()}
                </div>
                <div>
                  <p className="font-medium text-gray-900">{item.userEmail?.split('@')[0]}</p>
                  <p className="text-sm text-gray-500">{item.userEmail}</p>
                </div>
              </div>
            </td>
            <td className="px-6 py-4 text-sm text-gray-900">
              <div className="space-y-1">
                <p>체중: {item.weight}kg</p>
                <p>체지방률: {item.bodyFatPercentage}%</p>
                <p>골격근량: {item.skeletalMuscleMass}kg</p>
              </div>
            </td>
            <td className="px-6 py-4 text-sm text-gray-500">
              {formatDate(item.recordedAt)}
            </td>
            <td className="px-6 py-4">
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handleDelete(item.id)}
                  className="p-1 text-red-600 hover:bg-red-100 rounded"
                  title="삭제"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </td>
          </tr>
        ));

      case 'analysis':
        return data.map((item) => (
          <tr key={item.id} className="hover:bg-gray-50">
            <td className="px-6 py-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-br from-green-400 to-green-600 rounded-full flex items-center justify-center text-white font-bold">
                  {item.userEmail?.charAt(0).toUpperCase()}
                </div>
                <div>
                  <p className="font-medium text-gray-900">{item.userEmail?.split('@')[0]}</p>
                  <p className="text-sm text-gray-500">{item.userEmail}</p>
                </div>
              </div>
            </td>
            <td className="px-6 py-4 text-sm text-gray-900">
              <div className="max-w-xs">
                <p className="truncate">{item.bodyType}</p>
                <p className="text-xs text-gray-500 truncate">{item.analysisResult}</p>
              </div>
            </td>
            <td className="px-6 py-4 text-sm text-gray-500">
              {formatDate(item.analyzedAt)}
            </td>
            <td className="px-6 py-4">
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handleDelete(item.id)}
                  className="p-1 text-red-600 hover:bg-red-100 rounded"
                  title="삭제"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </td>
          </tr>
        ));

      case 'workout':
        return data.map((item) => (
          <tr key={item.id} className="hover:bg-gray-50">
            <td className="px-6 py-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-br from-orange-400 to-orange-600 rounded-full flex items-center justify-center text-white font-bold">
                  {item.userEmail?.charAt(0).toUpperCase()}
                </div>
                <div>
                  <p className="font-medium text-gray-900">{item.userEmail?.split('@')[0]}</p>
                  <p className="text-sm text-gray-500">{item.userEmail}</p>
                </div>
              </div>
            </td>
            <td className="px-6 py-4 text-sm text-gray-900">
              <div className="max-w-xs">
                <p className="truncate">{item.workoutType}</p>
                <p className="text-xs text-gray-500 truncate">{item.recommendation}</p>
              </div>
            </td>
            <td className="px-6 py-4 text-sm text-gray-500">
              {formatDate(item.recommendedAt)}
            </td>
            <td className="px-6 py-4">
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handleDelete(item.id)}
                  className="p-1 text-red-600 hover:bg-red-100 rounded"
                  title="삭제"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </td>
          </tr>
        ));

      case 'diet':
        return data.map((item) => (
          <tr key={item.id} className="hover:bg-gray-50">
            <td className="px-6 py-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-br from-purple-400 to-purple-600 rounded-full flex items-center justify-center text-white font-bold">
                  {item.userEmail?.charAt(0).toUpperCase()}
                </div>
                <div>
                  <p className="font-medium text-gray-900">{item.userEmail?.split('@')[0]}</p>
                  <p className="text-sm text-gray-500">{item.userEmail}</p>
                </div>
              </div>
            </td>
            <td className="px-6 py-4 text-sm text-gray-900">
              <div className="max-w-xs">
                <p className="truncate">{item.mealStyle}</p>
                <p className="text-xs text-gray-500">칼로리: {item.dailyCalories}kcal</p>
              </div>
            </td>
            <td className="px-6 py-4 text-sm text-gray-500">
              {formatDate(item.recommendedAt)}
            </td>
            <td className="px-6 py-4">
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handleDelete(item.id)}
                  className="p-1 text-red-600 hover:bg-red-100 rounded"
                  title="삭제"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </td>
          </tr>
        ));

      case 'survey':
        return data.map((item) => (
          <tr key={item.id} className="hover:bg-gray-50">
            <td className="px-6 py-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gradient-to-br from-pink-400 to-pink-600 rounded-full flex items-center justify-center text-white font-bold">
                  {item.userEmail?.charAt(0).toUpperCase()}
                </div>
                <div>
                  <p className="font-medium text-gray-900">{item.userEmail?.split('@')[0]}</p>
                  <p className="text-sm text-gray-500">{item.userEmail}</p>
                </div>
              </div>
            </td>
            <td className="px-6 py-4 text-sm text-gray-900">
              <div className="max-w-xs">
                <p className="truncate">{item.answerText}</p>
              </div>
            </td>
            <td className="px-6 py-4 text-sm text-gray-500">
              {formatDate(item.submittedAt)}
            </td>
            <td className="px-6 py-4">
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handleDelete(item.id)}
                  className="p-1 text-red-600 hover:bg-red-100 rounded"
                  title="삭제"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </td>
          </tr>
        ));

      default:
        return null;
    }
  };

  const getTableHeaders = () => {
    switch (activeTab) {
      case 'inbody':
        return (
          <>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">사용자</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">인바디 데이터</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">측정일</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">관리</th>
          </>
        );
      case 'analysis':
        return (
          <>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">사용자</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">분석 결과</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">분석일</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">관리</th>
          </>
        );
      case 'workout':
        return (
          <>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">사용자</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">추천 내용</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">추천일</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">관리</th>
          </>
        );
      case 'diet':
        return (
          <>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">사용자</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">식단 추천</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">추천일</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">관리</th>
          </>
        );
      case 'survey':
        return (
          <>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">사용자</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">응답 내용</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">제출일</th>
            <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">관리</th>
          </>
        );
      default:
        return null;
    }
  };

  if (loading) {
    return (
      <AdminLayout>
        <div className="text-center py-16">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-red-500 mx-auto mb-4"></div>
          <p className="text-xl text-gray-600">콘텐츠 데이터를 불러오는 중...</p>
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
                <FileText className="w-8 h-8" />
                <h1 className="text-3xl font-bold">콘텐츠 관리</h1>
              </div>
              <p className="text-red-100 text-lg">
                사용자 생성 콘텐츠 관리
              </p>
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

        {/* Tab Navigation */}
        <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <div className="flex flex-wrap gap-2">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => {
                    setActiveTab(tab.id);
                    setCurrentPage(0);
                  }}
                  className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                    activeTab === tab.id
                      ? 'bg-red-600 text-white'
                      : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {tab.label}
                </button>
              );
            })}
          </div>
        </div>

        {/* Search and Filters */}
        <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="relative">
              <Search className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setCurrentPage(0);
                }}
                placeholder="사용자 이메일로 검색..."
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-transparent"
              />
            </div>
            
            <input
              type="date"
              value={dateFilter}
              onChange={(e) => {
                setDateFilter(e.target.value);
                setCurrentPage(0);
              }}
              className="px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-transparent"
            />
            
            <div className="flex items-center gap-2 text-sm text-gray-600">
              <Filter className="w-4 h-4" />
              <span>페이지: {currentPage + 1} / {totalPages}</span>
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

        {/* Content Table */}
        <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  {getTableHeaders()}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {renderContent()}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="bg-gray-50 px-6 py-4 flex items-center justify-between">
              <div className="text-sm text-gray-700">
                전체 {totalElements}개 중 {currentPage * 20 + 1}-{Math.min((currentPage + 1) * 20, totalElements)}개 표시
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                  disabled={currentPage === 0}
                  className="p-2 rounded-lg border border-gray-300 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <ChevronLeft className="w-4 h-4" />
                </button>
                <span className="px-3 py-1 bg-red-500 text-white rounded-lg">
                  {currentPage + 1}
                </span>
                <button
                  onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                  disabled={currentPage >= totalPages - 1}
                  className="p-2 rounded-lg border border-gray-300 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Empty State */}
        {data.length === 0 && !loading && (
          <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
            <FileText className="w-16 h-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">데이터가 없습니다</h3>
            <p className="text-gray-500">
              {searchTerm || dateFilter 
                ? '검색 조건에 맞는 데이터가 없습니다.' 
                : '등록된 데이터가 없습니다.'}
            </p>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default AdminContentPage;
