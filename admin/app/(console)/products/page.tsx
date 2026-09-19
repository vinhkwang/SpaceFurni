import Link from "next/link";
import { apiFetch } from "@/lib/api/apiClient";
import type { AdminProductRowResponse, PageResponse } from "@/lib/api/types";
import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { ProductTable } from "@/components/products/ProductTable";

const PAGE_SIZE = 20;

function firstSearchParamValue(rawValue: string | string[] | undefined): string | undefined {
  return Array.isArray(rawValue) ? rawValue[0] : rawValue;
}

function toPageIndex(rawPage: string | undefined): number {
  const parsedPage = Number(rawPage);
  if (!Number.isInteger(parsedPage) || parsedPage < 0) {
    return 0;
  }
  return parsedPage;
}

function SearchIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className="h-3.5 w-3.5 shrink-0 stroke-current text-ink-muted"
      fill="none"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="10.5" cy="10.5" r="6.5" />
      <path d="m20 20-4.3-4.3" />
    </svg>
  );
}

export default async function ProductsPage({ searchParams }: PageProps<"/products">) {
  const [resolvedSearchParams, dictionary] = await Promise.all([searchParams, getLocale().then(getDictionary)]);
  const query = firstSearchParamValue(resolvedSearchParams.q) ?? "";
  const pageIndex = toPageIndex(firstSearchParamValue(resolvedSearchParams.page));

  const apiQuery = new URLSearchParams({ page: String(pageIndex), size: String(PAGE_SIZE) });
  if (query) {
    apiQuery.set("q", query);
  }

  const productPage = await apiFetch<PageResponse<AdminProductRowResponse>>(
    `/admin/products?${apiQuery.toString()}`,
    { cache: "no-store" },
  );

  return (
    <div className="rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div className="mb-6.5 flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="text-[16px] font-semibold text-ink">{dictionary.products.productsList}</div>
          <div className="mt-1 text-[11.5px] text-ink-muted">
            {dictionary.products.resultCount(productPage.totalElements, query)}
          </div>
        </div>
        <div className="flex items-center gap-3">
          <form className="flex h-11 w-72 items-center gap-2.5 rounded-pill bg-surface-raised px-4.5">
            <SearchIcon />
            <input
              type="text"
              name="q"
              defaultValue={query}
              placeholder={dictionary.products.searchPlaceholder}
              aria-label={dictionary.products.searchAriaLabel}
              className="flex-1 bg-transparent text-[12.5px] text-ink outline-none placeholder:text-ink-muted"
            />
          </form>
          <Link
            href="/products/new"
            className="flex h-11 items-center rounded-pill bg-deep px-5.5 text-[11px] font-semibold uppercase tracking-[0.13em] text-white transition-colors duration-200 hover:bg-terracotta"
          >
            {dictionary.products.create}
          </Link>
        </div>
      </div>

      <ProductTable
        products={productPage.content}
        startIndex={productPage.page * productPage.size}
        currentPage={productPage.page}
        totalPages={productPage.totalPages}
        query={query}
        dictionary={dictionary}
      />
    </div>
  );
}
