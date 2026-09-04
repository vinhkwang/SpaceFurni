import Link from "next/link";
import { formatMoney } from "@/lib/formatting/formatMoney";
import { formatDeliveryDate } from "@/lib/formatting/formatDeliveryDate";
import type { OrderResponse } from "@/lib/api/types";

type OrderConfirmationProps = {
  order: OrderResponse;
};

const NEXT_DAY_DELIVERY_OFFSET_DAYS = 1;
const STANDARD_DELIVERY_OFFSET_DAYS = 3;

function deliveryOffsetDays(order: OrderResponse): number {
  return order.deliveryWindow === "NEXT_DAY" ? NEXT_DAY_DELIVERY_OFFSET_DAYS : STANDARD_DELIVERY_OFFSET_DAYS;
}

const checkIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-6.5 w-6.5 stroke-current" fill="none" strokeWidth={2.5} strokeLinecap="round" strokeLinejoin="round">
    <path d="m5 13 4 4L19 7" />
  </svg>
);

export function OrderConfirmation({ order }: OrderConfirmationProps) {
  const deliveryDateLabel = formatDeliveryDate(new Date(order.placedAt), deliveryOffsetDays(order));

  return (
    <div className="rounded-2xl border border-hairline bg-white px-8 py-[70px] text-center">
      <div className="mx-auto mb-6.5 flex h-[78px] w-[78px] items-center justify-center rounded-full bg-deep text-white">
        {checkIcon}
      </div>
      <h2 className="mb-3 text-[27px] font-medium tracking-[-0.015em]">Order placed — thank you</h2>
      <p className="mx-auto mb-7.5 max-w-[420px] text-[13px] leading-[1.7] text-ink-soft">
        We&apos;ve emailed your receipt. Our delivery team will call the morning before they arrive, {deliveryDateLabel}.
      </p>

      <div className="mb-8 inline-flex items-center gap-6.5 rounded-2xl bg-surface px-7.5 py-4.5">
        <div>
          <div className="mb-1.5 text-[10px] uppercase tracking-[0.14em] text-ink-muted">Order</div>
          <div className="text-[14px] font-semibold">#{order.orderNumber}</div>
        </div>
        <span className="h-7.5 w-px bg-hairline" />
        <div>
          <div className="mb-1.5 text-[10px] uppercase tracking-[0.14em] text-ink-muted">Total paid</div>
          <div className="text-[14px] font-semibold">{formatMoney(order.totalAmount)}</div>
        </div>
      </div>

      <div>
        <Link
          href="/"
          className="inline-flex h-[50px] cursor-pointer items-center justify-center rounded-pill bg-deep px-7.5 text-[11.5px] font-semibold uppercase tracking-[0.14em] text-white transition-colors duration-300 hover:bg-terracotta"
        >
          Keep shopping
        </Link>
      </div>
    </div>
  );
}
