// src/components/Layout.jsx
import Header from './Header';
import Footer from './Footer';
import { useLocation } from 'react-router-dom';

export default function Layout({ children }) {
  const location = useLocation();
  // 홈페이지(/main)만 밝은 배경, 그 외는 딥네이비
  const isHome = location.pathname === '/main';
  const bgClass = isHome ? 'bg-[#F3F6FB] min-h-screen' : 'bg-[#10162A] min-h-screen';
  return (
    <div className={bgClass}>
      <Header />
      <main className="flex-1 max-w-7xl mx-auto w-full px-4 pt-24 pb-16">
        {children}
      </main>
      <Footer />
    </div>
  );
}
  