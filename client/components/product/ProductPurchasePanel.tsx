"use client";

import { useState } from "react";
import type { ProductDetailResponse } from "@/lib/api/types";
import { Price } from "@/components/ui/Price";
import { Rating } from "@/components/ui/Rating";
import { formatMoney } from "@/lib/formatting/formatMoney";
import { formatDeliveryDate } from "@/lib/formatting/formatDeliveryDate";

type ProductPurchasePanelProps = {
  product: ProductDetailResponse;
};

const minusIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-2.5 w-2.5 stroke-current" fill="none" strokeWidth={2.5} strokeLinecap="round">
    <path d="M5 12h14" />
  </svg>
);

const plusIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-2.5 w-2.5 stroke-current" fill="none" strokeWidth={2.5} strokeLinecap="round">
    <path d="M12 5v14M5 12h14" />
  </svg>
);

const bagIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-3 w-3 stroke-current" fill="none" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round">
    <path d="M6 8h12l-1 12H7L6 8Z" />
    <path d="M9 8V6a3 3 0 0 1 6 0v2" />
  </svg>
);

const truckIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="mt-0.5 h-3.5 w-3.5 flex-none stroke-current text-terracotta" fill="none" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round">
    <path d="M2 7h11v10H2zM13 11h4l4 4v2h-8z" />
    <circle cx="6.5" cy="18.5" r="1.5" />
    <circle cx="16.5" cy="18.5" r="1.5" />
  </svg>
);

const returnIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="mt-0.5 h-3 w-3 flex-none stroke-current text-terracotta" fill="none" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round">
    <path d="M3 12a9 9 0 1 0 3-6.7L3 8" />
    <path d="M3 3v5h5" />
  </svg>
);

const storeIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="mt-0.5 h-3 w-3 flex-none stroke-current text-terracotta" fill="none" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round">
    <path d="M3 9 4 4h16l1 5" />
    <path d="M4 9v11h16V9" />
    <path d="M9 20v-6h6v6" />
  </svg>
);

function saveAmountLabel(priceAmount: number, compareAtPriceAmount: number): string {
  return `Save ${formatMoney(compareAtPriceAmount - priceAmount)}`;
}

export function ProductPurchasePanel({ product }: ProductPurchasePanelProps) {
  const [selectedColorIndex, setSelectedColorIndex] = useState(0);
  const [quantity, setQuantity] = useState(1);

  const isInStock = product.availableQuantity > 0;
  const deliveryDate = formatDeliveryDate(new Date());

  function decrementQuantity() {
    setQuantity((currentQuantity) => Math.max(1, currentQuantity - 1));
  }

  function incrementQuantity() {
    setQuantity((currentQuantity) => Math.min(product.availableQuantity, currentQuantity + 1));
  }

  return (
    <div className="flex flex-col gap-5">
      <div>
        <div className="mb-3 text-[10.5px] uppercase tracking-[0.2em] text-terracotta">
          {product.categoryName}
        </div>
        <h1 className="mb-3.5 text-[34px] font-medium leading-[1.12] tracking-[-0.02em] text-pretty">
          {product.name}
        </h1>
        <div className="flex items-center gap-3.5">
          {product.ratingAverage === null ? null : (
            <Rating value={product.ratingAverage} reviewCount={product.reviewCount} />
          )}
          <span aria-hidden className="h-3 w-px bg-hairline" />
          <span className="text-[12px] text-ink-soft">{product.stockLabel}</span>
        </div>
      </div>

      <div className="flex flex-wrap items-baseline gap-x-3 gap-y-2 whitespace-nowrap border-y border-hairline py-5">
        <Price amount={product.priceAmount} compareAtAmount={product.compareAtPriceAmount} size="large" />
        {product.compareAtPriceAmount === null ? null : (
          <span className="rounded-pill bg-terracotta/10 px-[11px] py-[5px] text-[10.5px] font-semibold uppercase tracking-[0.1em] text-terracotta">
            {saveAmountLabel(product.priceAmount, product.compareAtPriceAmount)}
          </span>
        )}
        <span className="ml-auto text-[11.5px] text-ink-muted">VAT included</span>
      </div>

      <p className="text-[13.5px] leading-[1.75] text-ink-soft">{product.shortDescription}</p>

      {product.colorSwatchHexCodes.length === 0 ? null : (
        <div>
          <div className="mb-3 text-[10.5px] uppercase tracking-[0.16em] text-ink-muted">
            Finish — {product.primaryColorName}
          </div>
          <div className="flex gap-2.5">
            {product.colorSwatchHexCodes.map((hexCode, colorIndex) => {
              const isSelected = colorIndex === selectedColorIndex;

              return (
                <button
                  key={hexCode}
                  type="button"
                  onClick={() => setSelectedColorIndex(colorIndex)}
                  aria-label={`Select finish ${colorIndex + 1} of ${product.colorSwatchHexCodes.length}`}
                  aria-current={isSelected ? "true" : undefined}
                  className={`flex h-10 w-10 items-center justify-center rounded-full transition duration-250 ${
                    isSelected ? "ring-2 ring-ink ring-offset-2 ring-offset-white" : ""
                  }`}
                >
                  <span
                    className="h-7.5 w-7.5 rounded-full border border-hairline"
                    style={{ backgroundColor: hexCode }}
                  />
                </button>
              );
            })}
          </div>
        </div>
      )}

      <div className="flex items-stretch gap-3">
        <div className="flex h-13.5 items-center gap-0.5 rounded-pill border border-hairline px-1.5">
          <button
            type="button"
            onClick={decrementQuantity}
            disabled={!isInStock || quantity <= 1}
            aria-label="Decrease quantity"
            className="flex h-9.5 w-9.5 items-center justify-center rounded-full transition duration-200 hover:bg-surface disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent"
          >
            {minusIcon}
          </button>
          <span className="w-8.5 text-center text-[14px] font-semibold">{quantity}</span>
          <button
            type="button"
            onClick={incrementQuantity}
            disabled={!isInStock || quantity >= product.availableQuantity}
            aria-label="Increase quantity"
            className="flex h-9.5 w-9.5 items-center justify-center rounded-full transition duration-200 hover:bg-surface disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent"
          >
            {plusIcon}
          </button>
        </div>
        <button
          type="button"
          disabled
          className="flex h-13.5 flex-1 cursor-not-allowed items-center justify-center gap-3 rounded-pill bg-deep text-[11.5px] font-semibold uppercase tracking-[0.14em] text-white opacity-50"
        >
          {bagIcon}
          {isInStock ? "Add to cart" : "Out of stock"}
        </button>
      </div>

      <div className="flex flex-col gap-3.5 rounded-[14px] bg-surface px-5.5 py-5">
        <div className="flex items-start gap-3.5">
          {truckIcon}
          <div>
            <div className="mb-[3px] text-[12.5px] font-semibold">Free delivery in Hanoi</div>
            <div className="text-[11.5px] text-ink-muted">Arrives {deliveryDate} · carried in and assembled</div>
          </div>
        </div>
        <div className="flex items-start gap-3.5">
          {returnIcon}
          <div>
            <div className="mb-[3px] text-[12.5px] font-semibold">30-day returns</div>
            <div className="text-[11.5px] text-ink-muted">Change your mind at home — we collect it for free</div>
          </div>
        </div>
        <div className="flex items-start gap-3.5">
          {storeIcon}
          <div>
            <div className="mb-[3px] text-[12.5px] font-semibold">See it in the showroom</div>
            <div className="text-[11.5px] text-ink-muted">15 & 17 Ha Ke Tan, open until 18:00 today</div>
          </div>
        </div>
      </div>
    </div>
  );
}
