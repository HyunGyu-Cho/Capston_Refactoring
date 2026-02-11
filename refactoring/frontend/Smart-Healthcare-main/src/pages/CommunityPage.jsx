// src/pages/CommunityPage.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { apiCall } from '../api/config';
import { useUser } from '../api/auth';
import Button from '../components/Button';
import InputField from '../components/InputField';
import Card from '../components/Card';
import { categories, getCategoryDisplayName, getCategoryStyle } from '../utils/categoryUtils';

export default function CommunityPage() {
  const navigate = useNavigate();
  const { user: currentUser } = useUser();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // 검색 및 필터링 상태
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedTag, setSelectedTag] = useState('');
  const [sortType, setSortType] = useState('recent');
  
  // 페이지네이션 상태
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;
  
  // 게시글 작성 모달 상태
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    category: 'FREE',
    tags: []
  });
  const [tagInput, setTagInput] = useState('');



  // 정렬 옵션
  const sortOptions = [
    { value: 'recent', label: '최신순' },
    { value: 'popular', label: '인기순' },
    { value: 'comments', label: '댓글순' },
    { value: 'views', label: '조회수순' }
  ];

  // 게시글 목록 조회
  const fetchPosts = async (page = 0, reset = false) => {
    setLoading(true);
    setError('');
    
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: pageSize.toString(),
        sortType: sortType
      });

      if (searchTerm) params.append('search', searchTerm);
      if (selectedCategory) params.append('category', selectedCategory);
      if (selectedTag) params.append('tag', selectedTag);

      const response = await apiCall(`/api/community?${params.toString()}`);
      
      if (response.success !== false) {
        // 새로운 API 응답 구조: response.data.content
        const postsData = response.data?.content || [];
        const paginationData = response.data || {};
        
        if (reset) {
          setPosts(postsData);
        } else {
          setPosts(prev => [...prev, ...postsData]);
        }
        setTotalPages(paginationData.totalPages || 0);
        setTotalElements(paginationData.totalElements || 0);
        setCurrentPage(paginationData.pageable?.pageNumber || 0);
      } else {
        setError(response.error || '게시글을 불러오는데 실패했습니다.');
      }
    } catch (error) {
      console.error('게시글 조회 실패:', error);
      setError('게시글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 초기 로딩
  useEffect(() => {
    fetchPosts(0, true);
  }, [searchTerm, selectedCategory, selectedTag, sortType]);

  // 검색 실행
  const handleSearch = () => {
    setCurrentPage(0);
    fetchPosts(0, true);
  };

  // 필터 초기화
  const resetFilters = () => {
    setSearchTerm('');
    setSelectedCategory('');
    setSelectedTag('');
    setSortType('recent');
    setCurrentPage(0);
    fetchPosts(0, true);
  };

  // 더보기 로딩
  const loadMore = () => {
    if (currentPage < totalPages - 1 && !loading) {
      fetchPosts(currentPage + 1, false);
    }
  };

  // 게시글 클릭 시 상세 페이지로 이동
  const handlePostClick = (postId) => {
    navigate(`/community/${postId}`);
  };

  // 게시글 작성
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!currentUser) {
      setError('로그인이 필요합니다.');
      return;
    }

    try {
      const requestData = {
        ...formData,
        authorId: currentUser.id,
        tags: formData.tags.filter(tag => tag.trim() !== '')
      };

      if (editingId) {
        // 수정
        await apiCall(`/api/community/${editingId}`, {
          method: 'PUT',
          body: JSON.stringify(requestData)
        });
        setEditingId(null);
      } else {
        // 새로 작성
        await apiCall('/api/community', {
          method: 'POST',
          body: JSON.stringify(requestData)
        });
      }

      // 폼 초기화 및 모달 닫기
      setFormData({ title: '', content: '', category: 'FREE', tags: [] });
      setShowCreateModal(false);
      
      // 게시글 목록 새로고침
      fetchPosts(0, true);
    } catch (error) {
      console.error('게시글 저장 실패:', error);
      setError('게시글 저장에 실패했습니다.');
    }
  };

  // 게시글 수정 모드
  const handleEdit = (post) => {
    setEditingId(post.id);
    setFormData({
      title: post.title,
      content: post.content,
      category: post.category,
      tags: Array.from(post.tags || [])
    });
    setShowCreateModal(true);
  };

  // 게시글 삭제
  const handleDelete = async (id) => {
    if (!window.confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
      return;
    }

    try {
      const authorId = currentUser?.id;
      if (!authorId) {
        setError('로그인이 필요합니다.');
        return;
      }
      await apiCall(`/api/community/${id}?authorId=${authorId}`, { method: 'DELETE' });
      fetchPosts(0, true);
    } catch (error) {
      console.error('게시글 삭제 실패:', error);
      setError('게시글 삭제에 실패했습니다.');
    }
  };

  // 태그 추가
  const addTag = () => {
    if (tagInput.trim() && !formData.tags.includes(tagInput.trim())) {
      setFormData(prev => ({
        ...prev,
        tags: [...prev.tags, tagInput.trim()]
      }));
      setTagInput('');
    }
  };

  // 태그 제거
  const removeTag = (tagToRemove) => {
    setFormData(prev => ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }));
  };

  // 날짜 포맷팅
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <Layout>
      <div className="w-full max-w-6xl mx-auto mt-24 p-6 bg-white min-h-screen">
        {/* 헤더 */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-black mb-2">커뮤니티</h1>
          <p className="text-gray-600">건강한 삶을 위한 정보를 공유하고 소통해보세요!</p>
        </div>

        {/* 검색 및 필터 */}
        <div className="mb-6 p-4 bg-gray-50 rounded border border-gray-200">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
            <input
              type="text"
              placeholder="제목 또는 내용으로 검색..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
            />
            
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
            >
              <option value="">모든 카테고리</option>
              {categories.map(cat => (
                <option key={cat.value} value={cat.value}>{cat.label}</option>
              ))}
            </select>

            <input
              type="text"
              placeholder="태그로 검색..."
              value={selectedTag}
              onChange={(e) => setSelectedTag(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
            />

            <select
              value={sortType}
              onChange={(e) => setSortType(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
            >
              {sortOptions.map(option => (
                <option key={option.value} value={option.value}>{option.label}</option>
              ))}
            </select>
          </div>

          <div className="flex gap-2">
            <button onClick={handleSearch} className="px-3 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600">
              검색
            </button>
            <button onClick={resetFilters} className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200">
              초기화
            </button>
            <button 
              onClick={() => setShowCreateModal(true)} 
              className="px-3 py-1 text-sm bg-green-500 text-white rounded hover:bg-green-600 ml-auto"
            >
              글쓰기
            </button>
          </div>
        </div>

        {/* 에러 메시지 */}
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        {/* 게시글 목록 */}
        <div className="space-y-0 border-t border-gray-200">
          {posts.map(post => (
            <div key={post.id} className="border-b border-gray-200 py-4 hover:bg-gray-50 cursor-pointer" onClick={() => handlePostClick(post.id)}>
              {/* 카테고리 */}
              <div className="text-xs text-gray-500 mb-1 text-left">
                {getCategoryDisplayName(post.category)}
              </div>
              
              {/* 제목 */}
              <h3 className="text-base font-medium text-black mb-2 hover:text-blue-600 text-left">
                {post.title}
              </h3>
              
              {/* 작성자 정보 */}
              <div className="flex items-center gap-4 text-xs text-gray-500 mb-2 text-left">
                <span className="font-medium">{post.authorName?.split('@')[0] || '익명'}</span>
                <span>|</span>
                <span>조회 {post.viewCount || 0}</span>
                <span>|</span>
                <span>{formatDate(post.createdAt)}</span>
              </div>

              {/* 하단 정보 */}
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-4 text-xs text-gray-500 text-left">
                  {post.likeCount > 0 && (
                    <>
                      <span>추천 {post.likeCount}</span>
                      <span>|</span>
                    </>
                  )}
                  <span>{post.commentCount || 0} 댓글</span>
                </div>

                <div className="flex items-center gap-2" onClick={(e) => e.stopPropagation()}>
                  {/* 수정/삭제 버튼 (작성자 또는 관리자만) */}
                  {currentUser && (currentUser.id === post.authorId || currentUser.role === 'ADMIN') && (
                    <>
                      <button 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleEdit(post);
                        }} 
                        className="text-xs text-blue-600 hover:underline"
                      >
                        수정
                      </button>
                      <button 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete(post.id);
                        }} 
                        className="text-xs text-red-600 hover:underline"
                      >
                        삭제
                      </button>
                    </>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* 더보기 버튼 */}
        {currentPage < totalPages - 1 && (
          <div className="text-center mt-6">
            <button 
              onClick={loadMore} 
              disabled={loading}
              className="w-full max-w-md px-4 py-2 border border-gray-300 rounded text-gray-700 hover:bg-gray-50 disabled:opacity-50"
            >
              {loading ? '로딩 중...' : '더보기'}
            </button>
          </div>
        )}

        {/* 게시글이 없을 때 */}
        {posts.length === 0 && !loading && (
          <div className="text-center py-12">
            <div className="text-gray-500">
              <p className="text-lg mb-2">아직 게시글이 없습니다</p>
              <p className="text-sm">첫 번째 게시글을 작성해보세요!</p>
            </div>
          </div>
        )}

        {/* 게시글 작성/수정 모달 */}
        {showCreateModal && (
          <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50 p-4">
            <div className="w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-white rounded border border-gray-200">
              <div className="flex justify-between items-center mb-4 p-4 border-b border-gray-200">
                <h2 className="text-lg font-bold text-black">
                  {editingId ? '게시글 수정' : '새 게시글 작성'}
                </h2>
                <button
                  onClick={() => {
                    setShowCreateModal(false);
                    setEditingId(null);
                    setFormData({ title: '', content: '', category: 'FREE', tags: [] });
                  }}
                  className="text-gray-500 hover:text-gray-700 text-xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleSubmit} className="p-4 space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    제목
                  </label>
                  <input
                    type="text"
                    value={formData.title}
                    onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
                    required
                    placeholder="제목을 입력하세요"
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    카테고리
                  </label>
                  <select
                    value={formData.category}
                    onChange={(e) => setFormData(prev => ({ ...prev, category: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                    required
                  >
                    {categories.map(cat => (
                      <option key={cat.value} value={cat.value}>{cat.label}</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    내용
                  </label>
                  <textarea
                    value={formData.content}
                    onChange={(e) => setFormData(prev => ({ ...prev, content: e.target.value }))}
                    required
                    rows={6}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                    placeholder="내용을 입력하세요"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    태그
                  </label>
                  <div className="flex gap-2 mb-2">
                    <input
                      type="text"
                      value={tagInput}
                      onChange={(e) => setTagInput(e.target.value)}
                      placeholder="태그를 입력하세요"
                      onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addTag())}
                      className="flex-1 px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                    />
                    <button type="button" onClick={addTag} className="px-3 py-2 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200">
                      추가
                    </button>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {formData.tags.map(tag => (
                      <span 
                        key={tag} 
                        className="px-2 py-1 bg-gray-100 text-gray-700 text-sm rounded flex items-center gap-1"
                      >
                        #{tag}
                        <button
                          type="button"
                          onClick={() => removeTag(tag)}
                          className="text-gray-500 hover:text-gray-700"
                        >
                          ×
                        </button>
                      </span>
                    ))}
                  </div>
                </div>

                <div className="flex gap-2 justify-end pt-4 border-t border-gray-200">
                  <button 
                    type="button" 
                    onClick={() => {
                      setShowCreateModal(false);
                      setEditingId(null);
                      setFormData({ title: '', content: '', category: 'FREE', tags: [] });
                    }}
                    className="px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
                  >
                    취소
                  </button>
                  <button 
                    type="submit"
                    className="px-4 py-2 text-sm bg-blue-500 text-white rounded hover:bg-blue-600"
                  >
                    {editingId ? '수정하기' : '작성하기'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}