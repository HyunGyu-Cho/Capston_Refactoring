import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, LogOut, Home, Settings, Users, BarChart3, MessageSquare, FileText } from 'lucide-react';
import AuthManager from '../utils/authManager';

const AdminHeader = () => {
  const navigate = useNavigate();
  const admin = JSON.parse(sessionStorage.getItem('currentAdmin'));

  const handleLogout = () => {
    if (window.confirm('관리자 로그아웃하시겠습니까?')) {
      AuthManager.logoutAdmin();
      navigate('/admin/login');
    }
  };

  const menuItems = [
    { path: '/admin/dashboard', label: '대시보드', icon: Home },
    { path: '/admin/users', label: '사용자 관리', icon: Users },
    { path: '/admin/community', label: '커뮤니티 관리', icon: MessageSquare },
    { path: '/admin/content', label: '콘텐츠 관리', icon: FileText },
    { path: '/admin/logs', label: '시스템 로그', icon: FileText },
    { path: '/admin/stats', label: '상세 통계', icon: BarChart3 },
    { path: '/admin/system', label: '시스템 관리', icon: Settings }
  ];

  return (
    <header className="bg-gradient-to-r from-red-600 to-red-800 text-white shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* 로고 및 브랜드 */}
          <div className="flex items-center gap-3">
            <Shield className="w-8 h-8" />
            <div>
              <h1 className="text-xl font-bold">Smart Healthcare</h1>
              <p className="text-xs text-red-200">관리자 모드</p>
            </div>
          </div>

          {/* 네비게이션 메뉴 */}
          <nav className="hidden md:flex items-center gap-6">
            {menuItems.map((item) => {
              const Icon = item.icon;
              const isActive = window.location.pathname === item.path;
              return (
                <button
                  key={item.path}
                  onClick={() => navigate(item.path)}
                  className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                    isActive
                      ? 'bg-red-500 text-white'
                      : 'text-red-100 hover:bg-red-500/50 hover:text-white'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span className="text-sm font-medium">{item.label}</span>
                </button>
              );
            })}
          </nav>

          {/* 관리자 정보 및 로그아웃 */}
          <div className="flex items-center gap-4">
            <div className="text-right hidden sm:block">
              <p className="text-sm font-medium">{admin?.email?.split('@')[0] || '관리자'}</p>
              <p className="text-xs text-red-200">{admin?.email}</p>
            </div>
            <button
              onClick={handleLogout}
              className="bg-red-500 hover:bg-red-400 p-2 rounded-lg transition-colors"
              title="로그아웃"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* 모바일 네비게이션 */}
        <div className="md:hidden pb-4">
          <div className="grid grid-cols-2 gap-2">
            {menuItems.map((item) => {
              const Icon = item.icon;
              const isActive = window.location.pathname === item.path;
              return (
                <button
                  key={item.path}
                  onClick={() => navigate(item.path)}
                  className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                    isActive
                      ? 'bg-red-500 text-white'
                      : 'text-red-100 hover:bg-red-500/50 hover:text-white'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span className="text-sm font-medium">{item.label}</span>
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </header>
  );
};

export default AdminHeader;
