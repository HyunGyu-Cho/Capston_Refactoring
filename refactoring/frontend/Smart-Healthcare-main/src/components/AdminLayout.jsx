import React from 'react';
import AdminHeader from './AdminHeader';

const AdminLayout = ({ children }) => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900">
      <AdminHeader />
      <main className="py-6">
        {children}
      </main>
      
      {/* 관리자 전용 푸터 */}
      <footer className="bg-gray-800 border-t border-gray-700 py-6 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center text-gray-400">
            <p className="text-sm">
              Smart Healthcare 관리자 시스템 | 개발 모드
            </p>
            <p className="text-xs mt-1">
              관리자 전용 페이지입니다. 권한이 없는 사용자의 접근은 제한됩니다.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default AdminLayout;
