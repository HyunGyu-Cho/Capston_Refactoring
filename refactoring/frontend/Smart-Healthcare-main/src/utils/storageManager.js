/**
 * Storage 관리 유틸리티
 * localStorage와 sessionStorage를 쉽게 전환할 수 있도록 추상화
 */

// 사용할 Storage 타입 설정 (localStorage 또는 sessionStorage)
const STORAGE_TYPE = 'sessionStorage'; // 'localStorage' 또는 'sessionStorage'

const storage = STORAGE_TYPE === 'localStorage' ? localStorage : sessionStorage;

export const storageManager = {
  // 데이터 저장
  setItem: (key, value) => {
    try {
      storage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value));
    } catch (error) {
      console.error('Storage 저장 실패:', error);
    }
  },

  // 데이터 조회
  getItem: (key) => {
    try {
      const item = storage.getItem(key);
      if (!item) return null;
      
      // JSON 파싱 시도
      try {
        return JSON.parse(item);
      } catch {
        return item; // 문자열 그대로 반환
      }
    } catch (error) {
      console.error('Storage 조회 실패:', error);
      return null;
    }
  },

  // 데이터 삭제
  removeItem: (key) => {
    try {
      storage.removeItem(key);
    } catch (error) {
      console.error('Storage 삭제 실패:', error);
    }
  },

  // 전체 삭제
  clear: () => {
    try {
      storage.clear();
    } catch (error) {
      console.error('Storage 전체 삭제 실패:', error);
    }
  },

  // 키 존재 여부 확인
  hasItem: (key) => {
    return storage.getItem(key) !== null;
  },

  // 사용 중인 Storage 타입 반환
  getStorageType: () => STORAGE_TYPE
};

export default storageManager;
