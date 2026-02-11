// Storage Manager import
import { storageManager } from '../utils/storageManager';

// API 기본 설정
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// JWT 토큰 가져오기
const getAuthToken = () => {
  try {
    const token = storageManager.getItem('token');
    console.log('🔐 getAuthToken - 토큰 조회:', token ? '존재함' : '없음');
    
    // 토큰이 있으면 유효성 검사
    if (token) {
      try {
        // JWT 토큰은 3개의 부분으로 구성됨 (header.payload.signature)
        const parts = token.split('.');
        if (parts.length !== 3) {
          console.warn('🔐 잘못된 JWT 토큰 형식');
          return null;
        }
        
        // payload 부분을 디코딩하여 만료 시간 확인
        const payload = JSON.parse(atob(parts[1]));
        const currentTime = Math.floor(Date.now() / 1000);
        
        // exp (만료 시간) 필드가 있고, 현재 시간보다 미래인지 확인
        if (payload.exp && payload.exp < currentTime) {
          console.warn('🔐 JWT 토큰이 만료되었습니다.');
          // 만료된 토큰 제거
          storageManager.removeItem('token');
          storageManager.removeItem('user');
          storageManager.removeItem('currentUser');
          return null;
        }
        
        return token;
      } catch (error) {
        console.error('토큰 유효성 검사 실패:', error);
        // 잘못된 토큰 제거
        storageManager.removeItem('token');
        storageManager.removeItem('user');
        storageManager.removeItem('currentUser');
        return null;
      }
    }
    
    return null;
  } catch (error) {
    console.error('토큰 조회 실패:', error);
    return null;
  }
};

// 인증이 필요하지 않은 엔드포인트 목록
const PUBLIC_ENDPOINTS = [
  '/api/auth/login',
  '/api/auth/signup',
  '/api/auth/refresh',
  '/api/auth/admin-login',  // 관리자 로그인 API 추가
  '/api-docs',
  '/swagger-ui',
  '/actuator'
];

// 엔드포인트가 공개 API인지 확인
const isPublicEndpoint = (endpoint) => {
  return PUBLIC_ENDPOINTS.some(publicEndpoint => endpoint.startsWith(publicEndpoint));
};

// 공통 API 호출 함수
export const apiCall = async (endpoint, options = {}) => {
  const url = `${API_BASE_URL}${endpoint}`;
  
  // 기본 헤더 설정
  const defaultHeaders = {
    'Content-Type': 'application/json',
  };
  
  // 인증이 필요한 API인 경우 JWT 토큰 추가
  if (!isPublicEndpoint(endpoint)) {
    const token = getAuthToken();
    console.log('🔐 apiCall - 엔드포인트:', endpoint);
    console.log('🔐 apiCall - 토큰 존재:', token ? '예' : '아니오');
    console.log('🔐 apiCall - 토큰 값:', token ? token.substring(0, 20) + '...' : 'null');
    if (token) {
      defaultHeaders['Authorization'] = `Bearer ${token}`;
      console.log('🔐 apiCall - Authorization 헤더 추가됨:', `Bearer ${token.substring(0, 20)}...`);
    } else {
      console.warn('🔐 apiCall - 토큰이 없어서 Authorization 헤더 추가 안됨');
    }
  }
  
  const defaultOptions = {
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  };

  try {
    const response = await fetch(url, {
      ...defaultOptions,
      ...options,
    });

    // 401 Unauthorized 응답 처리 (토큰 만료 또는 유효하지 않음)
    if (response.status === 401) {
      console.warn('🔐 인증 실패 - 토큰이 만료되었거나 유효하지 않습니다.');
      
      // 토큰이 있는 경우에만 로그아웃 처리
      if (getAuthToken()) {
        // AuthManager를 사용하여 로그아웃 처리
        try {
          const { AuthManager } = await import('../utils/authManager');
          AuthManager.logoutUser();
          
          // 로그인 페이지로 리다이렉트 (브라우저 환경에서만)
          if (typeof window !== 'undefined') {
            window.location.href = '/login';
          }
        } catch (error) {
          console.error('로그아웃 처리 실패:', error);
        }
      }
      
      throw new Error('인증이 필요합니다. 다시 로그인해주세요.');
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      
      // 디버깅을 위한 상세 로그
      console.error('❌ API 에러 응답:', {
        status: response.status,
        statusText: response.statusText,
        errorData: errorData,
        endpoint: endpoint
      });
      
      // 유효성 검사 오류 처리 (구체적인 필드별 오류 메시지)
      if (errorData.data && typeof errorData.data === 'object') {
        const validationErrors = Object.values(errorData.data).join(', ');
        throw new Error(validationErrors || errorData.error || errorData.message || `HTTP ${response.status}: ${response.statusText}`);
      }
      
      // error 또는 message 필드에서 에러 메시지 추출
      const errorMessage = errorData.error || errorData.message || `HTTP ${response.status}: ${response.statusText}`;
      throw new Error(errorMessage);
    }

    // 응답 body가 있는 경우에만 JSON 파싱
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      const text = await response.text();
      return text ? JSON.parse(text) : {};
    }
    
    // body가 없는 경우 (204 No Content 등)
    return null;
  } catch (error) {
    // 네트워크 오류나 기타 오류 처리
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error('서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.');
    }
    throw error;
  }
};

export default API_BASE_URL;
