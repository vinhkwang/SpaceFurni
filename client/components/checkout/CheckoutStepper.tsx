import { getDictionary, type Dictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";

export type CheckoutStep = "delivery" | "payment" | "confirmation";

type CheckoutStepperProps = {
  currentStep: CheckoutStep;
};

type StepDefinition = {
  key: CheckoutStep | "cart";
  label: string;
};

function stepDefinitions(dictionary: Dictionary): StepDefinition[] {
  return [
    { key: "cart", label: dictionary.checkout.stepperCart },
    { key: "delivery", label: dictionary.checkout.stepperDelivery },
    { key: "payment", label: dictionary.checkout.stepperPayment },
    { key: "confirmation", label: dictionary.checkout.stepperConfirmation },
  ];
}

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

const STEP_KEYS: StepDefinition["key"][] = ["cart", "delivery", "payment", "confirmation"];

function activeStepIndex(currentStep: CheckoutStep): number {
  return STEP_KEYS.findIndex((key) => key === currentStep);
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

export async function CheckoutStepper({ currentStep }: CheckoutStepperProps) {
  const dictionary = getDictionary(await getLocale());
  const activeIndex = activeStepIndex(currentStep);
  const definitions = stepDefinitions(dictionary);

  return (
    <div className="flex items-center gap-2.5">
      {definitions.map((definition, index) => {
        const isComplete = index < activeIndex;
        const isCurrent = index === activeIndex;
        const isLastDefinition = index === definitions.length - 1;

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
