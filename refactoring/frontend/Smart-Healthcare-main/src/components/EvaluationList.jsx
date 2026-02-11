import Card from "./Card";
import Skeleton from "react-loading-skeleton";
import 'react-loading-skeleton/dist/skeleton.css';

export default function EvaluationList({ evaluations, loading }) {
  if (loading) {
    return (
      <div className="flex flex-col gap-4">
        {[...Array(3)].map((_, i) => (
          <Card key={i} className="p-4 bg-white">
            <Skeleton height={24} width={80} />
            <Skeleton height={16} width="60%" />
          </Card>
        ))}
      </div>
    );
  }
  if (!evaluations.length) return <div className="text-gray-400">아직 후기가 없습니다.</div>;
  return (
    <div className="flex flex-col gap-4">
      {evaluations.map((e, i) => (
        <Card key={i} className="p-4 bg-white animate-fade-in">
          <div className="flex items-center gap-2 mb-1">
            <span className="text-yellow-400 text-xl">{'★'.repeat(e.rating)}</span>
            <span className="text-gray-500 text-sm">{e.rating}점</span>
          </div>
          <div className="text-gray-800">{e.comment}</div>
        </Card>
      ))}
    </div>
  );
} 