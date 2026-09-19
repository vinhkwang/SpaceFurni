import Image from "next/image";
import Link from "next/link";
import type { AdminProductRowResponse, ProductStatus } from "@/lib/api/types";
import { formatMoney } from "@/lib/formatting/formatMoney";
import type { Dictionary } from "@/lib/i18n/getDictionary";
import { ProductRowActions } from "@/components/products/ProductRowActions";

const LOW_STOCK_THRESHOLD = 6;

type ProductTableProps = {
  products: AdminProductRowResponse[];
  startIndex: number;
  currentPage: number;
  totalPages: number;
  query: string;
  dictionary: Dictionary;
};

const tableRowGridClassName = "grid grid-cols-[44px_84px_1.4fr_1fr_120px_80px_110px_120px] items-center gap-3.5";

function statusBadgeClassName(status: ProductStatus): string {
  return status === "PUBLISHED" ? "bg-success/12 text-success" : "bg-hairline-soft text-ink-muted";
}

function stockCellClassName(stockOnHand: number): string {
  return stockOnHand < LOW_STOCK_THRESHOLD ? "font-semibold text-terracotta" : "text-ink-soft";
}

function buildProductsHref(page: number, query: string): string {
  const params = new URLSearchParams();
  if (query) {
    params.set("q", query);
  }
  if (page > 0) {
    params.set("page", String(page));
  }
  const queryString = params.toString();
  return queryString ? `/products?${queryString}` : "/products";
}

function pageNumbers(totalPages: number): number[] {
  return Array.from({ length: totalPages }, (_, pageOffset) => pageOffset + 1);
}

function ChevronIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className={`h-[7px] w-[7px] stroke-current ${className ?? ""}`}
      fill="none"
      strokeWidth={3.5}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="m9 5 7 7-7 7" />
    </svg>
  );
}

export function ProductTable({ products, startIndex, currentPage, totalPages, query, dictionary }: ProductTableProps) {
  return (
    <div>
      <div
        className={`${tableRowGridClassName} border-b border-hairline-soft px-2.5 pb-3.5 text-[10px] uppercase tracking-[0.14em] text-ink-muted`}
      >
        <span>{dictionary.products.columnIndex}</span>
        <span>{dictionary.products.columnImage}</span>
        <span>{dictionary.products.columnTitle}</span>
        <span>{dictionary.products.columnCategory}</span>
        <span className="text-right">{dictionary.products.columnPrice}</span>
        <span className="text-right">{dictionary.products.columnStock}</span>
        <span className="text-center">{dictionary.products.columnStatus}</span>
        <span className="text-right">{dictionary.products.columnActions}</span>
      </div>

      {products.map((product, index) => (
        <div
          key={product.id}
          className={`${tableRowGridClassName} border-b border-hairline-soft/70 px-2.5 py-3.5 transition-colors duration-200 hover:bg-canvas`}
        >
          <span className="text-[12px] text-ink-muted">{startIndex + index + 1}</span>
          <div className="flex h-13 w-16 items-center justify-center rounded-xl bg-surface-warm p-1.5">
            <div className="relative h-full w-full">
              <Image src={product.imageUrl} alt="" fill sizes="64px" className="object-contain mix-blend-multiply" />
            </div>
          </div>
          <div>
            <div className="text-[13px] font-medium text-ink">{product.title}</div>
            <div className="mt-0.5 text-[11px] text-ink-muted">{product.sku}</div>
          </div>
          <span className="text-[12.5px] text-ink-soft">{product.categoryLabel}</span>
          <span className="text-right text-[12.5px] font-semibold text-ink">{formatMoney(product.priceAmount)}</span>
          <span className={`text-right text-[12.5px] ${stockCellClassName(product.stockOnHand)}`}>
            {product.stockOnHand}
          </span>
          <span className="justify-self-center">
            <span
              className={`rounded-pill px-3 py-1.5 text-[10.5px] font-semibold ${statusBadgeClassName(product.status)}`}
            >
              {dictionary.products.statusLabels[product.status]}
            </span>
          </span>
          <ProductRowActions productId={product.id} />
        </div>
      ))}

      {products.length === 0 ? (
        <div className="py-15 text-center text-[13px] text-ink-muted">
          {query ? dictionary.products.noProductsMatch(query) : dictionary.products.noProductsYet}
        </div>
      ) : null}

      {totalPages > 1 ? (
        <nav aria-label={dictionary.common.paginationAriaLabel} className="flex items-center justify-center gap-1.5 pt-5.5">
          {currentPage > 0 ? (
            <Link
              href={buildProductsHref(currentPage - 1, query)}
              aria-label={dictionary.common.previousPage}
              className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline text-ink-muted transition-colors duration-200 hover:border-deep hover:text-ink"
            >
              <ChevronIcon className="rotate-180" />
            </Link>
          ) : (
            <span
              aria-hidden
              className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline-soft text-ink-muted/40"
            >
              <ChevronIcon className="rotate-180" />
            </span>
          )}

          {pageNumbers(totalPages).map((pageNumber) =>
            pageNumber - 1 === currentPage ? (
              <span
                key={pageNumber}
                aria-current="page"
                className="flex h-8.5 w-8.5 items-center justify-center rounded-xl bg-deep text-[12px] font-semibold text-white"
              >
                {pageNumber}
              </span>
            ) : (
              <Link
                key={pageNumber}
                href={buildProductsHref(pageNumber - 1, query)}
                aria-label={dictionary.common.pageAriaLabel(pageNumber)}
                className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline text-[12px] transition-colors duration-200 hover:border-deep"
              >
                {pageNumber}
              </Link>
            ),
          )}

          {currentPage < totalPages - 1 ? (
            <Link
              href={buildProductsHref(currentPage + 1, query)}
              aria-label={dictionary.common.nextPage}
              className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline text-ink-muted transition-colors duration-200 hover:border-deep hover:text-ink"
            >
              <ChevronIcon />
            </Link>
          ) : (
            <span
              aria-hidden
              className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline-soft text-ink-muted/40"
            >
              <ChevronIcon />
            </span>
          )}
        </nav>
      ) : null}
    </div>
  );
}
