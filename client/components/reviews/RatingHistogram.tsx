import { Rating } from "@/components/ui/Rating";
import type { RatingHistogramResponse } from "@/lib/api/types";

type RatingHistogramProps = {
  ratingAverage: number | null;
  reviewCount: number;
  histogram: RatingHistogramResponse;
};

export function RatingHistogram({ ratingAverage, reviewCount, histogram }: RatingHistogramProps) {
  if (ratingAverage === null || reviewCount === 0) {
    return <p className="text-[13px] text-ink-soft">No reviews yet — be the first.</p>;
  }

  const maxCount = Math.max(...histogram.counts.map((entry) => entry.count), 1);

  return (
    <div className="flex flex-col gap-5 sm:flex-row sm:items-center sm:gap-12">
      <Rating value={ratingAverage} reviewCount={reviewCount} />
      <div className="flex flex-1 flex-col gap-1.5">
        {histogram.counts.map((entry) => (
          <div key={entry.stars} className="flex items-center gap-3">
            <span className="w-3 text-[11px] text-ink-muted">{entry.stars}</span>
            <div className="h-[6px] flex-1 overflow-hidden rounded-pill bg-hairline-soft">
              <div
                className="h-full rounded-pill bg-brass"
                style={{ width: `${(entry.count / maxCount) * 100}%` }}
              />
            </div>
            <span className="w-6 text-right text-[11px] text-ink-muted">{entry.count}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
