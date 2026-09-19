"use client";

import { useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { cancelOrderAction } from "@/lib/orders/cancelOrderAction";
import { useDictionary } from "@/lib/i18n/LocaleProvider";

type CancelOrderButtonProps = {
  orderId: string;
};

type CancelStep = "idle" | "confirming";

export function CancelOrderButton({ orderId }: CancelOrderButtonProps) {
  const router = useRouter();
  const dictionary = useDictionary();
  const [step, setStep] = useState<CancelStep>("idle");
  const [reason, setReason] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, startSubmission] = useTransition();

  function confirmCancellation(): void {
    if (reason.trim() === "") {
      setErrorMessage(dictionary.orders.tellUsWhyCancelling);
      return;
    }
    startSubmission(async () => {
      const result = await cancelOrderAction(orderId, reason.trim());
      if (!result.success) {
        setErrorMessage(result.errorMessage);
        return;
      }
      router.refresh();
    });
  }

  if (step === "idle") {
    return (
      <button
        type="button"
        onClick={() => setStep("confirming")}
        className="flex h-[46px] w-full cursor-pointer items-center justify-center rounded-pill border border-hairline text-[11px] font-semibold uppercase tracking-[0.13em] text-ink transition-colors duration-300 hover:border-terracotta hover:text-terracotta"
      >
        {dictionary.orders.cancelOrder}
      </button>
    );
  }

  return (
    <div className="flex flex-col gap-3 rounded-2xl border border-hairline bg-surface p-4.5">
      <p className="text-[12.5px] font-medium text-ink">{dictionary.orders.areYouSureCancel}</p>
      <textarea
        value={reason}
        onChange={(event) => setReason(event.target.value)}
        placeholder={dictionary.orders.reasonForCancellingPlaceholder}
        rows={3}
        className="w-full rounded-xl border border-hairline bg-white px-3.5 py-3 text-[12.5px] text-ink outline-none focus:border-terracotta"
      />
      {errorMessage === null ? null : <p className="text-[11.5px] text-terracotta">{errorMessage}</p>}
      <div className="flex gap-2.5">
        <button
          type="button"
          onClick={() => {
            setStep("idle");
            setErrorMessage(null);
          }}
          disabled={isSubmitting}
          className="flex h-[44px] flex-1 cursor-pointer items-center justify-center rounded-pill border border-hairline text-[11px] font-semibold uppercase tracking-[0.13em] text-ink-soft disabled:cursor-not-allowed disabled:opacity-50"
        >
          {dictionary.orders.neverMind}
        </button>
        <button
          type="button"
          onClick={confirmCancellation}
          disabled={isSubmitting}
          className="flex h-[44px] flex-1 cursor-pointer items-center justify-center rounded-pill bg-deep text-[11px] font-semibold uppercase tracking-[0.13em] text-white transition-colors duration-300 hover:bg-terracotta disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isSubmitting ? dictionary.orders.cancelling : dictionary.orders.confirmCancellation}
        </button>
      </div>
    </div>
  );
}
