import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import Button from '../components/Button';
import { Clock, Eye, Edit, Trash2, Plus } from 'lucide-react';
import HeroWithBg from '../components/HeroWithBg';
import SectionWithWave from '../components/SectionWithWave';
import { getSurveyHistoryByUserId, getSurveyById, deleteSurvey } from '../api/survey';
import { getCurrentUserId, useUser } from '../api/auth';

export default function SurveyHistoryPage() {
  const { isLoggedIn } = useUser();
  const [surveys, setSurveys] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedSurvey, setSelectedSurvey] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }

    loadSurveyHistory();
  }, [navigate, isLoggedIn]);

  const loadSurveyHistory = async () => {
    try {
      setLoading(true);
      setError('');
      const userId = getCurrentUserId();
      if (!userId) {
        setError('로그인이 필요합니다.');
        return;
      }

      const response = await getSurveyHistoryByUserId(userId);
      
      // 페이징된 응답인 경우 content 배열 추출
      if (response && response.data && response.data.content) {
        setSurveys(response.data.content);
      } else if (Array.isArray(response.data)) {
        setSurveys(response.data);
      } else if (Array.isArray(response)) {
        setSurveys(response);
      } else {
        setSurveys([]);
      }
    } catch (error) {
      console.error('설문 이력 로드 실패:', error);
      setError('설문 이력을 불러오는데 실패했습니다.');
      setSurveys([]);
    } finally {
      setLoading(false);
    }
  };

  const handleViewSurvey = (survey) => {
    navigate(`/survey-detail/${survey.id}`);
  };

  const handleEditSurvey = (survey) => {
    // 설문 수정 페이지로 이동 (아직 구현되지 않음)
    navigate('/survey', { state: { survey } });
  };

  const handleDeleteSurvey = (surveyId) => {
    setDeletingId(surveyId);
    setShowDeleteModal(true);
  };

  const confirmDelete = async () => {
    try {
      const userId = getCurrentUserId();
      await deleteSurvey(deletingId, userId);
      
      // 로컬 상태에서 제거
      setSurveys(prev => Array.isArray(prev) ? prev.filter(survey => survey.id !== deletingId) : []);
      setShowDeleteModal(false);
      setDeletingId(null);
    } catch (error) {
      console.error('설문 삭제 실패:', error);
      setError('설문 삭제에 실패했습니다.');
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const truncateText = (text, maxLength = 100) => {
    if (!text) return '내용 없음';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  };

  if (loading) {
    return (
      <Layout>
        <div className="text-center py-16">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-xl text-gray-600">설문 이력을 불러오는 중...</p>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <HeroWithBg
        title="설문 이력"
        subtitle="과거에 작성한 설문조사들을 확인하고 관리하세요"
        bgClass="bg-gradient-to-r from-purple-600 to-blue-600"
      />

      <SectionWithWave>
        <div className="max-w-6xl mx-auto">
          {/* 헤더 */}
          <div className="flex justify-between items-center mb-8">
            <div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">설문 이력</h2>
              <p className="text-gray-600">총 {Array.isArray(surveys) ? surveys.length : 0}개의 설문이 있습니다.</p>
            </div>
            <Button
              onClick={() => navigate('/survey')}
              className="flex items-center gap-2"
            >
              <Plus className="w-4 h-4" />
              새 설문 작성
            </Button>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
              {error}
            </div>
          )}

          {/* 설문 목록 */}
          {!Array.isArray(surveys) || surveys.length === 0 ? (
            <div className="text-center py-16">
              <Clock className="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h3 className="text-xl font-semibold text-gray-900 mb-2">설문 이력이 없습니다</h3>
              <p className="text-gray-600 mb-6">아직 작성한 설문이 없습니다. 첫 번째 설문을 작성해보세요!</p>
              <Button onClick={() => navigate('/survey')}>
                설문 작성하기
              </Button>
            </div>
          ) : (
            <div className="grid gap-6">
              {Array.isArray(surveys) && surveys.map((survey) => (
                <div key={survey.id} className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow">
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-2">
                        <span className="text-sm text-gray-500">
                          {formatDate(survey.createdAt)}
                        </span>
                        <span className="text-xs text-gray-400">ID: {survey.id}</span>
                      </div>
                      <h3 className="font-medium text-gray-900 mb-2">설문조사</h3>
                      <p className="text-sm text-gray-600 line-clamp-2">
                        {survey.answerText ? truncateText(survey.answerText, 100) : '설문 내용이 없습니다.'}
                      </p>
                    </div>
                    
                    <div className="flex flex-col gap-2 ml-4">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleViewSurvey(survey)}
                        className="flex items-center gap-1 text-blue-600 hover:text-blue-700"
                      >
                        <Eye className="w-4 h-4" />
                        상세보기
                      </Button>
                      <div className="flex gap-1">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleEditSurvey(survey)}
                          className="flex items-center gap-1 text-green-600 hover:text-green-700"
                        >
                          <Edit className="w-3 h-3" />
                          수정
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleDeleteSurvey(survey.id)}
                          className="flex items-center gap-1 text-red-600 hover:text-red-700"
                        >
                          <Trash2 className="w-3 h-3" />
                          삭제
                        </Button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </SectionWithWave>

      {/* 설문 상세보기 모달 */}
      {selectedSurvey && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[80vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-xl font-bold text-gray-900">설문 상세보기</h3>
                <button
                  onClick={() => setSelectedSurvey(null)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  ✕
                </button>
              </div>
              
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    작성일시
                  </label>
                  <p className="text-gray-900">{formatDate(selectedSurvey.createdAt)}</p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    설문 내용
                  </label>
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <p className="text-gray-800 whitespace-pre-wrap">
                      {selectedSurvey.answerText || '내용 없음'}
                    </p>
                  </div>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    완료 상태
                  </label>
                  <span className={`inline-block px-3 py-1 rounded-full text-sm ${
                    selectedSurvey.isCompleted 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-yellow-100 text-yellow-800'
                  }`}>
                    {selectedSurvey.isCompleted ? '완료' : '미완료'}
                  </span>
                </div>
              </div>
              
              <div className="flex gap-2 mt-6">
                <Button
                  variant="outline"
                  onClick={() => handleEditSurvey(selectedSurvey)}
                  className="flex-1"
                >
                  수정하기
                </Button>
                <Button
                  onClick={() => setSelectedSurvey(null)}
                  className="flex-1"
                >
                  닫기
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 삭제 확인 모달 */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-4">설문 삭제</h3>
            <p className="text-gray-600 mb-6">
              정말로 이 설문을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                onClick={() => {
                  setShowDeleteModal(false);
                  setDeletingId(null);
                }}
                className="flex-1"
              >
                취소
              </Button>
              <Button
                onClick={confirmDelete}
                className="flex-1 bg-red-600 hover:bg-red-700"
              >
                삭제
              </Button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
