"use client";

import Link from "next/link";
import type { CartResponse } from "@/lib/api/types";
import { useDictionary } from "@/lib/i18n/LocaleProvider";

type CheckoutErrorBannerProps = {
  code: string;
  message: string;
  details: Record<string, string> | null;
  cart?: CartResponse;
};

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
  const dictionary = useDictionary();

  if (code === "INSUFFICIENT_STOCK") {
    const productName = insufficientStockProductName(details, cart);
    return (
      <div role="alert" className={bannerClassName()}>
        {alertIcon}
        <div className="flex flex-col gap-1.5">
          <span>
            {productName === null
              ? dictionary.checkout.insufficientStockGeneric
              : dictionary.checkout.insufficientStockNamed(productName)}
          </span>
          <Link
            href={details?.productId ? `/cart?highlightProductId=${details.productId}` : "/cart"}
            className="text-[11px] font-semibold uppercase tracking-[0.12em] underline-offset-2 hover:underline"
          >
            {dictionary.checkout.backToCart}
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
          <span>{dictionary.checkout.orderUpdatedElsewhere}</span>
          <button
            type="button"
            onClick={() => window.location.reload()}
            className="cursor-pointer text-[11px] font-semibold uppercase tracking-[0.12em] underline-offset-2 hover:underline"
          >
            {dictionary.checkout.refresh}
          </button>
        </div>
      </div>
    );
  }

  if (code === "PAYMENT_FAILED") {
    return (
      <div role="alert" className={bannerClassName()}>
        {alertIcon}
        <span>{dictionary.checkout.paymentFailed}</span>
      </div>
    );
  }

  return (
    <div role="alert" className={bannerClassName()}>
      {alertIcon}
      <span>{dictionary.checkout.errorCopy[code] ?? message ?? dictionary.checkout.fallbackErrorCopy}</span>
    </div>
  );
}
