import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { apiCall } from '../api/config';
import { useUser } from '../api/auth';
import Button from '../components/Button';
import InputField from '../components/InputField';
import Card from '../components/Card';
import BackButton from '../components/BackButton';
import { categories, getCategoryDisplayName, getCategoryStyle } from '../utils/categoryUtils';

// 상수 정의
const COMMENTS_PAGE_SIZE = 100; // 댓글 한 번에 가져올 개수

export default function CommunityPostDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user: currentUser } = useUser();
  
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // 댓글 관련 상태
  const [commentContent, setCommentContent] = useState('');
  const [replyTo, setReplyTo] = useState(null);
  const [showCommentForm, setShowCommentForm] = useState(false);
  
  // 추천/비추천 상태
  const [userReaction, setUserReaction] = useState(null);
  const [likeCount, setLikeCount] = useState(0);
  const [dislikeCount, setDislikeCount] = useState(0);
  


  // 게시글 상세 조회
  const fetchPost = async (showLoading = true) => {
    if (showLoading) {
      setLoading(true);
    }
    setError('');
    
    try {
      const data = await apiCall(`/api/community/${id}`);
      
      if (data.success !== false) {
        setPost(data.data);
        setLikeCount(data.data.likeCount || 0);
        setDislikeCount(data.data.dislikeCount || 0);
        
        // 사용자의 반응 확인
        if (currentUser) {
          checkUserReaction();
        }
      } else {
        setError(data.error || '게시글을 불러오는데 실패했습니다.');
      }
    } catch (error) {
      console.error('게시글 조회 실패:', error);
      setError('게시글을 불러오는데 실패했습니다.');
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  };

  // 댓글 목록 조회
  const fetchComments = async () => {
    try {
      const params = new URLSearchParams({
        page: '0',
        size: COMMENTS_PAGE_SIZE.toString()
      });
      const response = await apiCall(`/api/community/${id}/comments?${params.toString()}`);
      
      if (response.success) {
        // 백엔드에서 페이징된 응답이 오므로 data.content에서 댓글 추출
        const commentsData = response.data?.content || [];
        
        // 댓글 계층 구조 생성 (parentId 기반)
        const topLevelComments = commentsData.filter(c => !c.parentId);
        const commentMap = {};
        
        // 모든 댓글을 맵에 저장
        commentsData.forEach(comment => {
          commentMap[comment.id] = { ...comment, replies: [] };
        });
        
        // 대댓글을 부모 댓글의 replies 배열에 추가
        commentsData.forEach(comment => {
          if (comment.parentId && commentMap[comment.parentId]) {
            commentMap[comment.parentId].replies.push(commentMap[comment.id]);
          }
        });
        
        // 최상위 댓글만 설정 (대댓글은 replies 속성에 포함됨)
        setComments(topLevelComments.map(c => commentMap[c.id]));
      }
    } catch (error) {
      console.error('댓글 조회 실패:', error);
    }
  };

  // 사용자 반응 확인
  const checkUserReaction = async () => {
    try {
      const data = await apiCall(`/api/community/${id}/reaction/check?userId=${currentUser.id}`);
      if (data.success) {
        setUserReaction(data.reaction);
      }
    } catch (error) {
      console.error('사용자 반응 확인 실패:', error);
    }
  };

  // 추천/비추천 토글
  const handleReaction = async (type) => {
    if (!currentUser) {
      setError('로그인이 필요합니다.');
      return;
    }

    try {
      const data = await apiCall(`/api/community/${id}/reaction`, {
        method: 'POST',
        body: JSON.stringify({
          userId: currentUser.id,
          reactionType: type
        })
      });

      if (data.success) {
        // 사용자 반응 상태 업데이트
        if (userReaction && userReaction.type === type) {
          setUserReaction(null); // 반응 제거
        } else {
          setUserReaction({ type: type }); // 새로운 반응
        }
        
        // 게시글 정보 새로고침 (반응 수 업데이트)
        fetchPost(false);
      }
    } catch (error) {
      console.error('반응 처리 실패:', error);
      setError('반응 처리에 실패했습니다.');
    }
  };

  // 댓글 작성
  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    
    if (!currentUser) {
      setError('로그인이 필요합니다.');
      return;
    }

    if (!commentContent.trim()) {
      setError('댓글 내용을 입력해주세요.');
      return;
    }

    try {
      console.log('🔍 현재 사용자 정보:', currentUser);
      console.log('🔍 사용자 ID 타입:', typeof currentUser.id);
      console.log('🔍 사용자 ID 값:', currentUser.id);
      
      const requestData = {
        content: commentContent,
        authorId: currentUser.id
      };

      if (replyTo) {
        requestData.parentId = replyTo.id;
      }

      console.log('🔍 댓글 작성 요청 데이터:', requestData);
      console.log('🔍 API 엔드포인트:', `/api/community/${id}/comments`);
      console.log('🔍 게시글 ID:', id);

      const response = await apiCall(`/api/community/${id}/comments`, {
        method: 'POST',
        body: JSON.stringify(requestData)
      });

      console.log('🔍 댓글 작성 응답:', response);

      if (response) {
        // 폼 초기화
        setCommentContent('');
        setReplyTo(null);
        setShowCommentForm(false);
        setError(''); // 성공 시 에러 메시지 초기화
        
        // 댓글 목록 새로고침
        fetchComments();
      } else {
        setError('댓글 작성에 실패했습니다. 서버에서 응답을 받지 못했습니다.');
      }
    } catch (error) {
      console.error('❌ 댓글 작성 실패:', error);
      console.error('❌ 오류 상세:', error.message);
      setError(`댓글 작성에 실패했습니다: ${error.message}`);
    }
  };

  // 댓글 삭제
  const handleCommentDelete = async (commentId) => {
    if (!window.confirm('정말로 이 댓글을 삭제하시겠습니까?')) {
      return;
    }

    try {
      await apiCall(`/api/community/comments/${commentId}?authorId=${currentUser.id}`, { 
        method: 'DELETE' 
      });
      fetchComments();
    } catch (error) {
      console.error('댓글 삭제 실패:', error);
      setError('댓글 삭제에 실패했습니다.');
    }
  };

  // 게시글 삭제
  const handlePostDelete = async () => {
    if (!window.confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
      return;
    }

    try {
      const result = await apiCall(`/api/community/${id}?authorId=${currentUser.id}`, { 
        method: 'DELETE' 
      });
      
      // 성공적으로 삭제된 경우 커뮤니티 페이지로 이동
      if (result && result.success) {
        navigate('/community');
      } else {
        // 응답이 없거나 성공 플래그가 없는 경우에도 삭제가 성공했을 수 있음
        navigate('/community');
      }
    } catch (error) {
      console.error('게시글 삭제 실패:', error);
      alert('게시글 삭제에 실패했습니다: ' + (error.message || '알 수 없는 오류'));
      // 에러 페이지로 이동하지 않고 현재 페이지에 머무름
    }
  };

  // 초기 로딩
  useEffect(() => {
    if (id) {
      fetchPost();
      fetchComments();
    }
  }, [id]);

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

  // 댓글 렌더링 (계층 구조) - 미니멀한 디자인
  const renderComment = (comment, level = 0) => (
    <div key={comment.id} className={`${level > 0 ? 'ml-8' : ''}`}>
      <div className="border border-gray-200 rounded bg-white p-4 mb-3">
        <div className="flex justify-between items-start mb-2">
          <div className="flex items-center gap-2 text-left">
            <div className="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center text-white text-xs font-bold">
              {(comment.authorEmail?.split('@')[0] || '익명').charAt(0).toUpperCase()}
            </div>
            <span className="text-sm font-medium text-black">{comment.authorEmail?.split('@')[0] || '익명'}</span>
            <span className="text-xs text-gray-500">{formatDate(comment.createdAt)}</span>
            {comment.parentId && (
              <span className="text-xs text-blue-600 bg-blue-50 px-2 py-1 rounded">
                답글
              </span>
            )}
          </div>
          
          {currentUser && (currentUser.id === comment.authorId || currentUser.role === 'ADMIN') && (
            <button
              onClick={() => handleCommentDelete(comment.id)}
              className="text-xs text-red-600 hover:underline"
            >
              삭제
            </button>
          )}
        </div>
        
        <p className="text-sm text-black mb-3 leading-relaxed text-left">{comment.content}</p>
        
        <div className="flex justify-between items-center">
          <div className="flex gap-4">
            {level < 2 && ( // 최대 2단계까지만 답글 허용
              <button
                onClick={() => {
                  setReplyTo(comment);
                  setShowCommentForm(true);
                }}
                className="text-xs text-blue-600 hover:underline"
              >
                답글
              </button>
            )}
            <span className="text-xs text-gray-500">신고 | 공감 확인</span>
          </div>
          <div className="flex gap-2">
            <button className="text-xs text-gray-500">👍0</button>
            <button className="text-xs text-gray-500">👎0</button>
          </div>
        </div>
      </div>
      
      {/* 대댓글 렌더링 */}
      {comment.replies && comment.replies.length > 0 && (
        <div className="ml-4">
          {comment.replies.map(reply => renderComment(reply, level + 1))}
        </div>
      )}
    </div>
  );

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

  // 게시글 로딩 실패 시에만 에러 페이지 표시 (삭제 실패는 별도 처리)
  if (!post && !loading) {
    return (
      <Layout>
        <div className="w-full max-w-4xl mx-auto mt-24 p-6">
          <div className="text-center py-12">
            <div className="text-2xl text-red-500 mb-4">오류가 발생했습니다</div>
            <div className="text-gray-600 mb-4">{error || '게시글을 불러올 수 없습니다.'}</div>
            <Button onClick={() => navigate('/community')}>
              커뮤니티로 돌아가기
            </Button>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="w-full max-w-4xl mx-auto mt-24 p-6 bg-white min-h-screen">
        {/* 뒤로가기 버튼 */}
        <BackButton onClick={() => navigate('/community')} className="mb-6" />
        
        {/* 게시글 내용 - 미니멀한 디자인 */}
        <div className="mb-8">
          {/* 제목 */}
          <h1 className="text-2xl font-bold text-black mb-4 leading-tight text-left">
            [{getCategoryDisplayName(post.category)}] {post.title}
          </h1>
          
          {/* 메타 정보 */}
          <div className="flex items-center gap-4 mb-6 text-sm text-black">
            <span className="font-medium">{post.authorName?.split('@')[0] || '익명'}</span>
            <span>|</span>
            <span>댓글: {comments.length} 개</span>
            <span>|</span>
            <span>조회: {post.viewCount || 0}</span>
            <span>|</span>
            <span>{formatDate(post.createdAt)}</span>
            
            {currentUser && (currentUser.id === post.authorId || currentUser.role === 'ADMIN') && (
              <div className="flex gap-2 ml-auto">
                <button
                  onClick={() => navigate(`/community/edit/${id}`)}
                  className="text-blue-600 hover:underline text-sm"
                >
                  수정
                </button>
                <button
                  onClick={handlePostDelete}
                  className="text-red-600 hover:underline text-sm"
                >
                  삭제
                </button>
              </div>
            )}
          </div>

          {/* 내용 */}
          <div className="mb-6">
            <div className="text-base text-black leading-relaxed whitespace-pre-line text-left">
              {post.content}
            </div>
          </div>

          {/* 태그 */}
          {post.tags && post.tags.length > 0 && (
            <div className="flex flex-wrap gap-2 mb-6 justify-start">
              {post.tags.map(tag => (
                <span 
                  key={tag} 
                  className="px-2 py-1 bg-gray-100 text-gray-700 text-sm rounded font-medium"
                >
                  #{tag}
                </span>
              ))}
            </div>
          )}

          {/* 추천/비추천 버튼 - 간단한 스타일 */}
          <div className="flex gap-4 justify-start py-4 border-t border-gray-200">
            <button
              onClick={() => handleReaction('LIKE')}
              className={`px-3 py-1 text-sm rounded ${
                userReaction?.type === 'LIKE' 
                  ? 'bg-blue-100 text-blue-700' 
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              👍 추천 {likeCount}
            </button>
            
            <button
              onClick={() => handleReaction('DISLIKE')}
              className={`px-3 py-1 text-sm rounded ${
                userReaction?.type === 'DISLIKE' 
                  ? 'bg-red-100 text-red-700' 
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              👎 비추천 {dislikeCount}
            </button>
          </div>
        </div>

        {/* 댓글 섹션 - 미니멀한 디자인 */}
        <div className="mb-8">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold text-black text-left">댓글 ({comments.length})</h2>
            <button
              onClick={() => setShowCommentForm(!showCommentForm)}
              className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
            >
              {showCommentForm ? '취소' : '댓글 작성'}
            </button>
          </div>

          {/* 댓글 작성 폼 */}
          {showCommentForm && (
            <div className="mb-6 p-4 bg-gray-50 rounded border border-gray-200">
              {replyTo && (
                <div className="mb-3 p-3 bg-blue-50 rounded border-l-4 border-blue-400">
                  <div className="text-sm text-blue-700">
                    <strong>{replyTo.authorEmail?.split('@')[0] || '익명'}</strong>님에게 답글 작성 중
                  </div>
                  <button
                    onClick={() => setReplyTo(null)}
                    className="mt-2 text-xs text-blue-600 hover:underline"
                  >
                    답글 취소
                  </button>
                </div>
              )}
              
              <form onSubmit={handleCommentSubmit} className="space-y-3">
                <textarea
                  value={commentContent}
                  onChange={(e) => setCommentContent(e.target.value)}
                  placeholder={replyTo ? "답글을 입력하세요..." : "댓글을 입력하세요..."}
                  rows={3}
                  className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                  required
                />
                
                <div className="flex gap-2 justify-start">
                  <button
                    type="button"
                    onClick={() => {
                      setShowCommentForm(false);
                      setReplyTo(null);
                      setCommentContent('');
                    }}
                    className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
                  >
                    취소
                  </button>
                  <button 
                    type="submit"
                    className="px-3 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600"
                  >
                    {replyTo ? '답글 작성' : '댓글 작성'}
                  </button>
                </div>
              </form>
            </div>
          )}

          {/* 댓글 목록 */}
          <div className="space-y-4">
            {comments.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <p className="text-lg">아직 댓글이 없습니다</p>
                <p className="text-sm">첫 번째 댓글을 작성해보세요!</p>
              </div>
            ) : (
              comments.map(comment => renderComment(comment))
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
}
