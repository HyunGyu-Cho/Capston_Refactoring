// src/components/Card.jsx
export default function Card({ title, children, className = '' }) {
  return (
    <div
      className={`bg-gradient-to-br from-primary/10 to-accent/10 rounded-2xl shadow-fancy p-8 flex flex-col items-center hover:shadow-2xl hover:-translate-y-1 transition duration-300 ${className}`}
      data-aos="fade-up"
    >
      {title && <h3 className="text-lg font-bold text-gray-900 mb-2">{title}</h3>}
      {children}
    </div>
  );
}