import React from 'react';
import { Navigate } from 'react-router-dom';
import { useUser } from '../api/auth';

/**
 * 인증이 필요한 페이지를 보호하는 컴포넌트
 * 로그인하지 않은 사용자는 로그인 페이지로 리다이렉트
 */
const ProtectedRoute = ({ children }) => {
  const { isLoggedIn } = useUser();
  
  if (!isLoggedIn) {
    return <Navigate to="/login" replace />;
  }
  
  return children;
};

export default ProtectedRoute;
