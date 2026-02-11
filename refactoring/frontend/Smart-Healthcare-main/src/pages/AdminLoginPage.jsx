import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import Button from '../components/Button';
import InputField from '../components/InputField';
import { Shield, Lock, User, AlertCircle } from 'lucide-react';
import AuthManager from '../utils/authManager';
import { apiCall } from '../api/config';

export default function AdminLoginPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleInputChange = (field, value) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    if (error) setError(''); // 입력 시 에러 메시지 초기화
  };

  const handleAdminLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      console.log('🔐 관리자 로그인 시도:', formData.email);
      
      // apiCall을 사용하여 관리자 로그인 API 호출 (공개 API이므로 토큰 불필요)
      const data = await apiCall('/api/auth/admin-login', {
        method: 'POST',
        body: JSON.stringify(formData)
      });

      console.log('🔐 관리자 로그인 응답:', data);
      console.log('🔐 응답 구조 분석:');
      console.log('  - data.success:', data.success);
      console.log('  - data.data:', data.data);
      console.log('  - data.data.token:', data.data?.token);
      console.log('  - data.data.user:', data.data?.user);

      if (data.success) {
        // AuthManager를 통해 관리자 로그인 (중복 로그인 방지)
        const adminInfo = {
          id: data.data.user.id,
          email: data.data.user.email,
          role: data.data.user.role,
          loginTime: new Date().toISOString()
        };
        
        console.log('🔐 관리자 정보 저장:', adminInfo);
        console.log('🔐 토큰:', data.data.token);
        
        // AuthManager.loginAdmin 호출
        AuthManager.loginAdmin(data.data.token, adminInfo);
        console.log('🔐 AuthManager.loginAdmin 호출 완료');

        // 저장된 데이터 확인
        console.log('🔐 저장 후 sessionStorage 확인:');
        console.log('  - adminToken:', sessionStorage.getItem('adminToken'));
        console.log('  - currentAdmin:', sessionStorage.getItem('currentAdmin'));
        
        // AuthManager 상태 확인
        console.log('🔐 AuthManager 상태 확인:');
        console.log('  - isAdminLoggedIn():', AuthManager.isAdminLoggedIn());
        console.log('  - getCurrentAuthState():', AuthManager.getCurrentAuthState());

        console.log('🔐 관리자 대시보드로 이동');
        // 관리자 대시보드로 이동
        navigate('/admin/dashboard');
      } else {
        setError(data.error || '관리자 로그인에 실패했습니다.');
      }
    } catch (error) {
      console.error('❌ 관리자 로그인 오류:', error);
      setError(error.message || '서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout>
      <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-8">
          {/* 헤더 */}
          <div className="text-center">
            <div className="flex justify-center mb-6">
              <div className="bg-red-600 p-4 rounded-full">
                <Shield className="w-12 h-12 text-white" />
              </div>
            </div>
            <h2 className="text-3xl font-bold text-white mb-2">관리자 로그인</h2>
            <p className="text-gray-300">
              Smart Healthcare 시스템 관리자 전용 페이지입니다
            </p>
            <div className="mt-4 p-3 bg-blue-900/20 border border-blue-500/50 rounded-lg">
              <div className="flex items-center gap-2 text-blue-300">
                <AlertCircle className="w-5 h-5" />
                <span className="text-sm font-medium">
                  관리자 전용 로그인페이지
                </span>
              </div>
            </div>
          </div>

          {/* 로그인 폼 */}
          <form className="mt-8 space-y-6" onSubmit={handleAdminLogin}>
            <div className="bg-white rounded-2xl shadow-xl p-8">
              <div className="space-y-6">
                {/* 이메일 입력 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    관리자 이메일
                  </label>
                  <div className="relative">
                    <User className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
                    <InputField
                      type="email"
                      value={formData.email}
                      onChange={(e) => handleInputChange('email', e.target.value)}
                      placeholder="관리자 모드: 관리자 계정 이메일 입력"
                      className="pl-10"
                      required
                    />
                  </div>
                </div>

                {/* 비밀번호 입력 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    관리자 비밀번호
                  </label>
                  <div className="relative">
                    <Lock className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
                    <InputField
                      type="password"
                      value={formData.password}
                      onChange={(e) => handleInputChange('password', e.target.value)}
                      placeholder="관리자 모드: 관리자 계정 이메일 입력"
                      className="pl-10"
                      required
                    />
                  </div>
                </div>

                {/* 에러 메시지 */}
                {error && (
                  <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                    <div className="flex items-center gap-2 text-red-700">
                      <AlertCircle className="w-5 h-5" />
                      <span className="text-sm">{error}</span>
                    </div>
                  </div>
                )}

                {/* 로그인 버튼 */}
                <Button
                  type="submit"
                  disabled={loading || !formData.email || !formData.password}
                  className={`w-full py-3 rounded-lg font-medium transition-all ${
                    loading || !formData.email || !formData.password
                      ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                      : 'bg-red-600 text-white hover:bg-red-700'
                  }`}
                >
                  {loading ? (
                    <div className="flex items-center justify-center gap-2">
                      <div className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full"></div>
                      인증 중...
                    </div>
                  ) : (
                    <div className="flex items-center justify-center gap-2">
                      <Shield className="w-5 h-5" />
                      관리자 로그인
                    </div>
                  )}
                </Button>
              </div>
            </div>
          </form>

          {/* 하단 안내 */}
          <div className="text-center space-y-4">
            <p className="text-gray-400 text-sm">
              일반 사용자이신가요?
            </p>
            <button
              onClick={() => navigate('/login')}
              className="text-blue-400 hover:text-blue-300 transition-colors text-sm underline"
            >
              일반 사용자 로그인 페이지로 이동
            </button>
            <button
              onClick={() => navigate('/')}
              className="block text-gray-400 hover:text-gray-300 transition-colors text-sm"
            >
              메인 페이지로 돌아가기
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
}
