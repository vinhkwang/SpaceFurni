"use client";

import Link from "next/link";
import type { CartResponse } from "@/lib/api/types";
import { useCart } from "@/lib/cart/useCart";
import { CartLineItem } from "@/components/cart/CartLineItem";
import { EmptyCart } from "@/components/cart/EmptyCart";
import { formatMoney } from "@/lib/formatting/formatMoney";

type CartLinesProps = {
  cart: CartResponse;
  highlightProductId?: string;
};

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

function freeShippingNote(amountToFreeShippingAmount: number): string {
  return amountToFreeShippingAmount > 0
    ? `Add ${formatMoney(amountToFreeShippingAmount)} more for free delivery`
    : "You have unlocked free delivery";
}

export function CartLines({ cart, highlightProductId }: CartLinesProps) {
  const { cart: optimisticCart, isMutating, updateQuantity, removeLine } = useCart(cart);

  if (optimisticCart.lines.length === 0) {
    return <EmptyCart />;
  }

  return (
    <div className="flex flex-col gap-3.5">
      {optimisticCart.lines.map((line) => (
        <CartLineItem
          key={line.productId}
          line={line}
          isDisabled={isMutating}
          isHighlighted={line.productId === highlightProductId}
          onQuantityChange={(quantity) => updateQuantity(line.productId, quantity)}
          onRemove={() => removeLine(line.productId)}
        />
      ))}
      <div className="mt-2 flex items-center justify-between">
        <Link href="/" className="flex items-center gap-2.5 text-[11px] font-semibold uppercase tracking-[0.13em]">
          {backArrowIcon}
          Continue shopping
        </Link>
        <div className="text-[12px] text-ink-muted">
          {freeShippingNote(optimisticCart.priceBreakdown.amountToFreeShippingAmount)}
        </div>
      </div>
    </div>
  );
}
