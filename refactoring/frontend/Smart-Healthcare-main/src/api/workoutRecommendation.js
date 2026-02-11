/**
 * 운동 추천 API 클라이언트
 * 백엔드 서비스와의 단순한 통신만 담당
 */

import { apiCall } from './config';
import { getCurrentUserId } from './auth';
import { mapInbodyDataToAIDto, validateInbodyData } from '../utils/dataMapper';

/**
 * 💪 AI 운동 추천 요청
 * 백엔드에서 생성 + 저장 + 반환을 모두 처리
 */
export async function fetchWorkoutRecommendations(inbody, survey = {}) {
  try {
    const userId = getCurrentUserId();
    console.log('🔍 운동 추천 요청: userId=', userId);
    
    // 데이터 유효성 검사
    validateInbodyData(inbody);
    
    // 백엔드 DTO 형식으로 변환
    const requestData = mapInbodyDataToAIDto(inbody, userId, survey);
    
    console.log('🔍 API 호출: /api/workout-recommendation');
    const data = await apiCall('/api/workout-recommendation', {
      method: 'POST',
      body: JSON.stringify(requestData),
    });
    
    console.log('✅ 운동 추천 완료 (자동 저장됨):', data);
    return data;
    
  } catch (error) {
    console.error('❌ 운동 추천 요청 실패:', error);
    throw error;
  }
}
