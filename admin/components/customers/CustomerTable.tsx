import Link from "next/link";
import type { AdminCustomerRowResponse, CustomerTier } from "@/lib/api/types";
import { customerTierPresentation } from "@/lib/customers/customerTierPresentation";
import { formatMoney } from "@/lib/formatting/formatMoney";
import type { Dictionary } from "@/lib/i18n/getDictionary";

type CustomerTableProps = {
  customers: AdminCustomerRowResponse[];
  currentPage: number;
  totalPages: number;
  tier: CustomerTier | undefined;
  query: string;
  dictionary: Dictionary;
};

const tableRowGridClassName = "grid grid-cols-[2fr_1.4fr_110px_90px_140px_110px] items-center gap-4";

export function buildCustomersHref(tier: CustomerTier | undefined, query: string, page: number): string {
  const params = new URLSearchParams();
  if (tier) {
    params.set("tier", tier);
  }
  if (query) {
    params.set("q", query);
  }
  if (page > 0) {
    params.set("page", String(page));
  }
  const queryString = params.toString();
  return queryString ? `/customers?${queryString}` : "/customers";
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

function pageNumbers(totalPages: number): number[] {
  return Array.from({ length: totalPages }, (_, pageOffset) => pageOffset + 1);
}

export function CustomerTable({ customers, currentPage, totalPages, tier, query, dictionary }: CustomerTableProps) {
  return (
    <div>
      <div
        className={`${tableRowGridClassName} border-b border-hairline-soft px-2.5 pb-3.5 text-[10px] uppercase tracking-[0.14em] text-ink-muted`}
      >
        <span>{dictionary.customers.columnCustomer}</span>
        <span>{dictionary.customers.columnContact}</span>
        <span>{dictionary.customers.columnDistrict}</span>
        <span className="text-right">{dictionary.customers.columnOrders}</span>
        <span className="text-right">{dictionary.customers.columnLifetime}</span>
        <span className="text-center">{dictionary.customers.columnTier}</span>
      </div>

      {customers.map((customer) => {
        const tierPresentation = customerTierPresentation(dictionary, customer.tier);
        return (
          <Link
            key={customer.id}
            href={`/customers/${customer.id}`}
            className={`${tableRowGridClassName} border-b border-hairline-soft/70 px-2.5 py-3.5 transition-colors duration-200 hover:bg-canvas`}
          >
            <span className="text-[12.5px] font-medium text-ink">{customer.fullName}</span>
            <span className="text-[12.5px] text-ink-soft">{customer.email}</span>
            <span className="text-[12.5px] text-ink-soft">{customer.district}</span>
            <span className="text-right text-[12.5px] text-ink">{customer.orderCount}</span>
            <span className="text-right text-[13px] font-semibold text-ink">
              {formatMoney(customer.lifetimeValueAmount)}
            </span>
            <span className="justify-self-center">
              <span
                className="rounded-pill px-3 py-1.5 text-[10.5px] font-semibold"
                style={{ backgroundColor: tierPresentation.backgroundColor, color: tierPresentation.textColor }}
              >
                {tierPresentation.label}
              </span>
            </span>
          </Link>
        );
      })}

      {customers.length === 0 ? (
        <div className="py-15 text-center text-[13px] text-ink-muted">{dictionary.customers.noCustomersMatch}</div>
      ) : null}

      {totalPages > 1 ? (
        <nav aria-label={dictionary.common.paginationAriaLabel} className="flex items-center justify-center gap-1.5 pt-5.5">
          {currentPage > 0 ? (
            <Link
              href={buildCustomersHref(tier, query, currentPage - 1)}
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
                href={buildCustomersHref(tier, query, pageNumber - 1)}
                aria-label={dictionary.common.pageAriaLabel(pageNumber)}
                className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline text-[12px] transition-colors duration-200 hover:border-deep"
              >
                {pageNumber}
              </Link>
            ),
          )}

          {currentPage < totalPages - 1 ? (
            <Link
              href={buildCustomersHref(tier, query, currentPage + 1)}
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
