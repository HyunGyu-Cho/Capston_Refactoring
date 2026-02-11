import React, { useEffect } from 'react';
import Layout from '../components/Layout';
import AOS from 'aos';
import 'aos/dist/aos.css';

export default function AboutPage() {
  useEffect(() => {
    AOS.init({ duration: 900, once: true });
  }, []);

  return (
    <Layout>
      <div className="bg-[#10162A] min-h-screen w-full">
        {/* Hero/Intro */}
        <section className="py-20 max-w-6xl mx-auto w-full px-4 flex flex-col items-center justify-center text-center text-white" data-aos="fade-up">
          <h1 className="text-4xl md:text-5xl font-extrabold mb-6">스마트 헬스케어의 미래와 비전</h1>
          <p className="text-lg md:text-2xl opacity-80 max-w-2xl mx-auto mb-4 whitespace-pre-line">
            AI와 실시간 데이터 분석을 통해
            누구나 쉽게 건강을 관리하고<br/> 나만의 맞춤형 운동·식단을 추천받는
            새로운 건강관리 패러다임.
          </p>
        </section>

        {/* 미래 전망/신기술 */}
        <section className="py-20 max-w-6xl mx-auto w-full px-4" data-aos="fade-up">
          <h2 className="text-3xl md:text-4xl font-bold text-white text-center mb-12">실시간 분석과 개인별 맞춤 추천</h2>
          <div className="flex flex-wrap justify-center gap-8 mb-8">
            <img src="/assets/ai-doctor.jpg" alt="AI 실시간 건강 분석" className="rounded-2xl w-64 h-64 object-cover shadow-2xl" data-aos="zoom-in" data-aos-delay="100" />
            <img src="/assets/microbiome.jpg" alt="운동 데이터 시각화" className="rounded-2xl w-64 h-64 object-cover shadow-2xl" data-aos="zoom-in" data-aos-delay="200" />
            <img src="/assets/genetherapy.jpg" alt="식단 추천 알고리즘" className="rounded-2xl w-64 h-64 object-cover shadow-2xl" data-aos="zoom-in" data-aos-delay="300" />
            <img src="/assets/robot-rehab.jpg" alt="커뮤니티와 동기부여" className="rounded-2xl w-64 h-64 object-cover shadow-2xl" data-aos="zoom-in" data-aos-delay="400" />
          </div>
          <p className="text-white text-xl text-center opacity-80 max-w-3xl mx-auto">
            스마트 헬스케어는 실시간 인바디·활동 데이터 분석, AI 기반 맞춤 운동·식단 추천, 
            커뮤니티와 전문가 피드백을 결합해 일상 속 건강관리를 혁신합니다.
          </p>
        </section>

        {/* 가치와 비전 */}
        <section className="py-20 max-w-6xl mx-auto w-full px-4 flex flex-col items-center justify-center" data-aos="fade-up">
          <h2 className="text-3xl md:text-4xl font-bold text-white text-center mb-12">스마트 헬스케어의 가치와 비전</h2>
          <div className="w-full grid grid-cols-1 md:grid-cols-4 gap-8">
            {/* 개인화 */}
            <div className="bg-[#151C2F] rounded-2xl shadow-xl p-8 flex flex-col items-center justify-center min-h-[200px]">
              <span className="text-2xl text-white font-bold mb-4">개인화</span>
              <span className="text-white/80 text-center text-lg leading-relaxed">
                AI가 내 데이터에 맞춰<br />운동·식단을 추천
              </span>
            </div>
            {/* 실시간 분석 */}
            <div className="bg-[#151C2F] rounded-2xl shadow-xl p-8 flex flex-col items-center justify-center min-h-[200px]">
              <span className="text-2xl text-white font-bold mb-4">실시간 분석</span>
              <span className="text-white/80 text-center text-lg leading-relaxed">
                인바디·활동 데이터 기반<br />건강 상태 모니터링
              </span>
            </div>
            {/* 커뮤니티 */}
            <div className="bg-[#151C2F] rounded-2xl shadow-xl p-8 flex flex-col items-center justify-center min-h-[200px]">
              <span className="text-2xl text-white font-bold mb-4">커뮤니티</span>
              <span className="text-white/80 text-center text-lg leading-relaxed">
                추천받은 운동/식단<br />정보공유 
              </span>
            </div>
            {/* 일상 건강관리 */}
            <div className="bg-[#151C2F] rounded-2xl shadow-xl p-8 flex flex-col items-center justify-center min-h-[200px]">
              <span className="text-2xl text-white font-bold mb-4">일상 건강관리</span>
              <span className="text-white/80 text-center text-lg leading-relaxed">
                누구나 쉽게 실천하는<br />건강 습관 형성
              </span>
            </div>
          </div>
          <p className="text-white text-xl text-center opacity-80 mt-12 max-w-3xl mx-auto">
            스마트 헬스케어는 AI 및 사용자들의 피드백으로 모두의 건강한 일상을 만들어갑니다.
          </p>
        </section>

        {/* 결론 */}
        <section className="py-16 max-w-6xl mx-auto w-full px-4 flex flex-col items-center justify-center text-center text-white" data-aos="fade-up">
          <h2 className="text-2xl md:text-3xl font-bold mb-6">결론</h2>
          <p className="text-lg md:text-xl opacity-80 max-w-2xl mx-auto mb-4 whitespace-pre-line">
            스마트 헬스케어는 실시간 데이터 분석과 AI 추천을 통해 <br/>
            누구나 쉽고 즐겁게 건강을 관리할 수 있는 세상을 만듭니다.<br/>
            앞으로도 사용자 중심, 데이터 기반의 혁신을 이어가겠습니다.
          </p>
        </section>
      </div>
    </Layout>
  );
} 