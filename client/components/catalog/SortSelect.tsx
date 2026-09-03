import Link from "next/link";
import {
  buildProductListingHref,
  type ProductListingFilters,
} from "@/lib/catalog/productListingUrl";

type SortOption = {
  key: string;
  label: string;
};

type SortSelectProps = {
  departmentSlug: string;
  activeSortKey: string;
  filters: ProductListingFilters;
};

const sortOptions: SortOption[] = [
  { key: "newest", label: "Newest" },
  { key: "rating", label: "Top rated" },
  { key: "priceAsc", label: "Price ↑" },
  { key: "priceDesc", label: "Price ↓" },
];

const optionClassName =
  "flex h-10 items-center rounded-pill border px-4.5 text-[11.5px] transition duration-250";

export function SortSelect({ departmentSlug, activeSortKey, filters }: SortSelectProps) {
  return (
    <div className="flex flex-wrap items-center gap-2.5">
      <span
        id="product-sort-label"
        className="text-[10.5px] uppercase tracking-[0.16em] text-ink-muted"
      >
        Sort by
      </span>
      <div aria-labelledby="product-sort-label" role="group" className="flex flex-wrap gap-2.5">
        {sortOptions.map((sortOption) => {
          const isActive = sortOption.key === activeSortKey;

          return (
            <Link
              key={sortOption.key}
              href={buildProductListingHref(departmentSlug, {
                ...filters,
                sort: sortOption.key,
                page: undefined,
              })}
              aria-current={isActive ? "true" : undefined}
              className={`${optionClassName} ${
                isActive
                  ? "border-deep bg-deep text-white"
                  : "border-hairline bg-white hover:border-ink-muted"
              }`}
            >
              {sortOption.label}
            </Link>
          );
        })}
      </div>
    </div>
  );
}
