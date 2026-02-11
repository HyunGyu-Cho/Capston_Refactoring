// 인바디 데이터 관련 API
import { apiCall } from './config';

/**
 * 한글 필드명을 영문 필드명으로 변환하는 매핑
 */
const FIELD_MAPPING = {
  '성별': 'gender',
  '사용자 출생년도': 'birthYear',
  '체중': 'weight',
  '총체수분': 'totalBodyWater',
  '단백질': 'protein',
  '무기질': 'mineral',
  '체지방량': 'bodyFatMass',
  '근육량': 'muscleMass',
  '제지방량': 'fatFreeMass',
  '골격근량': 'skeletalMuscleMass',
  '체지방률': 'bodyFatPercentage',
  '오른팔 근육량': 'rightArmMuscleMass',
  '왼팔 근육량': 'leftArmMuscleMass',
  '몸통 근육량': 'trunkMuscleMass',
  '오른다리 근육량': 'rightLegMuscleMass',
  '왼다리 근육량': 'leftLegMuscleMass',
  '오른팔 체지방량': 'rightArmFatMass',
  '왼팔 체지방량': 'leftArmFatMass',
  '몸통 체지방량': 'trunkFatMass',
  '오른다리 체지방량': 'rightLegFatMass',
  '왼다리 체지방량': 'leftLegFatMass',
  '인바디점수': 'inbodyScore',
  '적정체중': 'idealWeight',
  '체중조절': 'weightControl',
  '지방조절': 'fatControl',
  '근육조절': 'muscleControl',
  '기초대사량': 'basalMetabolism',
  '복부지방률': 'abdominalFatPercentage',
  '내장지방레벨': 'visceralFatLevel',
  '비만도': 'obesityDegree',
  '체질량지수': 'bmi',
  '골무기질량': 'boneMineralContent',
  '복부둘레': 'waistCircumference'
};

/**
 * 한글 인바디 데이터를 영문 필드명으로 변환
 */
const convertInbodyData = (koreanData) => {
  const convertedData = {};
  
  Object.keys(koreanData).forEach(key => {
    const englishKey = FIELD_MAPPING[key];
    if (englishKey) {
      let value = koreanData[key];
      
      // 성별 변환
      if (key === '성별') {
        value = value === '남성' ? 'MALE' : 'FEMALE';
      }
      
      // 숫자 필드 변환
      if (typeof value === 'string' && !isNaN(parseFloat(value))) {
        value = parseFloat(value);
      }
      
      convertedData[englishKey] = value;
    }
  });
  
  return convertedData;
};

/**
 * 영문 필드명을 한글 필드명으로 변환 (백엔드 응답용)
 */
const convertEnglishToKorean = (englishData) => {
  const koreanData = {};
  
  // 역매핑 생성
  const reverseMapping = {};
  Object.entries(FIELD_MAPPING).forEach(([korean, english]) => {
    reverseMapping[english] = korean;
  });
  
  Object.keys(englishData).forEach(key => {
    const koreanKey = reverseMapping[key];
    if (koreanKey) {
      let value = englishData[key];
      
      // 성별 역변환
      if (key === 'gender') {
        value = value === 'MALE' ? '남성' : '여성'; // 프론트엔드에서는 "남성"/"여성" 사용
      }
      
      koreanData[koreanKey] = value;
    } else {
      // 매핑되지 않은 필드는 그대로 유지 (id, createdAt 등)
      koreanData[key] = englishData[key];
    }
  });
  
  return koreanData;
};

/**
 * 인바디 데이터 저장
 */
export async function saveInbodyData(inbodyData, userId) {
  try {
    console.log('📤 원본 인바디 데이터:', inbodyData);
    
    // 한글 필드명을 영문으로 변환
    const convertedData = convertInbodyData(inbodyData);
    convertedData.userId = userId;
    
    console.log('📤 변환된 인바디 데이터:', convertedData);
    
    const response = await apiCall('/api/inbody', {
      method: 'POST',
      body: JSON.stringify(convertedData)
    });
    
    // 백엔드에서 이미 ApiResponseDto로 래핑되어 있으므로 data만 반환
    console.log('✅ 인바디 데이터 저장 성공:', response.data);
    return response.data;
  } catch (error) {
    console.error('❌ 인바디 데이터 저장 실패:', error);
    throw error;
  }
}

/**
 * 사용자별 인바디 기록 조회 (Pageable 방식)
 */
export async function getInbodyRecords(userId, options = {}) {
  try {
    const { page = 0, size = 10, sort = 'createdAt,desc', startDate, endDate } = options;
    let endpoint = `/api/inbody/user/${userId}`;
    
    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', size);
    params.append('sort', sort);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    endpoint += `?${params.toString()}`;
    
    const response = await apiCall(endpoint, {
      method: 'GET'
    });
    
    console.log('✅ 인바디 기록 조회 성공:', response);
    console.log('✅ 응답 구조:', Object.keys(response || {}));
    
    // ApiResponseDto 형식으로 응답이 오므로 그대로 반환
    return response;
  } catch (error) {
    console.error('❌ 인바디 기록 조회 실패:', error);
    throw error;
  }
}


/**
 * 사용자의 최신 인바디 기록 조회 (size=1)
 */
export async function getLatestInbodyRecord(userId) {
  try {
    const response = await getInbodyRecords(userId, { size: 1 });
    
    console.log('🔍 getLatestInbodyRecord 응답:', response);
    
    // ApiResponseDto 형식에서 데이터 추출
    if (response && response.success && response.data && response.data.content && response.data.content.length > 0) {
      const englishData = response.data.content[0]; // Page의 content에서 첫 번째 요소
      console.log('🔍 영문 데이터 (변환 전):', englishData);
      console.log('🔍 waistCircumference 값:', englishData.waistCircumference);
      
      // 백엔드에서 받은 영문 필드명을 한글 필드명으로 변환
      const koreanData = convertEnglishToKorean(englishData);
      console.log('🔄 백엔드 응답을 한글 필드명으로 변환:', koreanData);
      console.log('🔍 복부둘레 값:', koreanData['복부둘레']);
      return koreanData; // 변환된 최신 기록 반환
    }
    
    console.log('⚠️ 최신 인바디 기록이 없습니다.');
    return null; // 기록이 없는 경우
  } catch (error) {
    console.error('❌ 최신 인바디 기록 조회 실패:', error);
    throw error;
  }
}

/**
 * 전체 인바디 기록 조회 (기본값)
 */
export async function getAllInbodyRecords(userId) {
  return await getInbodyRecords(userId);
}

/**
 * 특정 페이지 인바디 기록 조회
 */
export async function getInbodyRecordsPage(userId, page, size = 10) {
  return await getInbodyRecords(userId, { page, size });
}

/**
 * 인바디 기록 삭제
 */
export async function deleteInbodyRecord(recordId, userId) {
  try {
    const response = await apiCall(`/api/inbody/user/${userId}/records/${recordId}`, {
      method: 'DELETE'
    });
    
    // 백엔드에서 이미 ApiResponseDto로 래핑되어 있으므로 data만 반환
    console.log('✅ 인바디 기록 삭제 성공:', response.data);
    return response.data;
  } catch (error) {
    console.error('❌ 인바디 기록 삭제 실패:', error);
    throw error;
  }
}

