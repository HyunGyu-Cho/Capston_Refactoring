import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Users, 
  Search, 
  Filter, 
  Edit3, 
  UserCheck, 
  UserX, 
  Shield, 
  User, 
  Crown,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
  AlertCircle
} from 'lucide-react';
import AdminLayout from '../components/AdminLayout';

const AdminUsersPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [refreshing, setRefreshing] = useState(false);
  const navigate = useNavigate();

  const admin = JSON.parse(sessionStorage.getItem('currentAdmin'));
  const token = sessionStorage.getItem('adminToken');

  useEffect(() => {
    if (!admin || !token || admin.role !== 'ADMIN') {
      navigate('/admin/login');
      return;
    }
    loadUsers();
  }, [currentPage, searchTerm]);

  const loadUsers = async () => {
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

      const response = await fetch(`/api/admin/users?${params}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      const data = await response.json();
      
      if (data.success) {
        setUsers(data.data.content || []);
        setTotalPages(data.data.totalPages || 0);
        setTotalElements(data.data.totalElements || 0);
      } else {
        setError(data.error || '사용자 목록을 불러오는데 실패했습니다.');
      }
    } catch (err) {
      console.error('사용자 목록 로딩 오류:', err);
      setError('서버와 통신 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleRefresh = () => {
    setRefreshing(true);
    loadUsers();
  };

  const handleRoleChange = async (userId, newRole) => {
    if (!window.confirm(`사용자 역할을 ${newRole}로 변경하시겠습니까?`)) {
      return;
    }

    try {
      const response = await fetch(`/api/admin/users/${userId}/role?role=${newRole}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      const data = await response.json();

      if (data.success) {
        alert('사용자 역할이 변경되었습니다.');
        loadUsers();
      } else {
        alert('역할 변경에 실패했습니다: ' + (data.error || '알 수 없는 오류'));
      }
    } catch (err) {
      console.error('역할 변경 오류:', err);
      alert('서버와 통신 중 오류가 발생했습니다.');
    }
  };

  const handleStatusChange = async (userId, isDeleted) => {
    const action = isDeleted ? '비활성화' : '활성화';
    if (!window.confirm(`사용자를 ${action}하시겠습니까?`)) {
      return;
    }

    try {
      const response = await fetch(`/api/admin/users/${userId}/status?isDeleted=${isDeleted}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      const data = await response.json();

      if (data.success) {
        alert(`사용자가 ${action}되었습니다.`);
        loadUsers();
      } else {
        alert(`${action}에 실패했습니다: ` + (data.error || '알 수 없는 오류'));
      }
    } catch (err) {
      console.error('상태 변경 오류:', err);
      alert('서버와 통신 중 오류가 발생했습니다.');
    }
  };

  const getRoleIcon = (role) => {
    switch (role) {
      case 'ADMIN': return <Crown className="w-4 h-4 text-yellow-500" />;
      case 'MANAGER': return <Shield className="w-4 h-4 text-blue-500" />;
      default: return <User className="w-4 h-4 text-gray-500" />;
    }
  };

  const getRoleBadgeColor = (role) => {
    switch (role) {
      case 'ADMIN': return 'bg-yellow-100 text-yellow-800 border-yellow-300';
      case 'MANAGER': return 'bg-blue-100 text-blue-800 border-blue-300';
      default: return 'bg-gray-100 text-gray-800 border-gray-300';
    }
  };

  const getStatusBadgeColor = (isDeleted) => {
    return isDeleted 
      ? 'bg-red-100 text-red-800 border-red-300'
      : 'bg-green-100 text-green-800 border-green-300';
  };

  if (loading) {
    return (
      <AdminLayout>
        <div className="text-center py-16">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-red-500 mx-auto mb-4"></div>
          <p className="text-xl text-gray-600">사용자 목록을 불러오는 중...</p>
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
                <Users className="w-8 h-8" />
                <h1 className="text-3xl font-bold">사용자 관리</h1>
              </div>
              <p className="text-red-100 text-lg">
                전체 사용자: {totalElements}명
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

        {/* Search Bar */}
        <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <div className="flex items-center gap-4">
            <div className="relative flex-1">
              <Search className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setCurrentPage(0);
                }}
                placeholder="이메일이나 이름으로 검색..."
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-transparent"
              />
            </div>
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

        {/* Users Table */}
        <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">사용자</th>
                  <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">역할</th>
                  <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">상태</th>
                  <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">가입일</th>
                  <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">관리</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {users.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-gradient-to-br from-red-400 to-red-600 rounded-full flex items-center justify-center text-white font-bold">
                          {user.email.charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <p className="font-medium text-gray-900">{user.email.split('@')[0]}</p>
                          <p className="text-sm text-gray-500">{user.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        {getRoleIcon(user.role)}
                        <span className={`px-2 py-1 text-xs font-medium rounded-md border ${getRoleBadgeColor(user.role)}`}>
                          {user.role}
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-1 text-xs font-medium rounded-md border ${getStatusBadgeColor(user.isDeleted)}`}>
                        {user.isDeleted ? '비활성' : '활성'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {user.createdAt ? new Date(user.createdAt).toLocaleDateString('ko-KR') : '-'}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <select
                          value={user.role}
                          onChange={(e) => handleRoleChange(user.id, e.target.value)}
                          className="text-xs border border-gray-300 rounded px-2 py-1"
                          disabled={user.id === admin?.id}
                        >
                          <option value="USER">USER</option>
                          <option value="MANAGER">MANAGER</option>
                          <option value="ADMIN">ADMIN</option>
                        </select>
                        <button
                          onClick={() => handleStatusChange(user.id, !user.isDeleted)}
                          disabled={user.id === admin?.id}
                          className={`p-1 rounded ${
                            user.isDeleted 
                              ? 'text-green-600 hover:bg-green-100' 
                              : 'text-red-600 hover:bg-red-100'
                          } disabled:opacity-50 disabled:cursor-not-allowed`}
                          title={user.isDeleted ? '활성화' : '비활성화'}
                        >
                          {user.isDeleted ? <UserCheck className="w-4 h-4" /> : <UserX className="w-4 h-4" />}
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
                전체 {totalElements}명 중 {currentPage * 20 + 1}-{Math.min((currentPage + 1) * 20, totalElements)}명 표시
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
        {users.length === 0 && !loading && (
          <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
            <Users className="w-16 h-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">사용자가 없습니다</h3>
            <p className="text-gray-500">
              {searchTerm ? '검색 조건에 맞는 사용자가 없습니다.' : '등록된 사용자가 없습니다.'}
            </p>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default AdminUsersPage;
