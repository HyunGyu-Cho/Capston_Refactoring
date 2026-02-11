import React from 'react';

export default function HeroWithBg({ title, subtitle, height = 'h-[40vh] md:h-[50vh]', children }) {
  return (
    <section
      className={`relative w-screen left-1/2 right-1/2 -ml-[50vw] -mr-[50vw] flex items-center justify-center text-center text-white ${height} overflow-hidden bg-[#10162A]`}
      style={{
        position: 'relative',
        left: '50%',
        right: '50%',
        marginLeft: '-50vw',
        marginRight: '-50vw',
        width: '100vw',
        backgroundColor: '#10162A'
      }}
    >
      <div className="relative z-20 px-6 w-full flex flex-col items-center justify-center">
        <h1 className="text-4xl md:text-5xl font-extrabold mb-3 drop-shadow-lg">{title}</h1>
        {subtitle && (
          <p className="text-lg md:text-2xl whitespace-pre-line opacity-80 mb-2">{subtitle}</p>
        )}
        {children}
      </div>
    </section>
  );
}