import Link from "next/link";
import type { CartResponse } from "@/lib/api/types";
import { formatMoney } from "@/lib/formatting/formatMoney";
import { getDictionary, type Dictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { PromotionCodeInput } from "@/components/cart/PromotionCodeInput";

type OrderSummaryPanelProps = {
  cart: CartResponse;
  checkoutHref?: string;
};

function totalItemCount(cart: CartResponse): number {
  return cart.lines.reduce((runningTotal, line) => runningTotal + line.quantity, 0);
}

function shippingLabel(dictionary: Dictionary, shippingAmount: number): string {
  return shippingAmount === 0 ? dictionary.cart.free : formatMoney(shippingAmount);
}

function freeShippingNote(dictionary: Dictionary, amountToFreeShippingAmount: number): string {
  return amountToFreeShippingAmount > 0
    ? dictionary.cart.freeShippingNote(formatMoney(amountToFreeShippingAmount))
    : dictionary.cart.freeShippingUnlocked;
}

export async function OrderSummaryPanel({ cart, checkoutHref }: OrderSummaryPanelProps) {
  const dictionary = getDictionary(await getLocale());
  const { priceBreakdown } = cart;

  return (
    <div className="rounded-2xl border border-hairline bg-white px-7 pb-7.5 pt-7">
      <div className="mb-5.5 text-[10.5px] uppercase tracking-[0.18em] text-ink-muted">
        {dictionary.cart.orderSummary}
      </div>

      <div className="flex flex-col gap-3.5 border-b border-hairline pb-5">
        <div className="flex justify-between text-[13px]">
          <span className="text-ink-soft">
            {dictionary.cart.subtotalItems(dictionary.cart.itemCount(totalItemCount(cart)))}
          </span>
          <span className="font-semibold">{formatMoney(priceBreakdown.subtotalAmount)}</span>
        </div>
        <div className="flex justify-between text-[13px]">
          <span className="text-ink-soft">{dictionary.cart.delivery}</span>
          <span className="font-semibold">{shippingLabel(dictionary, priceBreakdown.shippingAmount)}</span>
        </div>
        {priceBreakdown.appliedPromotionCode === null ? null : (
          <div className="flex justify-between text-[13px] text-terracotta">
            <span>{dictionary.cart.promo(priceBreakdown.appliedPromotionCode)}</span>
            <span className="font-semibold">−{formatMoney(priceBreakdown.discountAmount)}</span>
          </div>
        )}
        <div className="flex justify-between text-[13px]">
          <span className="text-ink-soft">{dictionary.cart.assembly}</span>
          <span className="font-semibold text-ink-muted">{dictionary.cart.included}</span>
        </div>
      </div>

      <div className="flex items-baseline justify-between pt-5 pb-4">
        <span className="text-[12px] font-semibold uppercase tracking-[0.14em]">{dictionary.cart.total}</span>
        <span className="text-[23px] font-semibold tracking-[-0.02em]">
          {formatMoney(priceBreakdown.totalAmount)}
        </span>
      </div>

      <div className="mb-4">
        <PromotionCodeInput cart={cart} />
      </div>

      {checkoutHref === undefined ? null : (
        <Link
          href={checkoutHref}
          className="mb-4 flex h-13.5 w-full items-center justify-center gap-3 rounded-pill bg-deep text-[11.5px] font-semibold uppercase tracking-[0.14em] text-white transition-colors duration-300 hover:bg-terracotta"
        >
          {dictionary.cart.proceedToCheckout}
        </Link>
      )}

      <div className="text-[12px] text-ink-muted">
        {freeShippingNote(dictionary, priceBreakdown.amountToFreeShippingAmount)}
      </div>
    </div>
  );
}
