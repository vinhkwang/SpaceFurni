"use client";

type QuantityStepperProps = {
  value: number;
  onChange: (nextQuantity: number) => void;
  minimum?: number;
  maximum?: number;
};

const stepButtonClassName =
  "flex h-[38px] w-[38px] cursor-pointer items-center justify-center rounded-full transition-colors duration-200 hover:bg-surface disabled:cursor-not-allowed disabled:opacity-35 disabled:hover:bg-transparent";

export function QuantityStepper({
  value,
  onChange,
  minimum = 1,
  maximum,
}: QuantityStepperProps) {
  const canDecrease = value > minimum;
  const canIncrease = typeof maximum !== "number" || value < maximum;

  return (
    <div className="inline-flex h-[54px] items-center gap-0.5 rounded-pill border border-hairline px-1.5">
      <button
        type="button"
        aria-label="Decrease quantity"
        disabled={!canDecrease}
        onClick={() => onChange(value - 1)}
        className={stepButtonClassName}
      >
        <svg
          viewBox="0 0 24 24"
          aria-hidden
          className="h-2.5 w-2.5 stroke-current"
          fill="none"
          strokeWidth={2.5}
          strokeLinecap="round"
        >
          <path d="M5 12h14" />
        </svg>
      </button>
      <span className="w-[34px] text-center text-[14px] font-semibold tabular-nums">
        {value}
      </span>
      <button
        type="button"
        aria-label="Increase quantity"
        disabled={!canIncrease}
        onClick={() => onChange(value + 1)}
        className={stepButtonClassName}
      >
        <svg
          viewBox="0 0 24 24"
          aria-hidden
          className="h-2.5 w-2.5 stroke-current"
          fill="none"
          strokeWidth={2.5}
          strokeLinecap="round"
        >
          <path d="M12 5v14M5 12h14" />
        </svg>
      </button>
    </div>
  );
}
