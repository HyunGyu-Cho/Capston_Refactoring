import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { apiCall } from '../api/config';
import { useUser } from '../api/auth';
import Button from '../components/Button';
import InputField from '../components/InputField';
import Card from '../components/Card';
import BackButton from '../components/BackButton';
import { categories } from '../utils/categoryUtils';

export default function CommunityPostEditPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user: currentUser } = useUser();
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [post, setPost] = useState(null);
  
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    category: 'FREE',
    tags: []
  });
  const [tagInput, setTagInput] = useState('');



  // 게시글 데이터 로드
  const fetchPost = async () => {
    setLoading(true);
    setError('');
    
    try {
      const data = await apiCall(`/api/community/${id}`);
      
      if (data.success !== false) {
        // 새로운 API 응답 구조: response.data
        const postData = data.data || data.post;
        setPost(postData);
        setFormData({
          title: postData.title || '',
          content: postData.content || '',
          category: postData.category || 'FREE',
          tags: Array.from(postData.tags || [])
        });
      } else {
        setError(data.error || '게시글을 불러오는데 실패했습니다.');
      }
    } catch (error) {
      console.error('게시글 조회 실패:', error);
      setError('게시글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 권한 확인
  useEffect(() => {
    if (id) {
      fetchPost();
    }
  }, [id]);

  useEffect(() => {
    if (post && currentUser) {
      if (currentUser.id !== post.authorId && currentUser.role !== 'ADMIN') {
        setError('이 게시글을 수정할 권한이 없습니다.');
      }
    }
  }, [post, currentUser]);

  // 폼 제출
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!currentUser) {
      setError('로그인이 필요합니다.');
      return;
    }

    if (post && currentUser.id !== post.authorId && currentUser.role !== 'ADMIN') {
      setError('이 게시글을 수정할 권한이 없습니다.');
      return;
    }

    try {
      setLoading(true);
      
      const requestData = {
        ...formData,
        tags: formData.tags.filter(tag => tag.trim() !== '')
      };

      await apiCall(`/api/community/${id}`, {
        method: 'PUT',
        body: JSON.stringify(requestData)
      });

      // 수정 완료 후 상세 페이지로 이동
      navigate(`/community/${id}`);
    } catch (error) {
      console.error('게시글 수정 실패:', error);
      setError('게시글 수정에 실패했습니다.');
    } finally {
      setLoading(false);
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

  if (loading) {
    return (
      <Layout>
        <div className="w-full max-w-4xl mx-auto mt-24 p-6">
          <div className="text-center py-12">
            <div className="text-2xl text-gray-500">로딩 중...</div>
          </div>
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="w-full max-w-4xl mx-auto mt-24 p-6">
          <div className="text-center py-12">
            <div className="text-2xl text-red-500 mb-4">오류가 발생했습니다</div>
            <div className="text-gray-600 mb-4">{error}</div>
            <Button onClick={() => navigate('/community')}>
              커뮤니티로 돌아가기
            </Button>
          </div>
        </div>
      </Layout>
    );
  }

  if (!post) {
    return (
      <Layout>
        <div className="w-full max-w-4xl mx-auto mt-24 p-6">
          <div className="text-center py-12">
            <div className="text-2xl text-gray-500">게시글을 찾을 수 없습니다</div>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="w-full max-w-4xl mx-auto mt-24 p-6 bg-gradient-to-b from-blue-50 to-white min-h-screen">
        {/* 뒤로가기 버튼 */}
        <BackButton onClick={() => navigate(`/community/${id}`)} className="mb-6" />
        
        {/* 편집 폼 */}
        <Card className="bg-white shadow-lg">
          <div className="mb-6 pb-4 border-b-2 border-blue-100">
            <h1 className="text-3xl font-bold text-blue-600">게시글 수정</h1>
            <p className="text-gray-700 mt-2 text-lg">게시글 내용을 수정할 수 있습니다.</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 제목 */}
            <div>
              <label className="block text-sm font-semibold text-gray-800 mb-2">
                제목 *
              </label>
              <InputField
                type="text"
                value={formData.title}
                onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
                required
                placeholder="제목을 입력하세요"
                className="w-full"
              />
            </div>

            {/* 카테고리 */}
            <div>
              <label className="block text-sm font-semibold text-gray-800 mb-2">
                카테고리 *
              </label>
              <select
                value={formData.category}
                onChange={(e) => setFormData(prev => ({ ...prev, category: e.target.value }))}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
              >
                {categories.map(cat => (
                  <option key={cat.value} value={cat.value}>{cat.label}</option>
                ))}
              </select>
            </div>

            {/* 내용 */}
            <div>
              <label className="block text-sm font-semibold text-gray-800 mb-2">
                내용 *
              </label>
              <textarea
                value={formData.content}
                onChange={(e) => setFormData(prev => ({ ...prev, content: e.target.value }))}
                required
                rows={8}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="내용을 입력하세요"
              />
            </div>

            {/* 태그 */}
            <div>
              <label className="block text-sm font-semibold text-gray-800 mb-2">
                태그
              </label>
              <div className="flex gap-2 mb-3">
                <InputField
                  type="text"
                  value={tagInput}
                  onChange={(e) => setTagInput(e.target.value)}
                  placeholder="태그를 입력하세요"
                  onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addTag())}
                  className="flex-1"
                />
                <Button type="button" onClick={addTag} variant="outline">
                  추가
                </Button>
              </div>
              <div className="flex flex-wrap gap-2">
                {formData.tags.map(tag => (
                  <span 
                    key={tag} 
                    className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full flex items-center gap-1"
                  >
                    #{tag}
                    <button
                      type="button"
                      onClick={() => removeTag(tag)}
                      className="text-blue-600 hover:text-blue-800"
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
              <p className="text-sm text-gray-500 mt-2">
                태그는 Enter 키를 누르거나 추가 버튼을 클릭하여 추가할 수 있습니다.
              </p>
            </div>

            {/* 버튼 */}
            <div className="flex gap-3 justify-end pt-6 border-t-2 border-blue-100">
              <Button
                type="button"
                onClick={() => navigate(`/community/${id}`)}
                variant="outline"
              >
                취소
              </Button>
              <Button 
                type="submit" 
                disabled={loading}
                className="bg-blue-600 hover:bg-blue-700"
              >
                {loading ? '수정 중...' : '수정하기'}
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </Layout>
  );
}
