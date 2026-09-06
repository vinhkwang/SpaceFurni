import { apiFetch } from "@/lib/api/apiClient";
import type { AdminProductRowResponse, PageResponse } from "@/lib/api/types";
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

function resultCountLabel(totalElements: number, query: string): string {
  const pieceLabel = totalElements === 1 ? "1 product" : `${totalElements} products`;
  return query ? `${pieceLabel} matching “${query}”` : pieceLabel;
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
  const resolvedSearchParams = await searchParams;
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
          <div className="text-[16px] font-semibold text-ink">Products list</div>
          <div className="mt-1 text-[11.5px] text-ink-muted">
            {resultCountLabel(productPage.totalElements, query)}
          </div>
        </div>
        <form className="flex h-11 w-72 items-center gap-2.5 rounded-pill bg-surface-raised px-4.5">
          <SearchIcon />
          <input
            type="text"
            name="q"
            defaultValue={query}
            placeholder="Search products…"
            aria-label="Search products"
            className="flex-1 bg-transparent text-[12.5px] text-ink outline-none placeholder:text-ink-muted"
          />
        </form>
      </div>

      <ProductTable
        products={productPage.content}
        startIndex={productPage.page * productPage.size}
        currentPage={productPage.page}
        totalPages={productPage.totalPages}
        query={query}
      />
    </div>
  );
}
