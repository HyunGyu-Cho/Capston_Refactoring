export default function StarRating({ value, onChange, disabled }) {
  return (
    <div className="flex gap-1">
      {[1,2,3,4,5].map(star => (
        <button
          key={star}
          type="button"
          className={`text-3xl ${star <= value ? "text-yellow-400" : "text-gray-300"} focus:outline-none`}
          onClick={() => !disabled && onChange(star)}
          aria-label={`${star}점`}
          disabled={disabled}
        >★</button>
      ))}
    </div>
  );
} 