/**
 * OpenAI ChatGPT API 기반 체형 분석 시스템
 * 
 * 이 파일은 체형 분석과 건강 상태 요약에만 집중합니다.
 * 운동/식단 추천은 recommendation.js에서 처리합니다.
 */
import { apiCall } from './config';
import { getCurrentUserId, getCurrentToken } from './auth';
import { mapInbodyDataToAIDto, validateInbodyData } from '../utils/dataMapper';

/**
 * 🏥 체형 분석 - OpenAI ChatGPT API 전용 (HTTP 상태코드 기반)
 */
export async function fetchBodyTypeAnalysis(inbody) {
  try {
    // 실제 로그인된 사용자 ID 사용
    const userId = getCurrentUserId();
    
    // 데이터 유효성 검사
    validateInbodyData(inbody);
    
    // 프론트엔드 데이터를 AI DTO 형식으로 변환
    const requestData = mapInbodyDataToAIDto(inbody, userId);
    
    console.log('🔐 원본 프론트엔드 데이터:', inbody);
    console.log('🔐 변환된 백엔드 요청 데이터:', requestData);
    
    // JWT 토큰 확인
    const token = getCurrentToken();
    console.log('🔐 JWT 토큰:', token ? '존재함' : '없음');
    console.log('🔐 사용자 ID:', userId);
    
    const response = await apiCall('/api/body-analysis', {
      method: 'POST',
      body: JSON.stringify(requestData),
    });
    
    // 백엔드에서 받은 분석 데이터를 그대로 반환 (불필요한 변환 제거)
    const analysisData = response.data;
    
    // 분석 방법에 따른 사용자 친화적 메시지 생성
    const getAnalysisMessage = (analysisMethod) => {
      switch (analysisMethod) {
        case 'AI':
          return {
            method: 'AI 분석',
            message: '🤖 AI가 분석한 결과입니다.',
            accuracy: '높음 (AI 기반)',
            icon: '🤖'
          };
        case 'AI_FAILED_FALLBACK':
          return {
            method: 'AI 분석 실패 → 룰기반',
            message: '⚠️ AI 분석에 실패하여 룰기반으로 분석했습니다.',
            accuracy: '보통 (규칙 기반)',
            icon: '⚠️'
          };
        case 'RULE':
        default:
          return {
            method: '룰기반 분석',
            message: '📋 규칙 기반으로 분석한 결과입니다.',
            accuracy: '보통 (규칙 기반)',
            icon: '📋'
          };
      }
    };

    const analysisInfo = getAnalysisMessage(analysisData.analysisMethod);
    
    return {
      ...analysisData, // 백엔드에서 받은 모든 분석 데이터
      method: analysisInfo.method,
      message: analysisInfo.message,
      accuracy: analysisInfo.accuracy,
      icon: analysisInfo.icon,
      analyzedAt: new Date().toISOString(),
      timestamp: response.timestamp || new Date().toISOString()
    };
  } catch (error) {
    console.error('OpenAI 체형 분석 API 호출 실패:', error);
    throw error;
  }
}




/**
 * ❤️ 건강 상태 요약 (간단 버전) - HTTP 상태코드 기반
 */
export async function fetchHealthSummary(inbody) {
  try {
    const response = await apiCall('/api/health-summary', {
      method: 'POST',
      body: JSON.stringify(inbody),
    });
    
    // 백엔드에서 받은 요약 데이터를 그대로 반환
    return {
      ...response.data, // 백엔드에서 받은 모든 요약 데이터
      timestamp: response.timestamp || new Date().toISOString()
    };
  } catch (error) {
    console.error('건강 상태 요약 API 호출 실패:', error);
    throw error;
  }
}

/**
 * 📊 OpenAI 분석 서비스 상태 확인 (HTTP 상태코드 기반)
 */
export async function fetchAnalysisStatus() {
  try {
    const responseData = await apiCall('/api/analysis-status', {
      method: 'GET'
    });
    
    // 백엔드에서 받은 상태 데이터를 그대로 반환
    return {
      ...responseData.data, // 백엔드에서 받은 모든 상태 데이터
      timestamp: responseData.timestamp || new Date().toISOString()
    };
  } catch (error) {
    console.error('OpenAI 분석 상태 확인 실패:', error);
    throw error;
  }
}

/**
 * 사용자별 체형분석 히스토리 조회
 */
export async function getBodyAnalysisHistory(userId, options = {}) {
  try {
    const { limit, startDate, endDate, page = 0, size = 10 } = options;
    
    let endpoint = `/api/users/history/body-analysis/${userId}`;
    const params = new URLSearchParams();
    
    if (page !== undefined) params.append('page', page);
    if (size !== undefined) params.append('size', size);
    
    if (params.toString()) {
      endpoint += `?${params.toString()}`;
    }
    
    const response = await apiCall(endpoint, {
      method: 'GET'
    });
    
    console.log('✅ 체형분석 히스토리 조회 성공:', response.data);
    return response.data;
  } catch (error) {
    console.error('❌ 체형분석 히스토리 조회 실패:', error);
    throw error;
  }
}

/**
 * 사용자별 최신 체형 분석 결과 조회
 */
export async function getLatestBodyAnalysis(userId) {
  try {
    console.log('🔍 최신 체형 분석 조회 시작: userId=', userId);
    
    const response = await apiCall(`/api/body-analysis/${userId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    console.log('🔍 최신 체형 분석 조회 응답 전체:', response);
    console.log('🔍 response.success:', response?.success);
    console.log('🔍 response.data:', response?.data);
    console.log('🔍 response.data.label:', response?.data?.label);
    
    if (response && response.success) {
      if (response.data) {
        console.log('✅ 체형 분석 데이터 반환:', response.data);
        return response.data;
      } else {
        console.log('📝 체형 분석 결과가 없음 (success=true, data=null) - response:', response);
        return null;
      }
    } else {
      console.log('📝 체형 분석 결과가 없음 (success=false) - response:', response);
      return null;
    }
  } catch (error) {
    console.error('❌ 최신 체형 분석 조회 실패:', error);
    console.error('❌ 에러 상세:', error.message);
    return null; // 에러 시 null 반환 (데이터 없음으로 처리)
  }
}

// 레거시 지원을 위한 호환성 함수들
export const fetchBodyTypeAnalysisWithOpenAI = fetchBodyTypeAnalysis;