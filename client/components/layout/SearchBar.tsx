"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import type { ProductSummaryResponse } from "@/lib/api/types";
import { publicApiBaseUrl } from "@/lib/config/environment";
import { formatMoney } from "@/lib/formatting/formatMoney";

const SUGGESTION_DEBOUNCE_MILLISECONDS = 250;

type SearchSuggestionEnvelope = {
  success: boolean;
  data: ProductSummaryResponse[] | null;
};

async function fetchSuggestions(
  query: string,
  signal: AbortSignal,
): Promise<ProductSummaryResponse[]> {
  const response = await fetch(
    `${publicApiBaseUrl}/api/v1/products/search?q=${encodeURIComponent(query)}`,
    { signal },
  );
  const envelope = (await response.json()) as SearchSuggestionEnvelope;
  return envelope.success && envelope.data ? envelope.data : [];
}

export function SearchBar() {
  const [query, setQuery] = useState("");
  const [searchedQuery, setSearchedQuery] = useState("");
  const [suggestions, setSuggestions] = useState<ProductSummaryResponse[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const trimmedQuery = query.trim();
    if (trimmedQuery.length === 0) {
      return;
    }

    const abortController = new AbortController();
    const debounceTimer = setTimeout(() => {
      fetchSuggestions(trimmedQuery, abortController.signal)
        .then((matchedProducts) => {
          setSuggestions(matchedProducts);
          setSearchedQuery(trimmedQuery);
        })
        .catch(() => undefined);
    }, SUGGESTION_DEBOUNCE_MILLISECONDS);

    return () => {
      clearTimeout(debounceTimer);
      abortController.abort();
    };
  }, [query]);

  useEffect(() => {
    function closeOnOutsidePointerDown(event: PointerEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    document.addEventListener("pointerdown", closeOnOutsidePointerDown);
    return () => document.removeEventListener("pointerdown", closeOnOutsidePointerDown);
  }, []);

  const trimmedQuery = query.trim();
  const isShowingSuggestions =
    isOpen && trimmedQuery.length > 0 && searchedQuery === trimmedQuery;

  return (
    <div ref={containerRef} className="relative w-full max-w-[420px] flex-1">
      <div className="flex h-[46px] items-center gap-2.5 rounded-pill border border-hairline-soft bg-surface px-[18px] transition-colors duration-200 hover:border-hairline">
        <svg
          viewBox="0 0 24 24"
          aria-hidden
          className="h-3 w-3 shrink-0 stroke-ink-muted"
          fill="none"
          strokeWidth={2}
          strokeLinecap="round"
        >
          <path d="M11 3a8 8 0 1 0 0 16 8 8 0 0 0 0-16z" />
          <path d="m21 21-4.3-4.3" />
        </svg>
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onFocus={() => setIsOpen(true)}
          placeholder="Search sofas, tables, shelves…"
          aria-label="Search products"
          className="min-w-0 flex-1 bg-transparent text-[13px] tracking-[0.01em] outline-none placeholder:text-ink-muted"
        />
        {query.length > 0 ? (
          <button
            type="button"
            aria-label="Clear search"
            onClick={() => setQuery("")}
            className="cursor-pointer text-ink-muted"
          >
            <svg
              viewBox="0 0 24 24"
              aria-hidden
              className="h-3 w-3 stroke-current"
              fill="none"
              strokeWidth={2}
              strokeLinecap="round"
            >
              <path d="M18 6 6 18M6 6l12 12" />
            </svg>
          </button>
        ) : null}
      </div>

      {isShowingSuggestions ? (
        <div className="absolute inset-x-0 top-[54px] z-60 rounded-2xl border border-hairline-soft bg-white p-2 shadow-2xl">
          {suggestions.length === 0 ? (
            <p className="px-3 py-4 text-[12.5px] text-ink-muted">
              No products match that search.
            </p>
          ) : (
            suggestions.map((product) => (
              <Link
                key={product.id}
                href={`/products/${product.slug}`}
                onClick={() => setIsOpen(false)}
                className="flex items-center gap-3 rounded-[10px] px-2.5 py-[9px] transition-colors duration-200 hover:bg-surface"
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
      ) : null}
    </div>
  );
}
