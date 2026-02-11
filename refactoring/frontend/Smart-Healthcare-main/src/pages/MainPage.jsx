// src/pages/MainPage.jsx
import { Link, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import Button from '../components/Button';
import Hero from '../components/Hero';
import { useRef } from 'react';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { useUser } from '../api/auth';

// lucide-react에서 실제 존재하는 아이콘만 import!
import { Dumbbell, PieChart, Utensils, Apple, Salad } from 'lucide-react';

// Swiper import
import { Swiper, SwiperSlide } from 'swiper/react';
import { Autoplay, Pagination } from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/pagination';

function FeatureCard({ icon: Icon, title, desc, cta, onCtaClick }) {
  return (
    <div className="bg-gradient-to-br from-primary/10 to-accent/10 rounded-2xl shadow-fancy p-8 flex flex-col items-center hover:shadow-2xl hover:-translate-y-1 transition duration-300 w-full" data-aos="fade-up">
      <div className="bg-primary/10 rounded-full p-4 mb-5">
        <Icon className="h-9 w-9 text-primary mx-auto" />
      </div>
      <h3 className="text-xl font-bold text-gray-900 mb-2 text-center font-display tracking-tight">{title}</h3>
      <p className="text-gray-600 mb-6 text-center leading-relaxed">{desc}</p>
      <Button onClick={onCtaClick}>{cta.text}</Button>
    </div>
  );
}

export default function MainPage() {
  const { isLoggedIn } = useUser();
  const featureRef = useRef(null);
  const navigate = useNavigate();

  // 인증이 필요한 기능에 대한 클릭 핸들러
  const handleProtectedFeatureClick = (path) => {
    if (isLoggedIn) {
      navigate(path);
    } else {
      navigate('/login');
    }
  };

  return (
    <>
      <Header />
      <Hero scrollToRef={featureRef} />
      {/* 주요 기능 등 나머지 콘텐츠 */}
      <section ref={featureRef} className="space-y-20 mb-24 pt-32 bg-gradient-to-b from-bg-section to-white">
      <h2 className="text-3xl font-extrabold text-center text-primary mb-10 font-display tracking-tight" data-aos="fade-up">What Smart Healthcare Can do</h2>
        <div className="grid md:grid-cols-3 gap-10">
          <FeatureCard
            icon={PieChart}
            title="인바디 데이터 입력"
            desc="근육량, 체지방률, 부위별 분포까지 정확히 입력해보세요."
            cta={{ text: '지금 시작하기' }}
            onCtaClick={() => handleProtectedFeatureClick('/inbody-input')}
          />
          <FeatureCard
            icon={Dumbbell}
            title="체형 분석"
            desc="입력한 데이터를 바탕으로 AI가 체형을 분석합니다."
            cta={{ text: '분석하러 가기' }}
            onCtaClick={() => handleProtectedFeatureClick('/body-analysis')}
          />
          <FeatureCard
            icon={Utensils}
            title="마이페이지"
            desc="개인 대시보드에서 건강 현황과 추천을 한눈에 확인하세요."
            cta={{ text: '마이페이지 가기' }}
            onCtaClick={() => handleProtectedFeatureClick('/mypage')}
          />
        </div>
      </section>
      {/* 서비스 특징 */}
      <section>
        <h2 className="text-3xl font-extrabold text-center text-primary mb-10 font-display tracking-tight" data-aos="fade-up">Why Choose Smart Healthcare</h2>
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-10">
          {[
            { icon: PieChart, text: 'AI 체형 분석' },
            { icon: Dumbbell, text: '맞춤형 운동 루틴' },
            { icon: Utensils, text: '맞춤 식단 제공' },
            { icon: Apple, text: '진행상황 추적' },
            { icon: Dumbbell, text: '전문가 피드백' },
            { icon: Salad, text: '커뮤니티 지원' },
          ].map((f, i) => (
            <div key={f.text} className="bg-white rounded-2xl shadow-fancy p-8 flex flex-col items-center hover:shadow-2xl hover:-translate-y-1 transition duration-300 group cursor-pointer" data-aos="fade-up" data-aos-delay={i*100}>
              <div className="bg-white rounded-full w-12 h-12 flex items-center justify-center shadow mb-3 mx-auto group-hover:bg-primary/10 transition">
                <f.icon className="h-7 w-7 text-primary" />
              </div>
              <h4 className="font-semibold text-gray-900 text-center font-display tracking-tight">{f.text}</h4>
              <p className="text-gray-500 text-sm text-center mt-2 leading-relaxed">
                최신 AI와 전문가가 함께하는 헬스케어 솔루션!
              </p>
            </div>
          ))}
        </div>
      </section>
      <Footer />
    </>
  );
}