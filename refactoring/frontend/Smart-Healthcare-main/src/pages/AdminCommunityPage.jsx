import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  MessageSquare, 
  Search, 
  Filter, 
  Edit3, 
  Trash2, 
  Eye,
  Flag,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
  AlertCircle,
  CheckCircle,
  XCircle,
  MoreHorizontal
} from 'lucide-react';
import AdminLayout from '../components/AdminLayout';

const AdminCommunityPage = () => {
  const [posts, setPosts] = useState([]);
  const [comments, setComments] = useState([]);
  const [activeTab, setActiveTab] = useState('posts');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [refreshing, setRefreshing] = useState(false);
  const navigate = useNavigate();

  const admin = JSON.parse(sessionStorage.getItem('currentAdmin'));
  const token = sessionStorage.getItem('adminToken');

  const categories = [
    { value: 'EXERCISE', label: '운동' },
    { value: 'DIET', label: '식단' },
    { value: 'QUESTION', label: '질문' },
    { value: 'FREE', label: '자유게시판' },
    { value: 'TIP', label: '팁' },
    { value: 'REVIEW', label: '후기' },
    { value: 'SUCCESS_STORY', label: '성공후기' }
  ];

  useEffect(() => {
    if (!admin || !token || admin.role !== 'ADMIN') {
      navigate('/admin/login');
      return;
    }
    loadData();
  }, [currentPage, searchTerm, categoryFilter, statusFilter, activeTab]);

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
      if (categoryFilter) {
        params.append('category', categoryFilter);
      }
      if (statusFilter) {
        params.append('status', statusFilter);
      }

      const endpoint = activeTab === 'posts' ? '/api/admin/community/posts' : '/api/admin/community/comments';
      const response = await fetch(`${endpoint}?${params}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      const data = await response.json();
      
      if (data.success) {
        if (activeTab === 'posts') {
          setPosts(data.data.content || []);
        } else {
          setComments(data.data.content || []);
        }
        setTotalPages(data.data.totalPages || 0);
        setTotalElements(data.data.totalElements || 0);
      } else {
        setError(data.error || `${activeTab === 'posts' ? '게시글' : '댓글'} 목록을 불러오는데 실패했습니다.`);
      }
    } catch (err) {
      console.error(`${activeTab} 목록 로딩 오류:`, err);
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

  const handleDelete = async (id, type) => {
    const itemName = type === 'post' ? '게시글' : '댓글';
    if (!window.confirm(`정말로 이 ${itemName}을 삭제하시겠습니까?`)) {
      return;
    }

    try {
      const endpoint = type === 'post' ? `/api/admin/community/posts/${id}` : `/api/admin/community/comments/${id}`;
      const response = await fetch(endpoint, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      const data = await response.json();

      if (data.success) {
        alert(`${itemName}이 삭제되었습니다.`);
        loadData();
      } else {
        alert(`${itemName} 삭제에 실패했습니다: ` + (data.error || '알 수 없는 오류'));
      }
    } catch (err) {
      console.error(`${itemName} 삭제 오류:`, err);
      alert('서버와 통신 중 오류가 발생했습니다.');
    }
  };

  const handleRestore = async (id, type) => {
    const itemName = type === 'post' ? '게시글' : '댓글';
    if (!window.confirm(`정말로 이 ${itemName}을 복원하시겠습니까?`)) {
      return;
    }

    try {
      const endpoint = type === 'post' ? `/api/admin/community/posts/${id}/restore` : `/api/admin/community/comments/${id}/restore`;
      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      const data = await response.json();

      if (data.success) {
        alert(`${itemName}이 복원되었습니다.`);
        loadData();
      } else {
        alert(`${itemName} 복원에 실패했습니다: ` + (data.error || '알 수 없는 오류'));
      }
    } catch (err) {
      console.error(`${itemName} 복원 오류:`, err);
      alert('서버와 통신 중 오류가 발생했습니다.');
    }
  };

  const getCategoryLabel = (category) => {
    const found = categories.find(cat => cat.value === category);
    return found ? found.label : category;
  };

  const getCategoryStyle = (category) => {
    const categoryValue = category;
    switch (categoryValue) {
      case 'EXERCISE':
        return 'bg-blue-100 text-blue-800';
      case 'DIET':
        return 'bg-green-100 text-green-800';
      case 'QUESTION':
        return 'bg-yellow-100 text-yellow-800';
      case 'TIP':
        return 'bg-purple-100 text-purple-800';
      case 'REVIEW':
        return 'bg-orange-100 text-orange-800';
      case 'SUCCESS_STORY':
        return 'bg-pink-100 text-pink-800';
      case 'FREE':
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusBadge = (isDeleted) => {
    return isDeleted 
      ? { color: 'bg-red-100 text-red-800 border-red-300', text: '삭제됨', icon: <XCircle className="w-4 h-4" /> }
      : { color: 'bg-green-100 text-green-800 border-green-300', text: '활성', icon: <CheckCircle className="w-4 h-4" /> };
  };

  if (loading) {
    return (
      <AdminLayout>
        <div className="text-center py-16">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-red-500 mx-auto mb-4"></div>
          <p className="text-xl text-gray-600">커뮤니티 데이터를 불러오는 중...</p>
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
                <MessageSquare className="w-8 h-8" />
                <h1 className="text-3xl font-bold">커뮤니티 관리</h1>
              </div>
              <p className="text-red-100 text-lg">
                게시글 및 댓글 관리
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
          <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg">
            <button
              onClick={() => {
                setActiveTab('posts');
                setCurrentPage(0);
              }}
              className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
                activeTab === 'posts'
                  ? 'bg-white text-red-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              게시글 관리
            </button>
            <button
              onClick={() => {
                setActiveTab('comments');
                setCurrentPage(0);
              }}
              className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
                activeTab === 'comments'
                  ? 'bg-white text-red-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              댓글 관리
            </button>
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
                placeholder="제목, 내용, 작성자로 검색..."
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-transparent"
              />
            </div>
            
            {activeTab === 'posts' && (
              <select
                value={categoryFilter}
                onChange={(e) => {
                  setCategoryFilter(e.target.value);
                  setCurrentPage(0);
                }}
                className="px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-transparent"
              >
                <option value="">모든 카테고리</option>
                {categories.map(category => (
                  <option key={category.value} value={category.value}>
                    {category.label}
                  </option>
                ))}
              </select>
            )}
            
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setCurrentPage(0);
              }}
              className="px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-transparent"
            >
              <option value="">모든 상태</option>
              <option value="active">활성</option>
              <option value="deleted">삭제됨</option>
            </select>
            
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
                  {activeTab === 'posts' ? (
                    <>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">게시글</th>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">카테고리</th>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">작성자</th>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">상태</th>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">작성일</th>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">관리</th>
                    </>
                  ) : (
                    <>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">댓글</th>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">게시글</th>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">작성자</th>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">상태</th>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">작성일</th>
                      <th className="px-6 py-4 text-left text-sm font-medium text-gray-900">관리</th>
                    </>
                  )}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {activeTab === 'posts' ? (
                  posts.map((post) => {
                    const statusBadge = getStatusBadge(post.isDeleted);
                    return (
                      <tr key={post.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4">
                          <div className="max-w-xs">
                            <p className="font-medium text-gray-900 truncate">{post.title}</p>
                            <p className="text-sm text-gray-500 truncate">{post.content}</p>
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <span className={`px-2 py-1 text-xs font-medium rounded-md border ${getCategoryStyle(post.category)}`}>
                            {getCategoryLabel(post.category)}
                          </span>
                        </td>
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-2">
                            <div className="w-8 h-8 bg-gradient-to-br from-red-400 to-red-600 rounded-full flex items-center justify-center text-white text-xs font-bold">
                              {post.authorEmail?.charAt(0).toUpperCase()}
                            </div>
                            <span className="text-sm text-gray-900">{post.authorEmail?.split('@')[0]}</span>
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-2">
                            {statusBadge.icon}
                            <span className={`px-2 py-1 text-xs font-medium rounded-md border ${statusBadge.color}`}>
                              {statusBadge.text}
                            </span>
                          </div>
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-500">
                          {post.createdAt ? new Date(post.createdAt).toLocaleDateString('ko-KR') : '-'}
                        </td>
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => navigate(`/community/${post.id}`)}
                              className="p-1 text-blue-600 hover:bg-blue-100 rounded"
                              title="게시글 보기"
                            >
                              <Eye className="w-4 h-4" />
                            </button>
                            {post.isDeleted ? (
                              <button
                                onClick={() => handleRestore(post.id, 'post')}
                                className="p-1 text-green-600 hover:bg-green-100 rounded"
                                title="복원"
                              >
                                <CheckCircle className="w-4 h-4" />
                              </button>
                            ) : (
                              <button
                                onClick={() => handleDelete(post.id, 'post')}
                                className="p-1 text-red-600 hover:bg-red-100 rounded"
                                title="삭제"
                              >
                                <Trash2 className="w-4 h-4" />
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })
                ) : (
                  comments.map((comment) => {
                    const statusBadge = getStatusBadge(comment.isDeleted);
                    return (
                      <tr key={comment.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4">
                          <div className="max-w-xs">
                            <p className="text-sm text-gray-900 truncate">{comment.content}</p>
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <button
                            onClick={() => navigate(`/community/${comment.postId}`)}
                            className="text-sm text-blue-600 hover:underline"
                          >
                            게시글 보기
                          </button>
                        </td>
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-2">
                            <div className="w-8 h-8 bg-gradient-to-br from-red-400 to-red-600 rounded-full flex items-center justify-center text-white text-xs font-bold">
                              {comment.authorEmail?.charAt(0).toUpperCase()}
                            </div>
                            <span className="text-sm text-gray-900">{comment.authorEmail?.split('@')[0]}</span>
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-2">
                            {statusBadge.icon}
                            <span className={`px-2 py-1 text-xs font-medium rounded-md border ${statusBadge.color}`}>
                              {statusBadge.text}
                            </span>
                          </div>
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-500">
                          {comment.createdAt ? new Date(comment.createdAt).toLocaleDateString('ko-KR') : '-'}
                        </td>
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-2">
                            {comment.isDeleted ? (
                              <button
                                onClick={() => handleRestore(comment.id, 'comment')}
                                className="p-1 text-green-600 hover:bg-green-100 rounded"
                                title="복원"
                              >
                                <CheckCircle className="w-4 h-4" />
                              </button>
                            ) : (
                              <button
                                onClick={() => handleDelete(comment.id, 'comment')}
                                className="p-1 text-red-600 hover:bg-red-100 rounded"
                                title="삭제"
                              >
                                <Trash2 className="w-4 h-4" />
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })
                )}
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
        {(activeTab === 'posts' ? posts.length === 0 : comments.length === 0) && !loading && (
          <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
            <MessageSquare className="w-16 h-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              {activeTab === 'posts' ? '게시글이' : '댓글이'} 없습니다
            </h3>
            <p className="text-gray-500">
              {searchTerm || categoryFilter || statusFilter 
                ? '검색 조건에 맞는 데이터가 없습니다.' 
                : `등록된 ${activeTab === 'posts' ? '게시글' : '댓글'}이 없습니다.`}
            </p>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default AdminCommunityPage;
