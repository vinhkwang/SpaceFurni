type RatingVariant = "compact" | "full";

type RatingProps = {
  value: number;
  reviewCount?: number | null;
  variant?: RatingVariant;
};

const starOutlinePath =
  "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";

const starPositions = [0, 1, 2, 3, 4];

function filledPercentageForStar(value: number, position: number): number {
  return Math.min(Math.max(value - position, 0), 1) * 100;
}

function ratingLabelFor(value: number, reviewCount?: number | null): string {
  if (typeof reviewCount !== "number") {
    return value.toFixed(1);
  }
  return `${value.toFixed(1)} · ${reviewCount} reviews`;
}

export function Rating({ value, reviewCount, variant = "full" }: RatingProps) {
  if (variant === "compact") {
    return (
      <span className="inline-flex items-center gap-1 text-[10.5px] tracking-[0.06em] text-brass">
        <svg viewBox="0 0 24 24" aria-hidden className="h-[9px] w-[9px] fill-current">
          <path d={starOutlinePath} />
        </svg>
        {value.toFixed(1)}
      </span>
    );
  }

  return (
    <span
      className="inline-flex items-center gap-3.5"
      aria-label={ratingLabelFor(value, reviewCount)}
    >
      <span className="inline-flex items-center gap-1 text-brass" aria-hidden>
        {starPositions.map((position) => (
          <span key={position} className="relative inline-flex h-[11px] w-[11px]">
            <svg viewBox="0 0 24 24" className="h-full w-full fill-hairline">
              <path d={starOutlinePath} />
            </svg>
            <span
              className="absolute inset-y-0 left-0 overflow-hidden"
              style={{ width: `${filledPercentageForStar(value, position)}%` }}
            >
              <svg viewBox="0 0 24 24" className="h-[11px] w-[11px] fill-current">
                <path d={starOutlinePath} />
              </svg>
            </span>
          </span>
        ))}
      </span>
      <span className="text-[12px] text-ink-soft">
        {ratingLabelFor(value, reviewCount)}
      </span>
    </span>
  );
}
