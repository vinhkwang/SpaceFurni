"use client";

import Link from "next/link";
import type { CartResponse } from "@/lib/api/types";

type CheckoutErrorBannerProps = {
  code: string;
  message: string;
  details: Record<string, string> | null;
  cart?: CartResponse;
};

const DEFAULT_ERROR_COPY: Record<string, string> = {
  VALIDATION_FAILED: "Check your delivery details and try again.",
  UNAUTHENTICATED: "Your session has expired. Sign in again to finish checking out.",
  FORBIDDEN: "You don't have permission to do that.",
  RESOURCE_NOT_FOUND: "We couldn't find something we needed. Refresh the page and try again.",
  DUPLICATE_RESOURCE: "That already exists.",
  PROMOTION_NOT_APPLICABLE: "That promo code isn't valid anymore.",
  INTERNAL_ERROR: "Something went wrong on our end. Try again in a moment.",
};

const FALLBACK_ERROR_COPY = "Something went wrong. Try again.";

const alertIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-3.5 w-3.5 shrink-0 stroke-current" fill="none" strokeWidth={2} strokeLinecap="round">
    <path d="M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18z" />
    <path d="M12 8v5" />
    <path d="M12 16h.01" />
  </svg>
);

function bannerClassName(): string {
  return "flex items-start gap-3 rounded-xl bg-terracotta/10 px-4.5 py-3.5 text-[12.5px] text-terracotta";
}

function insufficientStockProductName(details: Record<string, string> | null, cart?: CartResponse): string | null {
  const productId = details?.productId;
  if (productId === undefined || cart === undefined) {
    return null;
  }
  return cart.lines.find((line) => line.productId === productId)?.productName ?? null;
}

export function CheckoutErrorBanner({ code, message, details, cart }: CheckoutErrorBannerProps) {
  if (code === "INSUFFICIENT_STOCK") {
    const productName = insufficientStockProductName(details, cart);
    return (
      <div role="alert" className={bannerClassName()}>
        {alertIcon}
        <div className="flex flex-col gap-1.5">
          <span>
            {productName === null
              ? "One of the items in your cart no longer has enough stock."
              : `"${productName}" no longer has enough stock for the quantity in your cart.`}
          </span>
          <Link
            href={details?.productId ? `/cart?highlightProductId=${details.productId}` : "/cart"}
            className="text-[11px] font-semibold uppercase tracking-[0.12em] underline-offset-2 hover:underline"
          >
            Back to cart
          </Link>
        </div>
      </div>
    );
  }

  if (code === "CONCURRENT_MODIFICATION") {
    return (
      <div role="alert" className={bannerClassName()}>
        {alertIcon}
        <div className="flex flex-col gap-1.5">
          <span>Your order was updated somewhere else while you were checking out.</span>
          <button
            type="button"
            onClick={() => window.location.reload()}
            className="cursor-pointer text-[11px] font-semibold uppercase tracking-[0.12em] underline-offset-2 hover:underline"
          >
            Refresh
          </button>
        </div>
      </div>
    );
  }

  if (code === "PAYMENT_FAILED") {
    return (
      <div role="alert" className={bannerClassName()}>
        {alertIcon}
        <span>Your payment couldn&apos;t be processed. Your cart is untouched — you can try again.</span>
      </div>
    );
  }

  return (
    <div role="alert" className={bannerClassName()}>
      {alertIcon}
      <span>{DEFAULT_ERROR_COPY[code] ?? message ?? FALLBACK_ERROR_COPY}</span>
    </div>
  );
}
