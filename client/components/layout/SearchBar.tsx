"use client";

import { useRouter } from "next/navigation";
import { useEffect, useRef, useState, type KeyboardEvent } from "react";
import type { ProductSummaryResponse } from "@/lib/api/types";
import { publicApiBaseUrl } from "@/lib/config/environment";
import { SearchSuggestions } from "@/components/layout/SearchSuggestions";

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
  const router = useRouter();
  const [query, setQuery] = useState("");
  const [searchedQuery, setSearchedQuery] = useState("");
  const [suggestions, setSuggestions] = useState<ProductSummaryResponse[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [activeSuggestionIndex, setActiveSuggestionIndex] = useState(-1);
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
          setActiveSuggestionIndex(-1);
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

  function handleInputKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === "Escape") {
      setIsOpen(false);
      return;
    }

    if (!isShowingSuggestions || suggestions.length === 0) {
      return;
    }

    if (event.key === "ArrowDown") {
      event.preventDefault();
      setActiveSuggestionIndex((currentIndex) => Math.min(currentIndex + 1, suggestions.length - 1));
    } else if (event.key === "ArrowUp") {
      event.preventDefault();
      setActiveSuggestionIndex((currentIndex) => Math.max(currentIndex - 1, -1));
    } else if (event.key === "Enter" && activeSuggestionIndex >= 0) {
      event.preventDefault();
      router.push(`/products/${suggestions[activeSuggestionIndex].slug}`);
      setIsOpen(false);
    }
  }

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
          onKeyDown={handleInputKeyDown}
          placeholder="Search sofas, tables, shelves…"
          aria-label="Search products"
          role="combobox"
          aria-expanded={isShowingSuggestions}
          aria-controls="search-suggestions-listbox"
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
        <SearchSuggestions
          suggestions={suggestions}
          activeIndex={activeSuggestionIndex}
          onSelect={() => setIsOpen(false)}
        />
      ) : null}
    </div>
  );
}
