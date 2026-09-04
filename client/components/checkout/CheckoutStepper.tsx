export type CheckoutStep = "delivery" | "payment" | "confirmation";

type CheckoutStepperProps = {
  currentStep: CheckoutStep;
};

type StepDefinition = {
  key: CheckoutStep | "cart";
  label: string;
};

const STEP_DEFINITIONS: StepDefinition[] = [
  { key: "cart", label: "Cart" },
  { key: "delivery", label: "Delivery" },
  { key: "payment", label: "Payment" },
  { key: "confirmation", label: "Confirmation" },
];

const checkIcon = (
  <svg
    viewBox="0 0 24 24"
    aria-hidden
    className="h-2.5 w-2.5 stroke-current"
    fill="none"
    strokeWidth={3}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="m5 13 4 4L19 7" />
  </svg>
);

function activeStepIndex(currentStep: CheckoutStep): number {
  return STEP_DEFINITIONS.findIndex((definition) => definition.key === currentStep);
}

function circleClassName(isCurrent: boolean, isComplete: boolean): string {
  if (isCurrent) {
    return "flex h-6.5 w-6.5 items-center justify-center rounded-full bg-deep text-[11px] font-semibold text-white";
  }
  if (isComplete) {
    return "flex h-6.5 w-6.5 items-center justify-center rounded-full bg-brass text-[11px] font-semibold text-ink";
  }
  return "flex h-6.5 w-6.5 items-center justify-center rounded-full bg-surface text-[11px] font-semibold text-ink-muted";
}

function labelClassName(isCurrent: boolean): string {
  return isCurrent
    ? "text-[11px] font-semibold uppercase tracking-[0.13em] text-ink"
    : "text-[11px] font-semibold uppercase tracking-[0.13em] text-ink-muted";
}

export function CheckoutStepper({ currentStep }: CheckoutStepperProps) {
  const activeIndex = activeStepIndex(currentStep);

  return (
    <div className="flex items-center gap-2.5">
      {STEP_DEFINITIONS.map((definition, index) => {
        const isComplete = index < activeIndex;
        const isCurrent = index === activeIndex;
        const isLastDefinition = index === STEP_DEFINITIONS.length - 1;

        return (
          <div key={definition.key} className="flex items-center gap-2.5">
            <div className="flex items-center gap-2.5">
              <span
                className={circleClassName(isCurrent, isComplete)}
                aria-current={isCurrent ? "step" : undefined}
              >
                {isComplete ? checkIcon : index + 1}
              </span>
              <span className={labelClassName(isCurrent)}>{definition.label}</span>
            </div>
            {isLastDefinition ? null : <span aria-hidden className="h-px w-9 bg-hairline" />}
          </div>
        );
      })}
    </div>
  );
}
