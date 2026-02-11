// src/components/Button.jsx
import { motion } from 'framer-motion';

export default function Button({ children, className = '', ...props }) {
  return (
    <motion.button
      whileHover={{ scale: 1.05 }}
      whileTap={{ scale: 0.95 }}
      transition={{ type: 'spring', stiffness: 300 }}
      className={`bg-primary font-semibold rounded-full py-3 px-8 text-base transition hover:bg-primary/80 shadow-md hover:scale-105 active:scale-95 ${className}`}
      {...props}
    >
      {children}
    </motion.button>
  );
}
