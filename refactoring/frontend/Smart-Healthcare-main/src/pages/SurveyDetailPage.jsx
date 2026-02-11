import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../components/Layout';
import Button from '../components/Button';
import { ArrowLeft, Edit, Trash2, Calendar, User, FileText } from 'lucide-react';
import { getSurveyById, deleteSurvey } from '../api/survey';
import { useUser } from '../api/auth';

export default function SurveyDetailPage() {
  const { isLoggedIn } = useUser();
  const navigate = useNavigate();
  const { id } = useParams();
  const [survey, setSurvey] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }

    if (id) {
      loadSurveyDetail();
    }
  }, [navigate, isLoggedIn, id]);

  const loadSurveyDetail = async () => {
    try {
      setLoading(true);
      setError('');
      
      const response = await getSurveyById(id);
      if (response && response.data) {
        setSurvey(response.data);
      } else {
        setError('설문조사를 찾을 수 없습니다.');
      }
    } catch (error) {
      console.error('설문 상세 조회 실패:', error);
      setError('설문조사 정보를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = () => {
    navigate('/survey', { state: { survey } });
  };

  const handleDelete = () => {
    setShowDeleteModal(true);
  };

  const confirmDelete = async () => {
    try {
      setDeleting(true);
      await deleteSurvey(survey.id);
      
      // 삭제 성공 후 이력 페이지로 이동
      navigate('/survey-history');
    } catch (error) {
      console.error('설문 삭제 실패:', error);
      setError('설문조사 삭제에 실패했습니다.');
    } finally {
      setDeleting(false);
      setShowDeleteModal(false);
    }
  };

  const cancelDelete = () => {
    setShowDeleteModal(false);
  };

  if (loading) {
    return (
      <Layout>
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
            <p className="text-gray-600">설문조사 정보를 불러오는 중...</p>
          </div>
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <div className="text-red-500 text-6xl mb-4">⚠️</div>
            <h2 className="text-2xl font-bold text-gray-800 mb-2">오류가 발생했습니다</h2>
            <p className="text-gray-600 mb-6">{error}</p>
            <div className="space-x-4">
              <Button onClick={() => navigate('/survey-history')} variant="outline">
                설문 이력으로 돌아가기
              </Button>
              <Button onClick={loadSurveyDetail}>
                다시 시도
              </Button>
            </div>
          </div>
        </div>
      </Layout>
    );
  }

  if (!survey) {
    return (
      <Layout>
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <div className="text-gray-400 text-6xl mb-4">📝</div>
            <h2 className="text-2xl font-bold text-gray-800 mb-2">설문조사를 찾을 수 없습니다</h2>
            <p className="text-gray-600 mb-6">요청하신 설문조사가 존재하지 않거나 삭제되었습니다.</p>
            <Button onClick={() => navigate('/survey-history')}>
              설문 이력으로 돌아가기
            </Button>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* 헤더 */}
          <div className="mb-8">
            <Button
              onClick={() => navigate('/survey-history')}
              variant="outline"
              className="mb-4 flex items-center gap-2"
            >
              <ArrowLeft className="w-4 h-4" />
              설문 이력으로 돌아가기
            </Button>
            
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-3xl font-bold text-gray-900 mb-2">설문조사 상세</h1>
                <p className="text-gray-600">작성한 설문조사의 상세 정보를 확인할 수 있습니다.</p>
              </div>
              
              <div className="flex gap-3">
                <Button
                  onClick={handleEdit}
                  variant="outline"
                  className="flex items-center gap-2"
                >
                  <Edit className="w-4 h-4" />
                  수정하기
                </Button>
                <Button
                  onClick={handleDelete}
                  variant="outline"
                  className="flex items-center gap-2 text-red-600 hover:text-red-700 border-red-300 hover:border-red-400"
                >
                  <Trash2 className="w-4 h-4" />
                  삭제하기
                </Button>
              </div>
            </div>
          </div>

          {/* 설문 정보 카드 */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
            {/* 메타 정보 */}
            <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="flex items-center gap-3">
                  <Calendar className="w-5 h-5 text-blue-600" />
                  <div>
                    <p className="text-sm text-gray-500">작성일</p>
                    <p className="font-semibold text-gray-900">
                      {new Date(survey.createdAt).toLocaleDateString('ko-KR', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </p>
                  </div>
                </div>
                
                <div className="flex items-center gap-3">
                  <User className="w-5 h-5 text-green-600" />
                  <div>
                    <p className="text-sm text-gray-500">사용자 ID</p>
                    <p className="font-semibold text-gray-900">{survey.userId}</p>
                  </div>
                </div>
                
                <div className="flex items-center gap-3">
                  <FileText className="w-5 h-5 text-purple-600" />
                  <div>
                    <p className="text-sm text-gray-500">설문 ID</p>
                    <p className="font-semibold text-gray-900">{survey.id}</p>
                  </div>
                </div>
              </div>
            </div>

            {/* 설문 내용 */}
            <div className="p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">설문 내용</h3>
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-gray-800 whitespace-pre-wrap leading-relaxed">
                  {survey.answerText || '설문 내용이 없습니다.'}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 삭제 확인 모달 */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
            <div className="text-center">
              <div className="text-red-500 text-4xl mb-4">⚠️</div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">설문조사 삭제</h3>
              <p className="text-gray-600 mb-6">
                이 설문조사를 삭제하시겠습니까?<br />
                삭제된 설문조사는 복구할 수 없습니다.
              </p>
              
              <div className="flex gap-3 justify-center">
                <Button
                  onClick={cancelDelete}
                  variant="outline"
                  disabled={deleting}
                >
                  취소
                </Button>
                <Button
                  onClick={confirmDelete}
                  className="bg-red-600 hover:bg-red-700 text-white"
                  disabled={deleting}
                >
                  {deleting ? '삭제 중...' : '삭제하기'}
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
