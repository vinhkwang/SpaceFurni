"use client";

import Image from "next/image";
import Link from "next/link";
import type { CartLineResponse } from "@/lib/api/types";
import { QuantityStepper } from "@/components/ui/QuantityStepper";
import { formatMoney } from "@/lib/formatting/formatMoney";

type CartLineItemProps = {
  line: CartLineResponse;
  onQuantityChange: (quantity: number) => void;
  onRemove: () => void;
  isDisabled: boolean;
};

const removeIcon = (
  <svg
    viewBox="0 0 24 24"
    aria-hidden
    className="h-3.5 w-3.5 stroke-current"
    fill="none"
    strokeWidth={1.8}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M4 7h16" />
    <path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2" />
    <path d="M6 7l1 13a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2l1-13" />
  </svg>
);

export function CartLineItem({ line, onQuantityChange, onRemove, isDisabled }: CartLineItemProps) {
  return (
    <div className="flex items-center gap-5.5 rounded-[15px] border border-hairline bg-white px-5.5 py-4.5">
      <Link
        href={`/products/${line.productSlug}`}
        className="relative h-[104px] w-[120px] flex-none rounded-[11px] bg-surface-warm"
      >
        {line.imageUrl === null ? null : (
          <Image
            src={line.imageUrl}
            alt=""
            fill
            sizes="120px"
            className="object-contain p-3 mix-blend-multiply"
          />
        )}
      </Link>

      <div className="flex-1">
        <Link href={`/products/${line.productSlug}`} className="mb-2 block text-[15.5px] font-medium">
          {line.productName}
        </Link>
        <div className="flex items-center gap-3 text-[11.5px] text-ink-muted">
          {line.colorHexCode === null ? null : (
            <>
              <span className="flex items-center gap-1.5">
                <span
                  aria-hidden
                  className="h-3 w-3 rounded-full border border-hairline"
                  style={{ backgroundColor: line.colorHexCode }}
                />
                {line.colorName}
              </span>
              <span aria-hidden className="h-2.5 w-px bg-hairline" />
            </>
          )}
          <span>{formatMoney(line.unitPriceAmount)} each</span>
        </div>
      </div>

      <QuantityStepper value={line.quantity} onChange={onQuantityChange} minimum={1} />

      <div className="w-[132px] flex-none text-right text-[16px] font-semibold">
        {formatMoney(line.lineTotalAmount)}
      </div>

      <button
        type="button"
        onClick={onRemove}
        disabled={isDisabled}
        aria-label={`Remove ${line.productName} from cart`}
        className="flex h-9.5 w-9.5 flex-none items-center justify-center rounded-full text-ink-muted transition-colors duration-250 hover:bg-terracotta/10 hover:text-terracotta disabled:cursor-not-allowed disabled:opacity-50"
      >
        {removeIcon}
      </button>
    </div>
  );
}
