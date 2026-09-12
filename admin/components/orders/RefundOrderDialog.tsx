"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import type { OrderStatus } from "@/lib/api/types";
import { processRefundAction } from "@/lib/orders/orderActions";
import { formatMoney } from "@/lib/formatting/formatMoney";
import { useToast } from "@/components/ui/Toast";

type RefundOrderDialogProps = {
  orderNumber: string;
  status: OrderStatus;
  totalAmount: number;
};

export function RefundOrderDialog({ orderNumber, status, totalAmount }: RefundOrderDialogProps) {
  const router = useRouter();
  const { showToast } = useToast();
  const [isOpen, setIsOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  if (status !== "CANCELLED") {
    return null;
  }

  async function handleConfirm() {
    setIsSubmitting(true);
    setErrorMessage("");

    const result = await processRefundAction(orderNumber, totalAmount);

    setIsSubmitting(false);
    if (!result.success) {
      setErrorMessage(result.errorMessage);
      return;
    }
    setIsOpen(false);
    showToast("Refund processed.");
    router.refresh();
  }

  return (
    <div className="rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div className="mb-4.5 text-[10.5px] uppercase tracking-[0.18em] text-ink-muted">Refund</div>
      <button
        type="button"
        onClick={() => setIsOpen(true)}
        className="flex h-11 w-full items-center justify-center rounded-xl border border-hairline text-[11.5px] font-medium text-ink transition-colors duration-200 hover:border-terracotta hover:text-terracotta"
      >
        Process refund
      </button>

      {isOpen ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-deep/40 px-6">
          <div className="w-full max-w-[400px] rounded-2xl bg-white p-7">
            <div className="mb-2 text-[15px] font-semibold text-ink">Refund this order?</div>
            <p className="mb-5 text-[12.5px] text-ink-soft">
              {formatMoney(totalAmount)} will be refunded to the customer through the original payment method.
            </p>
            {errorMessage ? (
              <p role="alert" className="mb-4 rounded-xl bg-terracotta/10 px-4 py-3 text-[12px] text-terracotta">
                {errorMessage}
              </p>
            ) : null}
            <div className="flex gap-2.5">
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                disabled={isSubmitting}
                className="flex h-11 flex-1 items-center justify-center rounded-xl border border-hairline text-[11.5px] font-medium text-ink-soft disabled:cursor-not-allowed disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleConfirm}
                disabled={isSubmitting}
                className="flex h-11 flex-1 items-center justify-center rounded-xl bg-deep text-[11.5px] font-medium text-white transition-colors duration-200 hover:bg-terracotta disabled:cursor-not-allowed disabled:opacity-50"
              >
                {isSubmitting ? "Refunding…" : "Confirm refund"}
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
