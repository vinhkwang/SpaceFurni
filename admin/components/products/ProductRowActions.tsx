"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import type { ProductStatus } from "@/lib/api/types";
import { adjustProductStockAction, archiveProductAction } from "@/lib/products/productActions";
import { useToast } from "@/components/ui/Toast";

type ProductRowActionsProps = {
  productId: string;
  status: ProductStatus;
  stockOnHand: number;
};

const iconButtonClassName =
  "flex h-7.5 w-7.5 items-center justify-center rounded-lg border border-hairline text-ink-muted transition-colors duration-200 hover:border-deep hover:text-ink disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:border-hairline disabled:hover:text-ink-muted";

function MinusIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-3 w-3 stroke-current" fill="none" strokeWidth={2.2} strokeLinecap="round">
      <path d="M5 12h14" />
    </svg>
  );
}

function PlusIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-3 w-3 stroke-current" fill="none" strokeWidth={2.2} strokeLinecap="round">
      <path d="M12 5v14M5 12h14" />
    </svg>
  );
}

function ArchiveIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-3 w-3 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <rect x="4" y="5" width="16" height="4" rx="1" />
      <path d="M6 9v8a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V9" />
      <path d="M10 13h4" />
    </svg>
  );
}

export function ProductRowActions({ productId, status, stockOnHand }: ProductRowActionsProps) {
  const router = useRouter();
  const { showToast } = useToast();
  const [isPending, setIsPending] = useState(false);
  const isArchived = status === "ARCHIVED";

  async function adjustStock(delta: number) {
    setIsPending(true);
    const result = await adjustProductStockAction(productId, delta);
    setIsPending(false);
    if (result.success) {
      showToast(delta > 0 ? "Stock increased." : "Stock decreased.");
      router.refresh();
    }
  }

  async function archive() {
    setIsPending(true);
    const result = await archiveProductAction(productId);
    setIsPending(false);
    if (result.success) {
      showToast("Product archived.");
      router.refresh();
    }
  }

  return (
    <div className="flex items-center justify-end gap-1.5">
      <button
        type="button"
        disabled={isPending || stockOnHand <= 0}
        onClick={() => adjustStock(-1)}
        aria-label="Decrease stock"
        className={iconButtonClassName}
      >
        <MinusIcon />
      </button>
      <button
        type="button"
        disabled={isPending}
        onClick={() => adjustStock(1)}
        aria-label="Increase stock"
        className={iconButtonClassName}
      >
        <PlusIcon />
      </button>
      <button
        type="button"
        disabled={isPending || isArchived}
        onClick={archive}
        aria-label="Archive product"
        className={iconButtonClassName}
      >
        <ArchiveIcon />
      </button>
    </div>
  );
}
