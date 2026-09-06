"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import type { OrderStatus } from "@/lib/api/types";
import { transitionOrderStatusAction } from "@/lib/orders/orderActions";
import { useToast } from "@/components/ui/Toast";

type OrderStatusActionsProps = {
  orderNumber: string;
  currentStatus: OrderStatus;
  version: number;
};

const ALL_ORDER_STATUSES: OrderStatus[] = ["PENDING", "PAID", "PACKING", "DELIVERED", "CANCELLED"];

const STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: "Pending",
  PAID: "Paid",
  PACKING: "Packing",
  DELIVERED: "Delivered",
  CANCELLED: "Cancelled",
};

export function OrderStatusActions({ orderNumber, currentStatus, version }: OrderStatusActionsProps) {
  const router = useRouter();
  const { showToast } = useToast();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [isConflict, setIsConflict] = useState(false);

  async function handleTransition(targetStatus: OrderStatus) {
    setIsSubmitting(true);
    setErrorMessage("");
    setIsConflict(false);

    const result = await transitionOrderStatusAction(orderNumber, targetStatus, version);

    setIsSubmitting(false);
    if (result.success) {
      showToast("Status updated.");
      router.refresh();
      return;
    }
    setErrorMessage(result.errorMessage);
    setIsConflict(result.isConflict);
  }

  return (
    <div className="rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div className="mb-4.5 text-[10.5px] uppercase tracking-[0.18em] text-ink-muted">Update status</div>

      {isConflict ? (
        <div className="mb-4.5 flex items-center justify-between gap-3 rounded-xl bg-terracotta/10 px-4 py-3 text-[12px] text-terracotta">
          <span>This order changed elsewhere.</span>
          <button
            type="button"
            onClick={() => router.refresh()}
            className="cursor-pointer font-semibold underline underline-offset-2"
          >
            Reload
          </button>
        </div>
      ) : errorMessage ? (
        <p role="alert" className="mb-4.5 rounded-xl bg-terracotta/10 px-4 py-3 text-[12px] text-terracotta">
          {errorMessage}
        </p>
      ) : null}

      <div className="grid grid-cols-2 gap-2.5">
        {ALL_ORDER_STATUSES.map((status) => {
          const isCurrentStatus = status === currentStatus;
          return (
            <button
              key={status}
              type="button"
              disabled={isCurrentStatus || isSubmitting}
              onClick={() => handleTransition(status)}
              className={`flex h-11 items-center justify-center rounded-xl border text-[11.5px] font-medium transition-colors duration-200 ${
                isCurrentStatus
                  ? "border-deep bg-deep text-white"
                  : "border-hairline text-ink hover:border-ink-muted disabled:cursor-not-allowed disabled:opacity-50"
              }`}
            >
              {STATUS_LABELS[status]}
            </button>
          );
        })}
      </div>
    </div>
  );
}
