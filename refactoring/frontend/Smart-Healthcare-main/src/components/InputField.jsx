// src/components/InputField.jsx
export default function InputField({ label, ...props }) {
  return (
    <div className="flex flex-col gap-1">
      <label className="text-gray-800 font-semibold text-base mb-1 tracking-wide">{label}</label>
      <input
        className="px-4 py-2 border border-blue-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary shadow-sm transition duration-200 hover:border-primary focus:border-primary"
        {...props}
      />
    </div>
  );
}