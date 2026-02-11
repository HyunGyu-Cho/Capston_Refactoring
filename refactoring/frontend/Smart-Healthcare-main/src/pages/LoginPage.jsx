import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login, signup } from '../api/auth';
import { Shield } from 'lucide-react';

const LoginPage = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      if (isLogin) {
        // 로그인
        await login(email, password);
        // auth.js에서 이미 sessionStorage에 저장되므로 페이지 이동
        navigate('/main'); // 로그인 성공 시 메인 페이지로
      } else {
        // 회원가입
        await signup(email, password);
        setError('회원가입이 완료되었습니다. 로그인해주세요.');
        setIsLogin(true);
      }
    } catch (err) {
      setError(err.message || '오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-purple-50 to-pink-50 flex items-center justify-center py-16 px-6">
      <div className="w-full max-w-md">
        {/* 헤더 */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full mb-6 shadow-lg">
            <span className="text-3xl">🔐</span>
          </div>
          <h1 className="text-3xl font-bold text-gray-800 mb-2">{isLogin ? '로그인' : '회원가입'}</h1>
          <p className="text-gray-600">스마트 헬스케어에 오신 것을 환영합니다</p>
        </div>

        {/* 로그인/회원가입 카드 */}
        <div className="bg-white rounded-3xl shadow-xl border border-gray-100 p-8 mb-4">
          {/* 관리자 로그인 버튼 */}
          <div className="flex justify-end mb-6">
            <button
              type="button"
              onClick={() => navigate('/admin/login')}
              title="시스템 관리자 전용 로그인 페이지로 이동"
              className="inline-flex items-center gap-2 bg-gradient-to-r from-red-600 to-red-700 text-white px-4 py-2 rounded-xl text-xs font-medium hover:from-red-700 hover:to-red-800 transition-all transform hover:scale-105 shadow-lg"
            >
              <Shield className="w-3 h-3" />
              관리자 로그인
            </button>
          </div>
      
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="flex items-center gap-2 mb-2 text-sm font-medium text-gray-700">
                <span className="text-lg">📧</span>
                이메일
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                placeholder="이메일을 입력하세요"
                className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all"
              />
            </div>
            
            <div>
              <label className="flex items-center gap-2 mb-2 text-sm font-medium text-gray-700">
                <span className="text-lg">🔒</span>
                비밀번호
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                placeholder="비밀번호를 입력하세요"
                className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all"
              />
            </div>
            
            {error && (
              <div className="p-4 bg-gradient-to-r from-red-50 to-pink-50 border border-red-200 rounded-xl">
                <div className="flex items-center gap-3">
                  <div className="w-6 h-6 bg-red-500 rounded-full flex items-center justify-center flex-shrink-0">
                    <span className="text-white text-xs">{error.includes('완료') ? '✓' : '⚠️'}</span>
                  </div>
                  <p className={`text-sm ${error.includes('완료') ? 'text-green-700' : 'text-red-700'}`}>{error}</p>
                </div>
              </div>
            )}
            
            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white py-4 rounded-xl text-base font-medium hover:from-blue-600 hover:to-purple-700 disabled:opacity-50 transition-all transform hover:scale-[1.02] shadow-lg"
            >
              {loading ? (
                <>
                  <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  처리중...
                </>
              ) : (
                <>
                  <span className="text-xl">{isLogin ? '🚀' : '✨'}</span>
                  {isLogin ? '로그인' : '회원가입'}
                </>
              )}
            </button>
          </form>
        </div>
        
        {/* 토글 버튼 */}
        <div className="text-center mt-6">
          {isLogin ? (
            <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-6">
              <p className="text-gray-600 mb-3">아직 계정이 없으신가요?</p>
              <button
                type="button"
                onClick={() => setIsLogin(false)}
                className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 font-medium transition-colors"
              >
                <span className="text-lg">✨</span>
                회원가입하기
              </button>
            </div>
          ) : (
            <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-6">
              <p className="text-gray-600 mb-3">이미 계정이 있으신가요?</p>
              <button
                type="button"
                onClick={() => setIsLogin(true)}
                className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 font-medium transition-colors"
              >
                <span className="text-lg">🚀</span>
                로그인하기
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default LoginPage;