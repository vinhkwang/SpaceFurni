import Image from "next/image";
import Link from "next/link";
import type { ProductSummaryResponse } from "@/lib/api/types";
import { formatMoney } from "@/lib/formatting/formatMoney";

type SearchSuggestionsProps = {
  suggestions: ProductSummaryResponse[];
  activeIndex: number;
  onSelect: () => void;
};

export function SearchSuggestions({ suggestions, activeIndex, onSelect }: SearchSuggestionsProps) {
  return (
    <div
      id="search-suggestions-listbox"
      role="listbox"
      aria-label="Search suggestions"
      className="absolute inset-x-0 top-[54px] z-60 rounded-2xl border border-hairline-soft bg-white p-2 shadow-2xl"
    >
      {suggestions.length === 0 ? (
        <p className="px-3 py-4 text-[12.5px] text-ink-muted">No products match that search.</p>
      ) : (
        suggestions.map((product, suggestionIndex) => (
          <Link
            key={product.id}
            href={`/products/${product.slug}`}
            role="option"
            aria-selected={suggestionIndex === activeIndex}
            onClick={onSelect}
            className={`flex items-center gap-3 rounded-[10px] px-2.5 py-[9px] transition-colors duration-200 ${
              suggestionIndex === activeIndex ? "bg-surface" : "hover:bg-surface"
            }`}
          >
            <span className="flex h-[38px] w-[46px] shrink-0 items-center justify-center rounded-[7px] bg-surface-warm p-1">
              {product.primaryImageUrl ? (
                <Image
                  src={product.primaryImageUrl}
                  alt=""
                  width={46}
                  height={38}
                  className="max-h-full w-auto object-contain mix-blend-multiply"
                />
              ) : null}
            </span>
            <span className="flex-1">
              <span className="block text-[12.5px] font-medium">{product.name}</span>
              <span className="block text-[10.5px] tracking-[0.04em] text-ink-muted">
                {product.categoryName}
              </span>
            </span>
            <span className="text-[12px] font-medium text-ink-soft">
              {formatMoney(product.priceAmount)}
            </span>
          </Link>
        ))
      )}
    </div>
  );
}
