import Link from "next/link";
import { apiFetch } from "@/lib/api/apiClient";
import type { CartResponse } from "@/lib/api/types";
import { Container } from "@/components/ui/Container";
import { CheckoutStepper, type CheckoutStep } from "@/components/checkout/CheckoutStepper";
import { DeliveryDetailsForm } from "@/components/checkout/DeliveryDetailsForm";
import { PaymentMethodSelector } from "@/components/checkout/PaymentMethodSelector";
import { PlaceOrderAction } from "@/components/checkout/PlaceOrderAction";
import { OrderSummaryPanel } from "@/components/cart/OrderSummaryPanel";

export const metadata = {
  title: "Checkout",
};

const CHECKOUT_STEPS = ["delivery", "payment", "confirmation"] as const satisfies readonly CheckoutStep[];

function toCheckoutStep(rawStep: string | string[] | undefined): CheckoutStep {
  const requestedStep = Array.isArray(rawStep) ? rawStep[0] : rawStep;
  return CHECKOUT_STEPS.find((step) => step === requestedStep) ?? "delivery";
}

function stepTitle(step: CheckoutStep): string {
  if (step === "delivery") {
    return "Delivery";
  }
  if (step === "payment") {
    return "Payment";
  }
  return "Order confirmed";
}

function stepContent(step: CheckoutStep, cart: CartResponse) {
  if (step === "delivery") {
    return <DeliveryDetailsForm />;
  }
  if (step === "payment") {
    return (
      <div className="flex flex-col gap-3.5">
        <PaymentMethodSelector />
        <PlaceOrderAction cart={cart} />
      </div>
    );
  }
  return <PlaceOrderAction cart={cart} />;
}

function backHref(step: CheckoutStep): string | null {
  if (step === "delivery") {
    return "/cart";
  }
  if (step === "payment") {
    return "/checkout?step=delivery";
  }
  return null;
}

const backArrowIcon = (
  <svg
    viewBox="0 0 24 24"
    aria-hidden
    className="h-2.5 w-2.5 stroke-current"
    fill="none"
    strokeWidth={2.5}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M19 12H5M12 19l-7-7 7-7" />
  </svg>
);

export default async function CheckoutPage(props: PageProps<"/checkout">) {
  const resolvedSearchParams = await props.searchParams;
  const step = toCheckoutStep(resolvedSearchParams.step);

  const cart = await apiFetch<CartResponse>("/cart", { cache: "no-store" });
  const backLink = backHref(step);
  const isConfirmationStep = step === "confirmation";
  const showSummary = !isConfirmationStep && cart.lines.length > 0;

  return (
    <main className="py-8.5">
      <Container>
        <div className="mb-8.5 flex flex-wrap items-center justify-between gap-6">
          <h1 className="text-[38px] font-medium tracking-[-0.02em]">{stepTitle(step)}</h1>
          <CheckoutStepper currentStep={step} />
        </div>

        <div
          className={
            showSummary
              ? "grid grid-cols-1 items-start gap-6.5 lg:grid-cols-[1fr_400px]"
              : "grid grid-cols-1"
          }
        >
          <div className="flex flex-col gap-3.5">
            {stepContent(step, cart)}

            {backLink === null ? null : (
              <Link
                href={backLink}
                className="flex items-center gap-2.5 text-[11px] font-semibold uppercase tracking-[0.13em]"
              >
                {backArrowIcon}
                Back a step
              </Link>
            )}
          </div>

          {showSummary ? (
            <div className="lg:sticky lg:top-5">
              <OrderSummaryPanel cart={cart} />
            </div>
          ) : null}
        </div>
      </Container>
    </main>
  );
}
