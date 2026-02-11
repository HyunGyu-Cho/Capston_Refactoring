import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Settings, 
  AlertTriangle,
  ChevronLeft,
  XCircle
} from 'lucide-react';
import AdminLayout from '../components/AdminLayout';

const AdminSystemPage = () => {
  const navigate = useNavigate();

  const admin = JSON.parse(sessionStorage.getItem('currentAdmin'));
  const token = sessionStorage.getItem('adminToken');

  useEffect(() => {
    if (!admin || !token || admin.role !== 'ADMIN') {
      navigate('/admin/login');
      return;
    }
  }, []);

  return (
    <AdminLayout>
      <div className="max-w-7xl mx-auto p-6">
        {/* Header */}
        <div className="bg-gradient-to-r from-red-600 to-red-800 rounded-3xl text-white p-8 mb-8">
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <Settings className="w-8 h-8" />
                <h1 className="text-3xl font-bold">시스템 관리</h1>
              </div>
              <p className="text-red-100 text-lg">
                시스템 상태 모니터링 및 관리
              </p>
            </div>
            <div className="flex items-center gap-4">
              <button
                onClick={() => navigate('/admin/dashboard')}
                className="bg-red-500 hover:bg-red-400 p-3 rounded-lg transition-colors"
              >
                <ChevronLeft className="w-6 h-6" />
              </button>
            </div>
          </div>
        </div>

        {/* 비활성화 안내 */}
        <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
          <div className="flex justify-center mb-6">
            <div className="bg-red-100 rounded-full p-6">
              <XCircle className="w-16 h-16 text-red-600" />
            </div>
          </div>
          <h2 className="text-3xl font-bold text-gray-900 mb-4">
            시스템 관리 페이지 비활성화
          </h2>
          <p className="text-lg text-gray-600 mb-8 max-w-2xl mx-auto">
            현재 시스템 관리 페이지는 비활성화되어 있습니다.<br/>
            이 기능은 향후 업데이트를 통해 다시 활성화될 예정입니다.
          </p>
          <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-6 mb-8 max-w-2xl mx-auto">
            <div className="flex items-start gap-3">
              <AlertTriangle className="w-6 h-6 text-yellow-600 flex-shrink-0 mt-1" />
              <div className="text-left">
                <h3 className="font-bold text-yellow-800 mb-2">안내사항</h3>
                <p className="text-sm text-yellow-700">
                  시스템 관리 기능은 현재 개발 중이며, 안정화 작업이 완료되면 다시 제공될 예정입니다.<br/>
                  문의사항이 있으시면 관리자에게 연락해 주세요.
                </p>
              </div>
            </div>
          </div>
          <button
            onClick={() => navigate('/admin/dashboard')}
            className="bg-red-600 hover:bg-red-700 text-white font-medium py-3 px-8 rounded-lg transition-colors"
          >
            대시보드로 돌아가기
          </button>
        </div>
      </div>
    </AdminLayout>
  );
};

export default AdminSystemPage;
