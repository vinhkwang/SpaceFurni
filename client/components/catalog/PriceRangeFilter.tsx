"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import type { FormEvent } from "react";
import {
  buildProductListingHref,
  hasActivePriceFilter,
  type ProductListingFilters,
} from "@/lib/catalog/productListingUrl";

type PriceRangeFilterProps = {
  departmentSlug: string;
  filters: ProductListingFilters;
};

const priceFieldClassName =
  "h-10 w-26 rounded-pill border border-hairline bg-white px-3.5 text-[12px] text-ink outline-none transition-colors duration-200 placeholder:text-ink-muted focus:border-terracotta";

function sanitisedPriceBound(rawValue: FormDataEntryValue | null): string | undefined {
  if (typeof rawValue !== "string") {
    return undefined;
  }
  const trimmedValue = rawValue.trim();
  if (trimmedValue === "") {
    return undefined;
  }
  const parsedValue = Number(trimmedValue);
  if (!Number.isFinite(parsedValue) || parsedValue < 0) {
    return undefined;
  }
  return String(parsedValue);
}

export function PriceRangeFilter({ departmentSlug, filters }: PriceRangeFilterProps) {
  const router = useRouter();
  const isPriceFilterActive = hasActivePriceFilter(filters);

  function applyPriceRange(submitEvent: FormEvent<HTMLFormElement>) {
    submitEvent.preventDefault();
    const submittedRange = new FormData(submitEvent.currentTarget);

    router.push(
      buildProductListingHref(departmentSlug, {
        ...filters,
        minPrice: sanitisedPriceBound(submittedRange.get("minPrice")),
        maxPrice: sanitisedPriceBound(submittedRange.get("maxPrice")),
        page: undefined,
      }),
    );
  }

  return (
    <form
      key={`${filters.minPrice ?? ""}-${filters.maxPrice ?? ""}`}
      onSubmit={applyPriceRange}
      className="flex flex-wrap items-center gap-3.5"
    >
      <span className="text-[10.5px] uppercase tracking-[0.16em] text-ink-muted">Price range</span>
      <div className="flex items-center gap-2">
        <input
          name="minPrice"
          inputMode="numeric"
          aria-label="Minimum price"
          placeholder="From"
          defaultValue={filters.minPrice ?? ""}
          className={priceFieldClassName}
        />
        <span aria-hidden className="text-ink-muted">
          –
        </span>
        <input
          name="maxPrice"
          inputMode="numeric"
          aria-label="Maximum price"
          placeholder="To"
          defaultValue={filters.maxPrice ?? ""}
          className={priceFieldClassName}
        />
        <button
          type="submit"
          className="h-10 cursor-pointer rounded-pill bg-deep px-5 text-[10.5px] font-semibold uppercase tracking-[0.13em] text-white transition-colors duration-250 hover:bg-terracotta"
        >
          Apply
        </button>
        {isPriceFilterActive ? (
          <Link
            href={buildProductListingHref(departmentSlug, {
              ...filters,
              minPrice: undefined,
              maxPrice: undefined,
              page: undefined,
            })}
            className="flex h-10 items-center rounded-pill border border-hairline px-4 text-[10.5px] uppercase tracking-[0.1em] transition-colors duration-250 hover:border-deep"
          >
            Clear
          </Link>
        ) : null}
      </div>
    </form>
  );
}
