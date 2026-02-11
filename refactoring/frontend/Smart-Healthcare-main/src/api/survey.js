// src/api/survey.js

import { apiCall } from './config';
import { getCurrentUserId } from './auth';

/**
 * 설문조사 저장 API
 */
export async function saveSurvey(surveyText, inbodyRecordId = null, surveyData = null) {
  try {
    const userId = getCurrentUserId();
    if (!userId) {
      throw new Error('로그인이 필요합니다.');
    }

    const requestData = {
      userId: userId,
      inbodyRecordId: inbodyRecordId,
      answerText: surveyText,
      surveyData: surveyData ? JSON.stringify(surveyData) : null
    };

    console.log('📤 설문조사 저장 요청:', requestData);

    const response = await apiCall('/api/survey', {
      method: 'POST',
      body: JSON.stringify(requestData)
    });

    console.log('✅ 설문조사 저장 성공:', response);
    return response;
  } catch (error) {
    console.error('❌ 설문조사 저장 실패:', error);
    throw error;
  }
}

/**
 * 사용자별 설문조사 이력 조회
 */
export async function getSurveyHistoryByUserId(userId) {
  try {
    const response = await apiCall(`/api/survey/user/${userId}/history`, {
      method: 'GET'
    });

    console.log('✅ 설문조사 이력 조회 성공:', response);
    return response;
  } catch (error) {
    console.error('❌ 설문조사 이력 조회 실패:', error);
    throw error;
  }
}


/**
 * 설문조사 상세 조회
 */
export async function getSurveyById(surveyId) {
  try {
    const response = await apiCall(`/api/survey/${surveyId}`, {
      method: 'GET'
    });

    console.log('✅ 설문조사 상세 조회 성공:', response);
    return response;
  } catch (error) {
    console.error('❌ 설문조사 상세 조회 실패:', error);
    throw error;
  }
}

/**
 * 사용자별 최신 설문조사 조회
 */
export async function getLatestSurveyByUserId(userId) {
  try {
    const response = await apiCall(`/api/survey/user/${userId}/latest`, {
      method: 'GET'
    });

    console.log('✅ 최신 설문조사 조회 성공:', response);
    return response;
  } catch (error) {
    console.error('❌ 최신 설문조사 조회 실패:', error);
    throw error;
  }
}

/**
 * 설문조사 삭제
 */
export async function deleteSurvey(surveyId, userId) {
  try {
    const response = await apiCall(`/api/survey/user/${userId}/history/${surveyId}`, {
      method: 'DELETE'
    });

    console.log('✅ 설문조사 삭제 성공:', response);
    return response;
  } catch (error) {
    console.error('❌ 설문조사 삭제 실패:', error);
    throw error;
  }
} 