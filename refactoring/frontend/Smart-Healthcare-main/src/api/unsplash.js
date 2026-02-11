// 백엔드 API를 통한 이미지 검색 유틸리티
// 사용법: fetchUnsplashImage('운동명 또는 식단명', 'exercise' 또는 'diet')

/**
 * 백엔드 API를 통한 이미지 검색
 * @param {string|object} query - 검색어 (문자열) 또는 운동/식단 객체
 * @param {string} type - 'exercise' 또는 'diet'
 */
export async function fetchUnsplashImage(query, type = 'exercise') {
  try {
    // 객체인 경우 AI가 생성한 unsplashQuery를 우선 사용하고, 없으면 name을 사용
    let searchTerm;
    if (typeof query === 'string') {
      searchTerm = query;
    } else if (query && typeof query === 'object') {
      searchTerm = query.unsplashQuery || query.name || '';
    } else {
      searchTerm = '';
    }

    if (!searchTerm || typeof searchTerm !== 'string') {
      console.warn('⚠️ 이미지 검색용 검색어가 비어있습니다.', query);
      return null;
    }

    console.log(`🔍 백엔드 API를 통한 이미지 검색: "${searchTerm}" (${type})`);
    
    // 백엔드 API 엔드포인트 결정 (운동/식단 타입에 따라 분기)
    const encoded = encodeURIComponent(searchTerm);
    let endpoint;
    if (type === 'diet') {
      endpoint = `/api/images/diet/${encoded}`;
    } else if (type === 'exercise') {
      endpoint = `/api/images/workout/${encoded}`;
    } else {
      // 기본값은 식단 엔드포인트 사용
      endpoint = `/api/images/diet/${encoded}`;
    }
    
    // 백엔드 API 호출
    const res = await fetch(endpoint);
    
    if (!res.ok) {
      console.warn(`⚠️ 백엔드 이미지 API 오류 (${res.status}): ${searchTerm}`);
      if (res.status === 403) {
        console.warn('⚠️ Unsplash API 403 오류 - API 키 문제 또는 할당량 초과');
      }
      return null;
    }
    
    const data = await res.json();
    
    if (data.success && data.data?.imageUrl) {
      console.log(`✅ 백엔드에서 이미지 URL 가져오기 성공: ${data.data.imageUrl}`);
      return data.data.imageUrl;
    } else {
      console.log('❌ 백엔드에서 이미지 URL을 찾을 수 없음');
      return null;
    }
    
  } catch (error) {
    console.warn('⚠️ 백엔드 이미지 API 호출 실패:', error.message);
    return null;
  }
} 