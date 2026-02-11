import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import AboutPage from './pages/AboutPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import BodyAnalysisPage from './pages/BodyAnalysisPage';
import SurveyPage from './pages/SurveyPage';
import RecommendationsPage from './pages/RecommendationsPage';
import RecommendedDietListPage from './pages/RecommendedDietListPage';
import RecommendedWorkoutListPage from './pages/RecommendedWorkoutListPage';
import DietDetailPage from './pages/DietDetailPage';
import WorkoutDetailPage from './pages/WorkoutDetailPage';
import InbodyInputPage from './pages/InbodyInputPage';
import InbodyHistoryPage from './pages/InbodyHistoryPage';
import CalendarPage from './pages/CalendarPage';
import CommunityPage from './pages/CommunityPage';
import CommunityPostDetailPage from './pages/CommunityPostDetailPage';
import CommunityPostEditPage from './pages/CommunityPostEditPage';
import MyPage from './pages/MyPage';
import EvaluationPage from './pages/EvaluationPage';
import SurveyHistoryPage from './pages/SurveyHistoryPage';
import SurveyDetailPage from './pages/SurveyDetailPage';
import HealthHistoryPage from './pages/HealthHistoryPage';
import AdminLoginPage from './pages/AdminLoginPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import AdminUsersPage from './pages/AdminUsersPage';
import AdminStatsPage from './pages/AdminStatsPage';
import AdminSystemPage from './pages/AdminSystemPage';
import AdminCommunityPage from './pages/AdminCommunityPage';
import AdminContentPage from './pages/AdminContentPage';
import AdminLogsPage from './pages/AdminLogsPage';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          {/* 기본 경로를 로그인 페이지로 설정 */}
          <Route path="/" element={<LoginPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          
          {/* 공개 페이지 */}
          <Route path="/main" element={<MainPage />} />
          <Route path="/about" element={<AboutPage />} />
          
          {/* 인증이 필요한 페이지들 - ProtectedRoute로 보호 */}
          <Route path="/body-analysis" element={
            <ProtectedRoute>
              <BodyAnalysisPage />
            </ProtectedRoute>
          } />
          <Route path="/survey" element={
            <ProtectedRoute>
              <SurveyPage />
            </ProtectedRoute>
          } />
          <Route path="/recommendations" element={
            <ProtectedRoute>
              <RecommendationsPage />
            </ProtectedRoute>
          } />
          <Route path="/recommended-diet-list" element={
            <ProtectedRoute>
              <RecommendedDietListPage />
            </ProtectedRoute>
          } />
          <Route path="/recommended-workout-list" element={
            <ProtectedRoute>
              <RecommendedWorkoutListPage />
            </ProtectedRoute>
          } />
          <Route path="/diet-detail/:id" element={
            <ProtectedRoute>
              <DietDetailPage />
            </ProtectedRoute>
          } />
          <Route path="/workout-detail/:id" element={
            <ProtectedRoute>
              <WorkoutDetailPage />
            </ProtectedRoute>
          } />
          <Route path="/inbody-input" element={
            <ProtectedRoute>
              <InbodyInputPage />
            </ProtectedRoute>
          } />
          <Route path="/inbody-history" element={
            <ProtectedRoute>
              <InbodyHistoryPage />
            </ProtectedRoute>
          } />
          <Route path="/calendar" element={
            <ProtectedRoute>
              <CalendarPage />
            </ProtectedRoute>
          } />
          <Route path="/community" element={
            <ProtectedRoute>
              <CommunityPage />
            </ProtectedRoute>
          } />
          <Route path="/community/:id" element={
            <ProtectedRoute>
              <CommunityPostDetailPage />
            </ProtectedRoute>
          } />
          <Route path="/community/edit/:id" element={
            <ProtectedRoute>
              <CommunityPostEditPage />
            </ProtectedRoute>
          } />
          <Route path="/mypage" element={
            <ProtectedRoute>
              <MyPage />
            </ProtectedRoute>
          } />
          <Route path="/evaluation" element={
            <ProtectedRoute>
              <EvaluationPage />
            </ProtectedRoute>
          } />
          <Route path="/survey-history" element={
            <ProtectedRoute>
              <SurveyHistoryPage />
            </ProtectedRoute>
          } />
          <Route path="/survey-detail/:id" element={
            <ProtectedRoute>
              <SurveyDetailPage />
            </ProtectedRoute>
          } />
          <Route path="/health-history" element={
            <ProtectedRoute>
              <HealthHistoryPage />
            </ProtectedRoute>
          } />
          
          {/* 관리자 전용 페이지 */}
          <Route path="/admin/login" element={<AdminLoginPage />} />
          <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
          <Route path="/admin/users" element={<AdminUsersPage />} />
          <Route path="/admin/community" element={<AdminCommunityPage />} />
          <Route path="/admin/content" element={<AdminContentPage />} />
          <Route path="/admin/logs" element={<AdminLogsPage />} />
          <Route path="/admin/stats" element={<AdminStatsPage />} />
          <Route path="/admin/system" element={<AdminSystemPage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;