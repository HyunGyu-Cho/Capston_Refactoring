/**
 * 🔐 인증 관련 API
 * - 간단한 로그인/회원가입 (개발용)
 * - Spring Security 없이 구현
 */

import { apiCall } from './config';
import { useState, useEffect } from 'react';
import AuthManager from '../utils/authManager';
import { storageManager } from '../utils/storageManager';

/**
 * 회원가입
 */
export async function signup(email, password) {
  try {
    const data = await apiCall('/api/auth/signup', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    
    console.log('✅ 회원가입 성공:', data);
    return data;
  } catch (error) {
    console.error('❌ 회원가입 실패:', error);
    throw error;
  }
}

/**
 * 로그인
 */
export async function login(email, password) {
  try {
    const data = await apiCall('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    
    // apiCall이 성공하면 data는 유효한 응답
    // AuthManager를 통해 로그인 상태 관리 (중복 로그인 방지)
    AuthManager.loginUser(data.data.token, data.data.user);
    
    console.log('✅ 일반 사용자 로그인 성공:', data.data.user);
    return data;
  } catch (error) {
    console.error('❌ 로그인 실패:', error);
    throw error;
  }
}

/**
 * 로그아웃
 */
export function logout() {
  AuthManager.logoutUser();
  console.log('✅ 일반 사용자 로그아웃 완료');
}

/**
 * 현재 로그인된 사용자 정보 조회
 */
export function getCurrentUser() {
  try {
    return storageManager.getItem('user');
  } catch (error) {
    console.error('❌ 사용자 정보 조회 실패:', error);
    return null;
  }
}

/**
 * 현재 토큰 조회
 */
export function getCurrentToken() {
  return storageManager.getItem('token');
}

/**
 * JWT 토큰 유효성 검사 (기본적인 형식 검사)
 */
export function isTokenValid(token) {
  if (!token) return false;
  
  try {
    // JWT 토큰은 3개의 부분으로 구성됨 (header.payload.signature)
    const parts = token.split('.');
    if (parts.length !== 3) return false;
    
    // payload 부분을 디코딩하여 만료 시간 확인
    const payload = JSON.parse(atob(parts[1]));
    const currentTime = Math.floor(Date.now() / 1000);
    
    // exp (만료 시간) 필드가 있고, 현재 시간보다 미래인지 확인
    if (payload.exp && payload.exp < currentTime) {
      console.warn('🔐 JWT 토큰이 만료되었습니다.');
      return false;
    }
    
    return true;
  } catch (error) {
    console.error('토큰 유효성 검사 실패:', error);
    return false;
  }
}

/**
 * 로그인 상태 확인
 */
export function isLoggedIn() {
  const user = getCurrentUser();
  const token = getCurrentToken();
  
  // 사용자 정보와 토큰이 모두 있고, 토큰이 유효한 경우에만 로그인 상태로 판단
  if (!user || !token) {
    return false;
  }
  
  // 토큰 유효성 검사
  if (!isTokenValid(token)) {
    // 만료된 토큰이 있으면 자동으로 로그아웃 처리
    console.warn('🔐 만료된 토큰으로 인해 자동 로그아웃 처리');
    logout();
    return false;
  }
  
  return true;
}

/**
 * 사용자 ID 조회 (체형분석용)
 */
export function getCurrentUserId() {
  const user = getCurrentUser();
  return user ? user.id : null;
}

/**
 * 사용자별 데이터 조회 (UserContext의 getUserData 대체)
 */
export function getUserData(key) {
  const user = getCurrentUser();
  if (!user) return null;
  
  // 사용자별 데이터 키 생성
  const userKey = `${key}_${user.id}`;
  try {
    return storageManager.getItem(userKey);
  } catch (error) {
    console.error(`${key} 데이터 로드 실패:`, error);
    return null;
  }
}

/**
 * 사용자별 데이터 저장 (UserContext의 setUserData 대체)
 */
export function setUserData(key, data) {
  const user = getCurrentUser();
  if (!user) return;
  
  // 사용자별 데이터 키 생성
  const userKey = `${key}_${user.id}`;
  try {
    storageManager.setItem(userKey, data);
  } catch (error) {
    console.error(`${key} 데이터 저장 실패:`, error);
  }
}

/**
 * 사용자 정보 업데이트 (UserContext의 updateUser 대체)
 */
export function updateUser(updatedData) {
  const user = getCurrentUser();
  if (!user) return;
  
  const updatedUser = { ...user, ...updatedData };
  storageManager.setItem('user', updatedUser);
  return updatedUser;
}

/**
 * useUser 훅 (React Hook)
 */
export function useUser() {
  const [user, setUser] = useState(getCurrentUser());
  const [forceUpdate, setForceUpdate] = useState(0);

  useEffect(() => {
    // 사용자 정보 변경 감지
    const handleStorageChange = () => {
      setUser(getCurrentUser());
      setForceUpdate(prev => prev + 1); // 강제 리렌더링
    };

    // storage 변경 이벤트 리스너 (다른 탭에서의 변경 감지)
    window.addEventListener('storage', handleStorageChange);
    
    // 같은 탭에서의 변경 감지를 위한 커스텀 이벤트
    window.addEventListener('userChanged', handleStorageChange);
    
    // 초기 사용자 정보 설정
    setUser(getCurrentUser());

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('userChanged', handleStorageChange);
    };
  }, []);

  const getUserData = (key) => {
    if (!user) return null;
    const userKey = `${key}_${user.id}`;
    try {
      return storageManager.getItem(userKey);
    } catch (error) {
      console.error(`${key} 데이터 로드 실패:`, error);
      return null;
    }
  };

  const setUserData = (key, data) => {
    if (!user) return;
    const userKey = `${key}_${user.id}`;
    try {
      storageManager.setItem(userKey, data);
      // 상태 업데이트를 위해 강제 리렌더링 트리거
      setUser(getCurrentUser());
    } catch (error) {
      console.error(`${key} 데이터 저장 실패:`, error);
    }
  };

  return {
    user,
    getUserData,
    setUserData,
    isLoggedIn: isLoggedIn(), // 토큰 유효성 검사 포함
    userId: user?.id
  };
}