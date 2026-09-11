import type { OrderTimelineStepResponse } from "@/lib/api/types";
import { formatOrderPlacedAt } from "@/lib/orders/formatOrderPlacedAt";

type OrderTimelineProps = {
  steps: OrderTimelineStepResponse[];
};

const checkIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-2.5 w-2.5 stroke-current" fill="none" strokeWidth={3} strokeLinecap="round" strokeLinejoin="round">
    <path d="m5 13 4 4L19 7" />
  </svg>
);

function formatStepDetail(detail: string): string {
  const placedAtParts = formatOrderPlacedAt(detail);
  return `${placedAtParts.date} · ${placedAtParts.time}`;
}

export function OrderTimeline({ steps }: OrderTimelineProps) {
  return (
    <div className="rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div className="mb-6 text-[15px] font-semibold text-ink">Progress</div>
      {steps.map((step, stepIndex) => {
        const isLastStep = stepIndex === steps.length - 1;
        return (
          <div key={step.label} className="grid grid-cols-[28px_1fr] gap-4">
            <div className="flex flex-col items-center">
              <span
                className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-pill ${
                  step.complete ? "bg-deep text-white" : "border border-hairline text-ink-muted/50"
                }`}
              >
                {checkIcon}
              </span>
              {!isLastStep ? <span className={`w-0.5 flex-1 ${step.complete ? "bg-deep" : "bg-hairline"}`} /> : null}
            </div>
            <div className={isLastStep ? "" : "pb-5"}>
              <div className={`text-[13px] font-medium ${step.complete ? "text-ink" : "text-ink-muted"}`}>
                {step.label}
              </div>
              {step.detail ? (
                <div className="mt-1 text-[11.5px] text-ink-muted">{formatStepDetail(step.detail)}</div>
              ) : null}
            </div>
          </div>
        );
      })}
    </div>
  );
}
