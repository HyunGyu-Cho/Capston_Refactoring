import { Link, useNavigate } from 'react-router-dom';
import { useUser, logout } from '../api/auth';

export default function Header() {
  const navigate = useNavigate();
  const { user, isLoggedIn } = useUser(); // React Hook으로 상태 관리

  const handleLogout = () => {
    logout(); // auth.js의 logout 함수만 호출
    navigate('/login'); // 로그아웃 시 로그인 페이지로 이동
  };
  const navItems = [
    { name: '홈', to: '/main' },
    { name: '소개', to: '/about' },
    { name: '인바디 입력', to: '/inbody-input' },
    { name: '인바디 히스토리', to: '/inbody-history' },
    { name: '평가', to: '/evaluation' },
    { name: '커뮤니티', to: '/community' },
    { name: '마이페이지', to: '/mypage' },
  ];

  return (
    <header className="bg-primary text-white shadow fixed top-0 left-0 w-full z-50">
      <div className="container mx-auto flex items-center justify-between px-4 py-3">
        <Link to="/main" className="text-2xl font-bold">Smart Healthcare</Link>
        <nav className="hidden md:flex space-x-6">
          {navItems.map(item => (
            <Link 
              key={item.to} 
              to={item.to} 
              className={`hover:text-secondary transition-colors ${
                item.name === '마이페이지' 
                  ? 'bg-green-700 px-4 py-2 rounded-full font-bold hover:bg-blue-600' 
                  : 'bg-black-700 px-4 py-2 rounded-full font-bold hover:bg-blue-600' 
              }`}
            >
              {item.name === '마이페이지' ? '👤 ' + item.name : item.name}
            </Link>
          ))}
        </nav>
        <div className="flex gap-2">
          {isLoggedIn ? (
            <div className="flex items-center gap-3">
              <span className="text-sm hidden sm:block">
                👋 {user?.email}님
              </span>
              <button 
                onClick={handleLogout}
                className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 transition"
              >
                로그아웃
              </button>
            </div>
          ) : (
            <>
              <Link to="/login" className="bg-blue-900 text-white px-4 py-2 rounded hover:bg-blue-700">로그인</Link>
              <Link to="/signup" className="bg-white text-primary px-4 py-2 rounded border border-primary hover:bg-primary hover:text-white transition">회원가입</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}