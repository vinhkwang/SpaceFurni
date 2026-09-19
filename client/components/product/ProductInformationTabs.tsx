"use client";

import { useState } from "react";
import type { ProductDetailResponse } from "@/lib/api/types";
import { useDictionary } from "@/lib/i18n/LocaleProvider";
import type { Dictionary } from "@/lib/i18n/dictionaries/en";

type ProductInformationTabsProps = {
  product: ProductDetailResponse;
};

type TabKey = "description" | "specifications" | "delivery";

type TabDefinition = {
  key: TabKey;
  label: string;
};

function tabDefinitions(dictionary: Dictionary): TabDefinition[] {
  return [
    { key: "description", label: dictionary.product.tabDescription },
    { key: "specifications", label: dictionary.product.tabSpecifications },
    { key: "delivery", label: dictionary.product.tabDelivery },
  ];
}

const commentIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-3 w-3 stroke-current" fill="none" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 12a8 8 0 1 1-3.4-6.5L21 4l-1 4.5A7.96 7.96 0 0 1 21 12Z" />
  </svg>
);

export function ProductInformationTabs({ product }: ProductInformationTabsProps) {
  const dictionary = useDictionary();
  const [activeTab, setActiveTab] = useState<TabKey>("description");

  return (
    <div>
      <div role="tablist" className="flex gap-8.5 border-b border-hairline">
        {tabDefinitions(dictionary).map((tabDefinition) => {
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
                <div className="mb-2 text-[14px] font-semibold">{dictionary.product.deliveryInfoHeading}</div>
                <p className="text-[13px] leading-[1.75] text-ink-soft">{dictionary.product.deliveryInfoBody}</p>
              </div>
              <div>
                <div className="mb-2 text-[14px] font-semibold">{dictionary.product.assemblyInfoHeading}</div>
                <p className="text-[13px] leading-[1.75] text-ink-soft">{dictionary.product.assemblyInfoBody}</p>
              </div>
              <div>
                <div className="mb-2 text-[14px] font-semibold">{dictionary.product.returnsInfoHeading}</div>
                <p className="text-[13px] leading-[1.75] text-ink-soft">{dictionary.product.returnsInfoBody}</p>
              </div>
            </div>
          ) : null}
        </div>

        <div className="rounded-[16px] bg-surface px-8 py-7.5">
          <div className="mb-4 text-[10.5px] uppercase tracking-[0.2em] text-terracotta">{dictionary.product.needAHand}</div>
          <div className="mb-3 text-[19px] font-medium leading-[1.35]">{dictionary.product.notSureItFits}</div>
          <p className="mb-5.5 text-[12.5px] leading-[1.7] text-ink-soft">{dictionary.product.designerBlurb}</p>
          <button
            type="button"
            disabled
            className="flex h-12 w-full cursor-not-allowed items-center justify-center gap-[11px] rounded-pill border border-hairline text-[11px] font-semibold uppercase tracking-[0.14em] opacity-50"
          >
            {commentIcon}
            {dictionary.product.askADesigner}
          </button>
        </div>
      </div>
    </div>
  );
}
