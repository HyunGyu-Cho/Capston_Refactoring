// src/pages/SignupPage.jsx
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import Layout from '../components/Layout';
import InputField from '../components/InputField';
import Button from '../components/Button';
import { signup } from '../api/auth';

export default function SignupPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = e => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const validateForm = () => {
    if (!formData.email || !formData.password || !formData.confirmPassword) {
      setError('모든 필드를 입력해주세요.');
      return false;
    }

    if (formData.password !== formData.confirmPassword) {
      setError('비밀번호가 일치하지 않습니다.');
      return false;
    }

    if (formData.password.length < 6) {
      setError('비밀번호는 6자 이상이어야 합니다.');
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      setError('올바른 이메일 형식을 입력해주세요.');
      return false;
    }

    return true;
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // 폼 검증
      if (!validateForm()) {
        return;
      }

      // 실제 회원가입 API 호출
      await signup(formData.email, formData.password);
      
      // 회원가입 성공 시 처리
      alert('회원가입이 완료되었습니다! 로그인해주세요.');
      navigate('/login'); // 로그인 페이지로 이동
      
    } catch (err) {
      setError('회원가입에 실패했습니다. 다시 시도해주세요.');
      console.error('회원가입 실패:', err);
    } finally {
      setLoading(false);
    }
  };

  // 소셜 회원가입 핸들러 (실제 인증 로직은 별도 구현 필요)
  const handleSocialSignup = async provider => {
    setLoading(true);
    setError('');

    try {
      // 실제 소셜 회원가입 API 호출 (추후 구현)
      // const response = await socialSignupAPI(provider);
      
      // 임시 소셜 회원가입 로직 (개발용)
      console.log(`${provider} 회원가입 시도`);
      alert(`${provider} 회원가입/로그인은 추후 구현 예정입니다.`);
      
      // 실제 구현 시에는 아래와 같이 처리
      // navigate('/'); // 회원가입 성공 시 메인 페이지로 이동
    } catch (err) {
      setError(`${provider} 회원가입에 실패했습니다.`);
      console.error(`${provider} 회원가입 실패:`, err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout>
      {/* Hero/Intro Section */}
      <section className="bg-gradient-to-tr from-blue-50 via-blue-100 to-pink-50 rounded-3xl p-8 mb-10 shadow-xl flex flex-col items-center">
        <h2 className="text-3xl font-bold text-primary mb-2">회원가입</h2>
        <p className="text-gray-700 text-center">Smart Healthcare의 다양한 서비스를 경험해보세요!</p>
      </section>

      {/* Form Section */}
      <section className="bg-white rounded-2xl shadow-md p-8 w-full max-w-md mx-auto mb-10">
        <form onSubmit={handleSubmit} className="flex flex-col gap-6">
          {error && (
            <div className="text-red-500 text-sm text-center bg-red-50 p-3 rounded-lg">
              {error}
            </div>
          )}

          <InputField 
            label="이메일" 
            name="email"
            type="email" 
            placeholder="이메일 입력" 
            value={formData.email}
            onChange={handleChange}
            required 
          />
          <InputField 
            label="비밀번호" 
            name="password"
            type="password" 
            placeholder="비밀번호 입력 (6자 이상)" 
            value={formData.password}
            onChange={handleChange}
            required 
          />
          <InputField 
            label="비밀번호 확인" 
            name="confirmPassword"
            type="password" 
            placeholder="비밀번호 재입력" 
            value={formData.confirmPassword}
            onChange={handleChange}
            required 
          />
          
          <Button type="submit" disabled={loading}>
            {loading ? '회원가입 중...' : '회원가입'}
          </Button>
        </form>

        <div className="flex flex-col gap-2 mt-6 w-full">
          <button 
            type="button" 
            onClick={() => handleSocialSignup('Google')} 
            disabled={loading}
            className="w-full py-2 rounded bg-white border border-gray-300 flex items-center justify-center gap-2 hover:bg-gray-50 disabled:opacity-50"
          >
            <img src="/assets/googleLogin.png" alt="구글" className="w-5 h-5" /> 구글로 회원가입/로그인
          </button>
          <button 
            type="button" 
            onClick={() => handleSocialSignup('Kakao')} 
            disabled={loading}
            className="w-full py-2 rounded bg-[#FEE500] text-black flex items-center justify-center gap-2 hover:bg-yellow-200 disabled:opacity-50"
          >
            <img src="/assets/kakaoLogin.png" alt="카카오" className="w-5 h-5" /> 카카오로 회원가입/로그인
          </button>
          <button 
            type="button" 
            onClick={() => handleSocialSignup('Naver')} 
            disabled={loading}
            className="w-full py-2 rounded bg-[#03C75A] text-white flex items-center justify-center gap-2 hover:bg-green-600 disabled:opacity-50"
          >
            <img src="/assets/naverLogin.png" alt="네이버" className="w-5 h-5" /> 네이버로 회원가입/로그인
          </button>
        </div>
      </section>

      {/* 안내/배너 Section */}
      <section className="flex flex-col items-center gap-4">
        <p className="text-lg text-center">회원이 되시면 맞춤형 건강관리, 커뮤니티 등 다양한 혜택을 누릴 수 있습니다.</p>
        
        {/* 로그인 페이지로 돌아가기 */}
        <div className="text-center">
          <p className="text-gray-600 mb-2">이미 계정이 있으신가요?</p>
          <button
            onClick={() => navigate('/login')}
            className="text-blue-600 hover:text-blue-800 underline"
          >
            로그인하기
          </button>
        </div>
      </section>
    </Layout>
  );
}