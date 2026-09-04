import type { CartResponse } from "@/lib/api/types";
import { formatMoney } from "@/lib/formatting/formatMoney";

type OrderSummaryPanelProps = {
  cart: CartResponse;
};

function totalItemCount(cart: CartResponse): number {
  return cart.lines.reduce((runningTotal, line) => runningTotal + line.quantity, 0);
}

function itemCountLabel(itemCount: number): string {
  return itemCount === 1 ? "1 item" : `${itemCount} items`;
}

function shippingLabel(shippingAmount: number): string {
  return shippingAmount === 0 ? "Free" : formatMoney(shippingAmount);
}

function freeShippingNote(amountToFreeShippingAmount: number): string {
  return amountToFreeShippingAmount > 0
    ? `Add ${formatMoney(amountToFreeShippingAmount)} more for free delivery`
    : "You have unlocked free delivery";
}

export function OrderSummaryPanel({ cart }: OrderSummaryPanelProps) {
  const { priceBreakdown } = cart;

  return (
    <div className="rounded-2xl border border-hairline bg-white px-7 pb-7.5 pt-7">
      <div className="mb-5.5 text-[10.5px] uppercase tracking-[0.18em] text-ink-muted">
        Order summary
      </div>

      <div className="flex flex-col gap-3.5 border-b border-hairline pb-5">
        <div className="flex justify-between text-[13px]">
          <span className="text-ink-soft">Subtotal · {itemCountLabel(totalItemCount(cart))}</span>
          <span className="font-semibold">{formatMoney(priceBreakdown.subtotalAmount)}</span>
        </div>
        <div className="flex justify-between text-[13px]">
          <span className="text-ink-soft">Delivery</span>
          <span className="font-semibold">{shippingLabel(priceBreakdown.shippingAmount)}</span>
        </div>
        {priceBreakdown.appliedPromotionCode === null ? null : (
          <div className="flex justify-between text-[13px] text-terracotta">
            <span>Promo {priceBreakdown.appliedPromotionCode}</span>
            <span className="font-semibold">−{formatMoney(priceBreakdown.discountAmount)}</span>
          </div>
        )}
        <div className="flex justify-between text-[13px]">
          <span className="text-ink-soft">Assembly</span>
          <span className="font-semibold text-ink-muted">Included</span>
        </div>
      </div>

      <div className="flex items-baseline justify-between py-5">
        <span className="text-[12px] font-semibold uppercase tracking-[0.14em]">Total</span>
        <span className="text-[23px] font-semibold tracking-[-0.02em]">
          {formatMoney(priceBreakdown.totalAmount)}
        </span>
      </div>

      <div className="text-[12px] text-ink-muted">
        {freeShippingNote(priceBreakdown.amountToFreeShippingAmount)}
      </div>
    </div>
  );
}
