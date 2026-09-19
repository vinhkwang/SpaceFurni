import Link from "next/link";
import type { AdminOrderRowResponse, OrderStatus } from "@/lib/api/types";
import { formatMoney } from "@/lib/formatting/formatMoney";
import { formatOrderPlacedAt } from "@/lib/formatting/formatOrderPlacedAt";
import type { Dictionary } from "@/lib/i18n/getDictionary";
import { orderStatusPresentation } from "@/lib/orders/orderStatusPresentation";

type OrderTableProps = {
  orders: AdminOrderRowResponse[];
  currentPage: number;
  totalPages: number;
  status: OrderStatus | undefined;
  dictionary: Dictionary;
};

const tableRowGridClassName = "grid grid-cols-[112px_1.3fr_1.5fr_120px_130px_120px] items-center gap-4";

export function buildOrdersHref(status: OrderStatus | undefined, page: number): string {
  const params = new URLSearchParams();
  if (status) {
    params.set("status", status);
  }
  if (page > 0) {
    params.set("page", String(page));
  }
  const queryString = params.toString();
  return queryString ? `/orders?${queryString}` : "/orders";
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

export function OrderTable({ orders, currentPage, totalPages, status, dictionary }: OrderTableProps) {
  return (
    <div>
      <div
        className={`${tableRowGridClassName} border-b border-hairline-soft px-2.5 pb-3.5 text-[10px] uppercase tracking-[0.14em] text-ink-muted`}
      >
        <span>{dictionary.orders.columnOrder}</span>
        <span>{dictionary.orders.columnCustomer}</span>
        <span>{dictionary.orders.columnItems}</span>
        <span>{dictionary.orders.columnPlaced}</span>
        <span className="text-right">{dictionary.orders.columnTotal}</span>
        <span className="text-center">{dictionary.orders.columnStatus}</span>
      </div>

      {orders.map((order) => {
        const placedAt = formatOrderPlacedAt(order.placedAt);
        const statusPresentation = orderStatusPresentation(dictionary, order.status);
        return (
          <Link
            key={order.orderNumber}
            href={`/orders/${order.orderNumber}`}
            className={`${tableRowGridClassName} border-b border-hairline-soft/70 px-2.5 py-3.5 transition-colors duration-200 hover:bg-canvas`}
          >
            <span className="text-[12.5px] font-semibold text-ink">{order.orderNumber}</span>
            <div>
              <div className="text-[12.5px] font-medium text-ink">{order.customerName}</div>
              <div className="mt-0.5 text-[11px] text-ink-muted">{order.district}</div>
            </div>
            <div>
              <div className="text-[12.5px] text-ink-soft">{order.itemSummary}</div>
              <div className="mt-0.5 text-[11px] text-ink-muted">
                {dictionary.orders.itemCount(order.lineCount)} · {order.paymentLabel}
              </div>
            </div>
            <div>
              <div className="text-[12px] text-ink">{placedAt.date}</div>
              <div className="mt-0.5 text-[11px] text-ink-muted">{placedAt.time}</div>
            </div>
            <span className="text-right text-[13px] font-semibold text-ink">{formatMoney(order.totalAmount)}</span>
            <span className="justify-self-center">
              <span
                className="rounded-pill px-3 py-1.5 text-[10.5px] font-semibold"
                style={{ backgroundColor: statusPresentation.backgroundColor, color: statusPresentation.textColor }}
              >
                {statusPresentation.label}
              </span>
            </span>
          </Link>
        );
      })}

      {orders.length === 0 ? (
        <div className="py-15 text-center text-[13px] text-ink-muted">{dictionary.orders.noOrdersMatch}</div>
      ) : null}

      {totalPages > 1 ? (
        <nav aria-label={dictionary.common.paginationAriaLabel} className="flex items-center justify-center gap-1.5 pt-5.5">
          {currentPage > 0 ? (
            <Link
              href={buildOrdersHref(status, currentPage - 1)}
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
                href={buildOrdersHref(status, pageNumber - 1)}
                aria-label={dictionary.common.pageAriaLabel(pageNumber)}
                className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline text-[12px] transition-colors duration-200 hover:border-deep"
              >
                {pageNumber}
              </Link>
            ),
          )}

          {currentPage < totalPages - 1 ? (
            <Link
              href={buildOrdersHref(status, currentPage + 1)}
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
