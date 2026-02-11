import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  FileText, 
  Search, 
  Filter, 
  ChevronLeft,
  ChevronRight,
  RefreshCw,
  AlertCircle,
  Info,
  AlertTriangle,
  XCircle,
  Eye,
  Download,
  Calendar,
  Clock,
  User,
  Activity,
  Database
} from 'lucide-react';
import AdminLayout from '../components/AdminLayout';

const AdminLogsPage = () => {
  const [logs, setLogs] = useState([]);
  const [activeTab, setActiveTab] = useState('activity');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [levelFilter, setLevelFilter] = useState('');
  const [dateFilter, setDateFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [refreshing, setRefreshing] = useState(false);
  const [selectedLog, setSelectedLog] = useState(null);
  const navigate = useNavigate();

  const admin = JSON.parse(sessionStorage.getItem('currentAdmin'));
  const token = sessionStorage.getItem('adminToken');

  const logTypes = [
    { id: 'activity', label: '활동 로그', icon: Activity },
    { id: 'error', label: '오류 로그', icon: AlertTriangle },
    { id: 'system', label: '시스템 로그', icon: Database },
    { id: 'security', label: '보안 로그', icon: AlertCircle }
  ];

  const logLevels = [
    { value: '', label: '모든 레벨' },
    { value: 'INFO', label: '정보' },
    { value: 'WARN', label: '경고' },
    { value: 'ERROR', label: '오류' },
    { value: 'DEBUG', label: '디버그' }
  ];

  useEffect(() => {
    if (!admin || !token || admin.role !== 'ADMIN') {
      navigate('/admin/login');
      return;
    }
    loadLogs();
  }, [currentPage, searchTerm, levelFilter, dateFilter, activeTab]);

  const loadLogs = async () => {
    setLoading(true);
    setError('');
    try {
      const params = new URLSearchParams({
        page: currentPage,
        size: 20,
        sort: 'timestamp,desc'
      });
      
      if (searchTerm.trim()) {
        params.append('search', searchTerm.trim());
      }
      if (levelFilter) {
        params.append('level', levelFilter);
      }
      if (dateFilter) {
        params.append('date', dateFilter);
      }

      const endpoints = {
        activity: '/api/admin/logs/activity',
        error: '/api/admin/logs/error',
        system: '/api/admin/logs/system',
        security: '/api/admin/logs/security'
      };

      const response = await fetch(`${endpoints[activeTab]}?${params}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      const result = await response.json();
      
      if (result.success) {
        setLogs(result.data.content || []);
        setTotalPages(result.data.totalPages || 0);
        setTotalElements(result.data.totalElements || 0);
      } else {
        setError(result.error || '로그를 불러오는데 실패했습니다.');
      }
    } catch (err) {
      console.error('로그 로딩 오류:', err);
      setError('서버와 통신 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleRefresh = () => {
    setRefreshing(true);
    loadLogs();
  };

  const handleExportLogs = async () => {
    try {
      const params = new URLSearchParams({
        type: activeTab,
        format: 'csv'
      });
      
      if (searchTerm.trim()) {
        params.append('search', searchTerm.trim());
      }
      if (levelFilter) {
        params.append('level', levelFilter);
      }
      if (dateFilter) {
        params.append('date', dateFilter);
      }

      const response = await fetch(`/api/admin/logs/export?${params}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${activeTab}_logs_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      } else {
        alert('로그 내보내기에 실패했습니다.');
      }
    } catch (err) {
      console.error('로그 내보내기 오류:', err);
      alert('로그 내보내기 중 오류가 발생했습니다.');
    }
  };

  const getLogLevelIcon = (level) => {
    switch (level) {
      case 'INFO':
        return <Info className="w-4 h-4 text-blue-500" />;
      case 'WARN':
        return <AlertTriangle className="w-4 h-4 text-yellow-500" />;
      case 'ERROR':
        return <XCircle className="w-4 h-4 text-red-500" />;
      case 'DEBUG':
        return <AlertCircle className="w-4 h-4 text-gray-500" />;
      default:
        return <Info className="w-4 h-4 text-gray-500" />;
    }
  };

  const getLogLevelColor = (level) => {
    switch (level) {
      case 'INFO':
        return 'bg-blue-100 text-blue-800';
      case 'WARN':
        return 'bg-yellow-100 text-yellow-800';
      case 'ERROR':
        return 'bg-red-100 text-red-800';
      case 'DEBUG':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatTimestamp = (timestamp) => {
    if (!timestamp) return '-';
    return new Date(timestamp).toLocaleString('ko-KR');
  };

  const truncateMessage = (message, maxLength = 100) => {
    if (!message) return '-';
    return message.length > maxLength ? message.substring(0, maxLength) + '...' : message;
  };

  if (loading) {
    return (
      <AdminLayout>
        <div className="text-center py-16">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-red-500 mx-auto mb-4"></div>
          <p className="text-xl text-gray-600">로그를 불러오는 중...</p>
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
                <h1 className="text-3xl font-bold">시스템 로그</h1>
              </div>
              <p className="text-red-100 text-lg">
                시스템 활동 및 오류 로그 관리
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
                onClick={handleExportLogs}
                className="bg-green-500 hover:bg-green-400 p-3 rounded-lg transition-colors"
                title="로그 내보내기"
              >
                <Download className="w-6 h-6" />
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
            {logTypes.map((type) => {
              const Icon = type.icon;
              return (
                <button
                  key={type.id}
                  onClick={() => {
                    setActiveTab(type.id);
                    setCurrentPage(0);
                  }}
                  className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                    activeTab === type.id
                      ? 'bg-red-600 text-white'
                      : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {type.label}
                </button>
              );
            })}
          </div>
        </div>

        {/* Search and Filters */}
        <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="relative">
              <Search className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setCurrentPage(0);
                }}
                placeholder="로그 메시지로 검색..."
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-transparent"
              />
            </div>
            
            <select
              value={levelFilter}
              onChange={(e) => {
                setLevelFilter(e.target.value);
                setCurrentPage(0);
              }}
              className="px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-transparent"
            >
              {logLevels.map(level => (
                <option key={level.value} value={level.value}>
                  {level.label}
                </option>
              ))}
            </select>
            
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

        {/* Logs Table */}
        <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">시간</th>
                  <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">레벨</th>
                  <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">사용자</th>
                  <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">메시지</th>
                  <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">IP 주소</th>
                  <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">관리</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm text-gray-900">
                      <div className="flex items-center gap-2">
                        <Clock className="w-4 h-4 text-gray-400" />
                        {formatTimestamp(log.timestamp)}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        {getLogLevelIcon(log.level)}
                        <span className={`px-2 py-1 text-xs font-medium rounded-md ${getLogLevelColor(log.level)}`}>
                          {log.level}
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <User className="w-4 h-4 text-gray-400" />
                        <span className="text-sm text-gray-900">
                          {log.userEmail || '시스템'}
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      <div className="max-w-xs">
                        <p className="truncate">{truncateMessage(log.message)}</p>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {log.ipAddress || '-'}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => setSelectedLog(log)}
                          className="p-1 text-blue-600 hover:bg-blue-100 rounded"
                          title="상세 보기"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
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

        {/* Log Detail Modal */}
        {selectedLog && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-2xl p-6 max-w-4xl w-full mx-4 max-h-[80vh] overflow-y-auto">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-xl font-bold text-gray-900">로그 상세 정보</h3>
                <button
                  onClick={() => setSelectedLog(null)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <XCircle className="w-6 h-6" />
                </button>
              </div>
              
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">시간</label>
                    <p className="mt-1 text-sm text-gray-900">{formatTimestamp(selectedLog.timestamp)}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">레벨</label>
                    <div className="mt-1 flex items-center gap-2">
                      {getLogLevelIcon(selectedLog.level)}
                      <span className={`px-2 py-1 text-xs font-medium rounded-md ${getLogLevelColor(selectedLog.level)}`}>
                        {selectedLog.level}
                      </span>
                    </div>
                  </div>
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">사용자</label>
                    <p className="mt-1 text-sm text-gray-900">{selectedLog.userEmail || '시스템'}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">IP 주소</label>
                    <p className="mt-1 text-sm text-gray-900">{selectedLog.ipAddress || '-'}</p>
                  </div>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700">메시지</label>
                  <p className="mt-1 text-sm text-gray-900 whitespace-pre-wrap">{selectedLog.message}</p>
                </div>
                
                {selectedLog.details && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700">상세 정보</label>
                    <pre className="mt-1 text-sm text-gray-900 bg-gray-100 p-3 rounded-md overflow-x-auto">
                      {JSON.stringify(selectedLog.details, null, 2)}
                    </pre>
                  </div>
                )}
                
                {selectedLog.stackTrace && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700">스택 트레이스</label>
                    <pre className="mt-1 text-sm text-gray-900 bg-gray-100 p-3 rounded-md overflow-x-auto max-h-40">
                      {selectedLog.stackTrace}
                    </pre>
                  </div>
                )}
              </div>
              
              <div className="mt-6 flex justify-end">
                <button
                  onClick={() => setSelectedLog(null)}
                  className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
                >
                  닫기
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Empty State */}
        {logs.length === 0 && !loading && (
          <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
            <FileText className="w-16 h-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">로그가 없습니다</h3>
            <p className="text-gray-500">
              {searchTerm || levelFilter || dateFilter 
                ? '검색 조건에 맞는 로그가 없습니다.' 
                : '등록된 로그가 없습니다.'}
            </p>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default AdminLogsPage;
