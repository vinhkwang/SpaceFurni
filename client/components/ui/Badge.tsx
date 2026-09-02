import type { ReactNode } from "react";
import type { ProductBadgeResponse } from "@/lib/api/types";

type BadgeVariant = ProductBadgeResponse["variant"];

type BadgeProps = {
  variant: BadgeVariant;
  children: ReactNode;
};

const variantClassNames: Record<BadgeVariant, string> = {
  SALE: "bg-terracotta text-white",
  NEW: "bg-deep text-white",
  BESTSELLER: "bg-brass text-ink",
};

export function Badge({ variant, children }: BadgeProps) {
  return (
    <span
      className={`inline-flex items-center rounded-pill px-[11px] py-[5px] text-[9.5px] font-semibold uppercase tracking-[0.12em] ${variantClassNames[variant]}`}
    >
      {children}
    </span>
  );
}
