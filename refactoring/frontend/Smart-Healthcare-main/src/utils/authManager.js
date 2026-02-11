/**
 * 인증 상태 관리자
 * 일반 사용자와 관리자 로그인 상태를 관리하고 중복 로그인을 방지합니다.
 */

import { storageManager } from './storageManager';

export const AuthManager = {
  // 로그인 상태 확인
  isUserLoggedIn: () => {
    return storageManager.getItem('token') && storageManager.getItem('currentUser');
  },

  isAdminLoggedIn: () => {
    return storageManager.getItem('adminToken') && storageManager.getItem('currentAdmin');
  },

  // 일반 사용자 로그인
  loginUser: (token, userInfo) => {
    // 관리자 로그인 상태가 있으면 제거
    if (AuthManager.isAdminLoggedIn()) {
      console.log('🔄 관리자 로그인 상태 제거 후 일반 사용자 로그인');
      AuthManager.logoutAdmin();
    }

    storageManager.setItem('token', token);
    storageManager.setItem('user', userInfo); // 'currentUser' -> 'user'로 통일
    storageManager.setItem('currentUser', userInfo); // 기존 호환성 유지
    console.log('✅ 일반 사용자 로그인 완료');
    
    // 커스텀 이벤트 발생으로 useUser Hook에 변경 알림
    window.dispatchEvent(new Event('userChanged'));
  },

  // 관리자 로그인
  loginAdmin: (token, adminInfo) => {
    // 일반 사용자 로그인 상태가 있으면 제거
    if (AuthManager.isUserLoggedIn()) {
      console.log('🔄 일반 사용자 로그인 상태 제거 후 관리자 로그인');
      AuthManager.logoutUser();
    }

    storageManager.setItem('adminToken', token);
    storageManager.setItem('currentAdmin', adminInfo);
    console.log('✅ 관리자 로그인 완료');
  },

  // 일반 사용자 로그아웃
  logoutUser: () => {
    storageManager.removeItem('token');
    storageManager.removeItem('user');
    storageManager.removeItem('currentUser');
    console.log('🚪 일반 사용자 로그아웃');
    
    // 커스텀 이벤트 발생으로 useUser Hook에 변경 알림
    window.dispatchEvent(new Event('userChanged'));
  },

  // 관리자 로그아웃
  logoutAdmin: () => {
    storageManager.removeItem('adminToken');
    storageManager.removeItem('currentAdmin');
    console.log('🚪 관리자 로그아웃');
  },

  // 전체 로그아웃
  logoutAll: () => {
    AuthManager.logoutUser();
    AuthManager.logoutAdmin();
    console.log('🚪 전체 로그아웃');
  },

  // 현재 로그인 상태
  getCurrentAuthState: () => {
    return {
      userLoggedIn: AuthManager.isUserLoggedIn(),
      adminLoggedIn: AuthManager.isAdminLoggedIn(),
      userInfo: AuthManager.isUserLoggedIn() ? storageManager.getItem('currentUser') : null,
      adminInfo: AuthManager.isAdminLoggedIn() ? storageManager.getItem('currentAdmin') : null
    };
  },

  // 로그인 타입 확인
  getLoginType: () => {
    if (AuthManager.isAdminLoggedIn()) return 'admin';
    if (AuthManager.isUserLoggedIn()) return 'user';
    return 'none';
  }
};

export default AuthManager;
