"use client";

import { useState } from "react";
import type { ProductDetailResponse } from "@/lib/api/types";

type ProductInformationTabsProps = {
  product: ProductDetailResponse;
};

type TabKey = "description" | "specifications" | "delivery";

type TabDefinition = {
  key: TabKey;
  label: string;
};

const tabDefinitions: TabDefinition[] = [
  { key: "description", label: "Description" },
  { key: "specifications", label: "Specifications" },
  { key: "delivery", label: "Delivery" },
];

const commentIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-3 w-3 stroke-current" fill="none" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 12a8 8 0 1 1-3.4-6.5L21 4l-1 4.5A7.96 7.96 0 0 1 21 12Z" />
  </svg>
);

export function ProductInformationTabs({ product }: ProductInformationTabsProps) {
  const [activeTab, setActiveTab] = useState<TabKey>("description");

  return (
    <div>
      <div role="tablist" className="flex gap-8.5 border-b border-hairline">
        {tabDefinitions.map((tabDefinition) => {
          const isActive = tabDefinition.key === activeTab;

          return (
            <button
              key={tabDefinition.key}
              type="button"
              role="tab"
              aria-selected={isActive}
              onClick={() => setActiveTab(tabDefinition.key)}
              className={`-mb-px border-b-2 pb-4 text-[12px] font-semibold uppercase tracking-[0.13em] transition-colors duration-250 ${
                isActive ? "border-terracotta text-ink" : "border-transparent text-ink-muted hover:text-ink"
              }`}
            >
              {tabDefinition.label}
            </button>
          );
        })}
      </div>

      <div className="grid grid-cols-1 items-start gap-[70px] pt-[34px] lg:grid-cols-[1fr_440px]">
        <div>
          {activeTab === "description" ? (
            <p className="max-w-[640px] text-[14px] leading-[1.8] text-ink-soft">{product.longDescription}</p>
          ) : null}

          {activeTab === "specifications" ? (
            <div className="max-w-[640px] border-t border-hairline-soft">
              {product.specifications.map((specification) => (
                <div
                  key={specification.key}
                  className="grid grid-cols-[190px_1fr] gap-5 border-b border-hairline-soft py-4"
                >
                  <span className="text-[11px] uppercase tracking-[0.14em] text-ink-muted">
                    {specification.key}
                  </span>
                  <span className="text-[13px] text-ink">{specification.value}</span>
                </div>
              ))}
            </div>
          ) : null}

          {activeTab === "delivery" ? (
            <div className="flex max-w-[640px] flex-col gap-5.5">
              <div>
                <div className="mb-2 text-[14px] font-semibold">Delivery</div>
                <p className="text-[13px] leading-[1.75] text-ink-soft">
                  Free two-person delivery inside a 10 km radius of our Thanh Xuan showroom, Monday to
                  Saturday. Outside Hanoi, we quote by district before we charge you — usually
                  250.000–600.000 ₫.
                </p>
              </div>
              <div>
                <div className="mb-2 text-[14px] font-semibold">Assembly</div>
                <p className="text-[13px] leading-[1.75] text-ink-soft">
                  Sofas and beds arrive assembled. Shelving and desks are assembled in your room and the
                  packaging leaves with our team.
                </p>
              </div>
              <div>
                <div className="mb-2 text-[14px] font-semibold">Returns</div>
                <p className="text-[13px] leading-[1.75] text-ink-soft">
                  30 days, no reason needed, as long as the piece is undamaged. Custom finishes are made to
                  order and can&apos;t be returned.
                </p>
              </div>
            </div>
          ) : null}
        </div>

        <div className="rounded-[16px] bg-surface px-8 py-7.5">
          <div className="mb-4 text-[10.5px] uppercase tracking-[0.2em] text-terracotta">Need a hand?</div>
          <div className="mb-3 text-[19px] font-medium leading-[1.35]">Not sure it fits your room?</div>
          <p className="mb-5.5 text-[12.5px] leading-[1.7] text-ink-soft">
            Send a photo and your wall measurements. A designer replies with a scaled layout within a day —
            free, no obligation.
          </p>
          <button
            type="button"
            disabled
            className="flex h-12 w-full cursor-not-allowed items-center justify-center gap-[11px] rounded-pill border border-hairline text-[11px] font-semibold uppercase tracking-[0.14em] opacity-50"
          >
            {commentIcon}
            Ask a designer
          </button>
        </div>
      </div>
    </div>
  );
}
