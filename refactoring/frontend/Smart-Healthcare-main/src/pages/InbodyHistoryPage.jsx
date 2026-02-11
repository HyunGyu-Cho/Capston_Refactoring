import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import Button from '../components/Button';
import { Calendar, TrendingUp, Trash2, Eye, Plus } from 'lucide-react';
import HeroWithBg from '../components/HeroWithBg';
import SectionWithWave from '../components/SectionWithWave';
import { getInbodyRecords, deleteInbodyRecord } from '../api/inbody';
import { getCurrentUserId, useUser } from '../api/auth';
import { getBodyTypeInfo } from '../utils/bodyTypeUtils';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ResponsiveContainer,
} from 'recharts';

// 영문 필드명을 한글 필드명으로 변환하는 매핑
const FIELD_MAPPING = {
  'gender': '성별',
  'birthYear': '사용자 출생년도',
  'weight': '체중',
  'totalBodyWater': '총체수분',
  'protein': '단백질',
  'mineral': '무기질',
  'bodyFatMass': '체지방량',
  'muscleMass': '근육량',
  'fatFreeMass': '제지방량',
  'skeletalMuscleMass': '골격근량',
  'bodyFatPercentage': '체지방률',
  'rightArmMuscleMass': '오른팔 근육량',
  'leftArmMuscleMass': '왼팔 근육량',
  'trunkMuscleMass': '몸통 근육량',
  'rightLegMuscleMass': '오른다리 근육량',
  'leftLegMuscleMass': '왼다리 근육량',
  'rightArmFatMass': '오른팔 체지방량',
  'leftArmFatMass': '왼팔 체지방량',
  'trunkFatMass': '몸통 체지방량',
  'rightLegFatMass': '오른다리 체지방량',
  'leftLegFatMass': '왼다리 체지방량',
  'inbodyScore': '인바디점수',
  'idealWeight': '적정체중',
  'weightControl': '체중조절',
  'fatControl': '지방조절',
  'muscleControl': '근육조절',
  'basalMetabolism': '기초대사량',
  'abdominalFatPercentage': '복부지방률',
  'visceralFatLevel': '내장지방레벨',
  'obesityDegree': '비만도',
  'bmi': '체질량지수',
  'boneMineralContent': '골무기질량',
  'waistCircumference': '복부둘레'
};

// 영문 데이터를 한글로 변환하는 함수
const convertEnglishToKorean = (englishData) => {
  const koreanData = {};
  
  Object.keys(englishData).forEach(key => {
    const koreanKey = FIELD_MAPPING[key];
    if (koreanKey) {
      let value = englishData[key];
      
      // 성별 변환
      if (key === 'gender') {
        value = value === 'MALE' ? '남성' : '여성';
      }
      
      koreanData[koreanKey] = value;
    } else {
      // 매핑되지 않은 필드는 그대로 유지 (id, createdAt 등)
      koreanData[key] = englishData[key];
    }
  });
  
  return koreanData;
};

// 인바디 변화 추이 라인 차트 컴포넌트
function InbodyTrendCharts({ records }) {
  if (!records || records.length === 0) return null;

  // 날짜 오름차순 정렬
  const sorted = [...records].sort(
    (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
  );

  const data = sorted.map((r, idx) => ({
    index: idx + 1,
    date: new Date(r.createdAt).toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
    }),
    weight: r['체중'] ?? null,
    bmi: r['체질량지수'] ?? null,
    bodyFatPercentage: r['체지방률'] ?? null,
    muscleMass: r['근육량'] ?? null,
    inbodyScore: r['인바디점수'] ?? null,
  }));

  const MetricChart = ({ dataKey, color, title, unit }) => (
    <div className="h-52">
      <h4 className="text-sm font-semibold text-gray-700 mb-2 flex items-center gap-2">
        <span className="inline-block w-2 h-2 rounded-full" style={{ backgroundColor: color }}></span>
        {title}
      </h4>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 10, right: 20, left: -20, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" vertical={false} />
          <XAxis dataKey="date" tick={{ fontSize: 11 }} />
          <YAxis tick={{ fontSize: 11 }} width={40} />
          <Tooltip
            formatter={(value) => (value != null ? `${value}${unit}` : '-')}
            labelFormatter={(label, payload) => {
              if (!payload || payload.length === 0) return label;
              const idx = payload[0].payload.index;
              return `#${idx}회 측정 (${label})`;
            }}
          />
          <Line
            type="monotone"
            dataKey={dataKey}
            stroke={color}
            strokeWidth={2}
            dot={{ r: 3 }}
            activeDot={{ r: 5 }}
            connectNulls
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );

  return (
    <div className="mb-10 bg-white rounded-2xl shadow-lg border border-gray-100 p-6">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h3 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-blue-500" />
            인바디 변화 추이
          </h3>
          <p className="text-sm text-gray-500 mt-1">
            최근 {records.length}회 측정 기준으로 주요 지표 5가지를 시각화했습니다.
          </p>
        </div>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <MetricChart dataKey="weight" color="#3b82f6" title="체중 (kg)" unit="kg" />
        <MetricChart dataKey="bmi" color="#10b981" title="BMI" unit="" />
        <MetricChart dataKey="bodyFatPercentage" color="#f97316" title="체지방률 (%)" unit="%" />
        <MetricChart dataKey="muscleMass" color="#8b5cf6" title="근육량 (kg)" unit="kg" />
        <MetricChart dataKey="inbodyScore" color="#ec4899" title="인바디 점수" unit="점" />
      </div>
    </div>
  );
}

export default function InbodyHistoryPage() {
  const { user: currentUser, isLoggedIn } = useUser();
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const navigate = useNavigate();
  const [user, setUser] = useState(null);

  useEffect(() => {
    // 로그인 체크
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }

    // 사용자 정보 로드
    if (currentUser) {
      setUser(currentUser);
    }

    // 인바디 기록 조회
    loadInbodyRecords();
  }, [navigate, isLoggedIn, currentUser]);

  const loadInbodyRecords = async () => {
    try {
      setLoading(true);
      setError('');
      
      // 실제 로그인된 사용자 ID 사용
      const userId = getCurrentUserId();
      if (!userId) {
        setError('로그인이 필요합니다.');
        return;
      }
      
      const response = await getInbodyRecords(userId, { limit: 50 });
      console.log('📊 인바디 기록 조회 결과:', response);
      console.log('📊 응답 타입:', typeof response);
      console.log('📊 응답 구조:', Object.keys(response || {}));
      
      // ApiResponseDto 형식에서 데이터 추출
      let recordsData = null;
      
      if (response && response.success && response.data) {
        // Page 객체에서 content 배열 추출
        if (response.data.content && Array.isArray(response.data.content)) {
          recordsData = response.data.content;
        } else if (Array.isArray(response.data)) {
          recordsData = response.data;
        } else {
          // 단일 객체인 경우 배열로 변환
          recordsData = [response.data];
        }
      }
      
      console.log('📊 추출된 기록 데이터:', recordsData);
      console.log('📊 기록 개수:', recordsData ? recordsData.length : 0);
      
      if (recordsData && Array.isArray(recordsData)) {
        // 영문 필드명을 한글 필드명으로 변환
        const convertedRecords = recordsData.map(record => convertEnglishToKorean(record));
        console.log('🔄 변환된 기록 데이터:', convertedRecords);
        
        setRecords(convertedRecords);
        if (convertedRecords.length === 0) {
          setError('저장된 인바디 기록이 없습니다.');
        }
      } else {
        console.warn('⚠️ 기록 데이터를 추출할 수 없습니다:', response);
        setRecords([]);
        setError('인바디 기록을 불러올 수 없습니다.');
      }
    } catch (err) {
      console.error('❌ 인바디 기록 조회 실패:', err);
      setError('인바디 기록을 불러오는 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteRecord = async (recordId) => {
    try {
      setDeletingId(recordId);
      
      const userId = getCurrentUserId();
      const response = await deleteInbodyRecord(recordId, userId);
      console.log('🗑️ 인바디 기록 삭제 결과:', response);
      
      // 목록에서 삭제된 기록 제거
      setRecords(prev => prev.filter(record => record.id !== recordId));
      setShowDeleteModal(false);
      setSelectedRecord(null);
    } catch (err) {
      console.error('❌ 인바디 기록 삭제 실패:', err);
      setError('인바디 기록 삭제 중 오류가 발생했습니다.');
    } finally {
      setDeletingId(null);
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

  const getBMIStatus = (record) => {
    const bmi = record['체질량지수'];
    if (!bmi) return { status: '미측정', color: 'text-gray-500' };
    if (bmi < 18.5) return { status: '저체중', color: 'text-blue-600' };
    if (bmi < 23) return { status: '정상', color: 'text-green-600' };
    if (bmi < 25) return { status: '과체중', color: 'text-yellow-600' };
    if (bmi < 30) return { status: '비만', color: 'text-orange-600' };
    return { status: '고도비만', color: 'text-red-600' };
  };

  const getBodyFatStatus = (record) => {
    const bodyFatPercentage = record['체지방률'];
    if (!bodyFatPercentage) return { status: '미측정', color: 'text-gray-500' };
    
    // 성별에 따른 기준 (대략적)
    const isMale = record['성별'] === '남성';
    const low = isMale ? 10 : 16;
    const normal = isMale ? 20 : 25;
    const high = isMale ? 25 : 30;
    
    if (bodyFatPercentage < low) return { status: '낮음', color: 'text-blue-600' };
    if (bodyFatPercentage < normal) return { status: '정상', color: 'text-green-600' };
    if (bodyFatPercentage < high) return { status: '높음', color: 'text-yellow-600' };
    return { status: '매우높음', color: 'text-red-600' };
  };

  if (loading) {
    return (
      <Layout>
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-gray-600">인바디 기록을 불러오는 중...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <HeroWithBg
        title="인바디 기록 히스토리"
        subtitle={"나의 건강 변화를\n한눈에 확인해보세요"}
        bgImage="/assets/inbody-bg.jpg"
      />
      
      <SectionWithWave bgColor="bg-white">
        <div className="max-w-6xl mx-auto">
          {/* 헤더 */}
          <div className="flex justify-between items-center mb-8">
            <div>
              <h2 className="text-3xl font-bold text-gray-800 mb-2">
                총 {records.length}개의 기록
              </h2>
              <p className="text-gray-600">
                인바디 측정 기록과 건강 변화를 추적해보세요
              </p>
            </div>
            <Button
              onClick={() => navigate('/inbody-input')}
              className="bg-primary text-white px-6 py-3 rounded-full flex items-center gap-2"
            >
              <Plus className="w-5 h-5" />
              새 기록 추가
            </Button>
          </div>

          {/* 인바디 변화 추이 차트 */}
          {records.length > 1 && (
            <InbodyTrendCharts records={records} />
          )}

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
              <p className="text-red-700">{error}</p>
            </div>
          )}

          {/* 기록 목록 */}
          {records.length === 0 ? (
            <div className="text-center py-12">
              <Calendar className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 className="text-xl font-semibold text-gray-600 mb-2">
                아직 인바디 기록이 없습니다
              </h3>
              <p className="text-gray-500 mb-6">
                첫 번째 인바디 측정을 시작해보세요
              </p>
              <Button
                onClick={() => navigate('/inbody-input')}
                className="bg-primary text-white px-6 py-3 rounded-full"
              >
                인바디 측정하기
              </Button>
            </div>
          ) : (
            <div className="grid gap-4">
              {records.map((record) => {
                const bmiStatus = getBMIStatus(record);
                const bodyFatStatus = getBodyFatStatus(record);
                
                return (
                  <div
                    key={record.id}
                    className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow"
                  >
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <span className="text-sm text-gray-500">
                            {formatDate(record.createdAt)}
                          </span>
                          <span className="text-xs text-gray-400">ID: {record.id}</span>
                        </div>
                        <h3 className="font-medium text-gray-900 mb-2">인바디 측정 기록</h3>
                        <div className="grid grid-cols-2 gap-2 text-sm">
                          <div className="flex justify-between">
                            <span className="text-gray-600">체중</span>
                            <span className="font-medium">{record['체중']}kg</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-600">BMI</span>
                            <span className="font-medium">{record['체질량지수']?.toFixed(1)}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-600">체지방률</span>
                            <span className="font-medium">{record['체지방률']?.toFixed(1)}%</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-600">근육량</span>
                            <span className="font-medium">{record['근육량']?.toFixed(1)}kg</span>
                          </div>
                        </div>
                      </div>
                      
                      <div className="flex flex-col gap-2 ml-4">
                        <button
                          onClick={() => setSelectedRecord(record)}
                          className="flex items-center gap-1 px-3 py-1 text-sm text-blue-600 hover:text-blue-700 border border-blue-200 rounded-md hover:bg-blue-50"
                        >
                          <Eye className="w-4 h-4" />
                          상세보기
                        </button>
                        <button
                          onClick={() => {
                            setSelectedRecord(record);
                            setShowDeleteModal(true);
                          }}
                          className="flex items-center gap-1 px-3 py-1 text-sm text-red-600 hover:text-red-700 border border-red-200 rounded-md hover:bg-red-50"
                        >
                          <Trash2 className="w-4 h-4" />
                          삭제
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </SectionWithWave>

      {/* 상세보기 모달 */}
      {selectedRecord && !showDeleteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl max-w-6xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-center mb-6">
                <h3 className="text-2xl font-bold">인바디 상세 기록</h3>
                <button
                  onClick={() => setSelectedRecord(null)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  ✕
                </button>
              </div>
              
              <div className="grid grid-cols-3 gap-4">
                {/* 기본 정보 */}
                <div>
                  <label className="text-gray-600">측정일시</label>
                  <p className="font-semibold">{formatDate(selectedRecord.createdAt)}</p>
                </div>
                <div>
                  <label className="text-gray-600">성별</label>
                  <p className="font-semibold">{selectedRecord['성별']}</p>
                </div>
                <div>
                  <label className="text-gray-600">사용자 출생년도</label>
                  <p className="font-semibold">{selectedRecord['사용자 출생년도'] || '-'}년</p>
                </div>
                
                {/* 체성분 기본 정보 */}
                <div>
                  <label className="text-gray-600">체중</label>
                  <p className="font-semibold">{selectedRecord['체중']}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">BMI</label>
                  <p className="font-semibold">{selectedRecord['체질량지수']?.toFixed(1)}</p>
                </div>
                <div>
                  <label className="text-gray-600">체지방률</label>
                  <p className="font-semibold">{selectedRecord['체지방률']?.toFixed(1)}%</p>
                </div>
                <div>
                  <label className="text-gray-600">근육량</label>
                  <p className="font-semibold">{selectedRecord['근육량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">골격근량</label>
                  <p className="font-semibold">{selectedRecord['골격근량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">제지방량</label>
                  <p className="font-semibold">{selectedRecord['제지방량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">체지방량</label>
                  <p className="font-semibold">{selectedRecord['체지방량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">총체수분</label>
                  <p className="font-semibold">{selectedRecord['총체수분']?.toFixed(1)}L</p>
                </div>
                <div>
                  <label className="text-gray-600">단백질</label>
                  <p className="font-semibold">{selectedRecord['단백질']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">무기질</label>
                  <p className="font-semibold">{selectedRecord['무기질']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">골무기질량</label>
                  <p className="font-semibold">{selectedRecord['골무기질량']?.toFixed(1)}kg</p>
                </div>
                
                {/* 부위별 근육량 */}
                <div>
                  <label className="text-gray-600">오른팔 근육량</label>
                  <p className="font-semibold">{selectedRecord['오른팔 근육량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">왼팔 근육량</label>
                  <p className="font-semibold">{selectedRecord['왼팔 근육량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">몸통 근육량</label>
                  <p className="font-semibold">{selectedRecord['몸통 근육량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">오른다리 근육량</label>
                  <p className="font-semibold">{selectedRecord['오른다리 근육량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">왼다리 근육량</label>
                  <p className="font-semibold">{selectedRecord['왼다리 근육량']?.toFixed(1)}kg</p>
                </div>
                
                {/* 부위별 체지방량 */}
                <div>
                  <label className="text-gray-600">오른팔 체지방량</label>
                  <p className="font-semibold">{selectedRecord['오른팔 체지방량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">왼팔 체지방량</label>
                  <p className="font-semibold">{selectedRecord['왼팔 체지방량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">몸통 체지방량</label>
                  <p className="font-semibold">{selectedRecord['몸통 체지방량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">오른다리 체지방량</label>
                  <p className="font-semibold">{selectedRecord['오른다리 체지방량']?.toFixed(1)}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">왼다리 체지방량</label>
                  <p className="font-semibold">{selectedRecord['왼다리 체지방량']?.toFixed(1)}kg</p>
                </div>
                
                {/* 추가 지표 */}
                <div>
                  <label className="text-gray-600">인바디 점수</label>
                  <p className="font-semibold text-primary">{selectedRecord['인바디점수'] || '-'}점</p>
                </div>
                <div>
                  <label className="text-gray-600">기초대사량</label>
                  <p className="font-semibold">{selectedRecord['기초대사량'] || '-'}kcal</p>
                </div>
                <div>
                  <label className="text-gray-600">적정체중</label>
                  <p className="font-semibold">{selectedRecord['적정체중']?.toFixed(1) || '-'}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">체중조절</label>
                  <p className="font-semibold">{selectedRecord['체중조절']?.toFixed(1) || '-'}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">지방조절</label>
                  <p className="font-semibold">{selectedRecord['지방조절']?.toFixed(1) || '-'}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">근육조절</label>
                  <p className="font-semibold">{selectedRecord['근육조절']?.toFixed(1) || '-'}kg</p>
                </div>
                <div>
                  <label className="text-gray-600">비만도</label>
                  <p className="font-semibold">{selectedRecord['비만도']?.toFixed(1) || '-'}%</p>
                </div>
                <div>
                  <label className="text-gray-600">내장지방레벨</label>
                  <p className="font-semibold">{selectedRecord['내장지방레벨']?.toFixed(1) || '-'}레벨</p>
                </div>
                <div>
                  <label className="text-gray-600">복부지방률</label>
                  <p className="font-semibold">{selectedRecord['복부지방률']?.toFixed(1) || '-'}%</p>
                </div>
                <div>
                  <label className="text-gray-600">복부둘레</label>
                  <p className="font-semibold">{selectedRecord['복부둘레']?.toFixed(1) || '-'}cm</p>
                </div>
              </div>
              
              <div className="mt-6 flex gap-3">
                <Button
                  onClick={() => setSelectedRecord(null)}
                  className="flex-1 bg-gray-500 text-white"
                >
                  닫기
                </Button>
                <Button
                  onClick={() => {
                    setShowDeleteModal(true);
                  }}
                  className="flex-1 bg-red-500 text-white"
                >
                  삭제
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 삭제 확인 모달 */}
      {showDeleteModal && selectedRecord && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl max-w-md w-full p-6">
            <h3 className="text-xl font-bold mb-4">기록 삭제</h3>
            <p className="text-gray-600 mb-6">
              {formatDate(selectedRecord.createdAt)}의 인바디 기록을 삭제하시겠습니까?
              <br />
              <span className="text-sm text-red-500">이 작업은 되돌릴 수 없습니다.</span>
            </p>
            
            <div className="flex gap-3">
              <Button
                onClick={() => {
                  setShowDeleteModal(false);
                  setSelectedRecord(null);
                }}
                className="flex-1 bg-gray-500 text-white"
                disabled={deletingId === selectedRecord.id}
              >
                취소
              </Button>
              <Button
                onClick={() => handleDeleteRecord(selectedRecord.id)}
                className="flex-1 bg-red-500 text-white"
                disabled={deletingId === selectedRecord.id}
              >
                {deletingId === selectedRecord.id ? '삭제 중...' : '삭제'}
              </Button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
