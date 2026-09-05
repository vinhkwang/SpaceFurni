import Link from "next/link";
import type { AdminOrderRowResponse, OrderStatus } from "@/lib/api/types";
import { formatMoney } from "@/lib/formatting/formatMoney";
import { formatOrderPlacedAt } from "@/lib/formatting/formatOrderPlacedAt";

type OrderTableProps = {
  orders: AdminOrderRowResponse[];
  currentPage: number;
  totalPages: number;
  status: OrderStatus | undefined;
};

const tableRowGridClassName = "grid grid-cols-[112px_1.3fr_1.5fr_120px_130px_120px] items-center gap-4";

function capitalizeStatus(status: OrderStatus): string {
  return status.charAt(0) + status.slice(1).toLowerCase();
}

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

export function OrderTable({ orders, currentPage, totalPages, status }: OrderTableProps) {
  return (
    <div>
      <div
        className={`${tableRowGridClassName} border-b border-hairline-soft px-2.5 pb-3.5 text-[10px] uppercase tracking-[0.14em] text-ink-muted`}
      >
        <span>Order</span>
        <span>Customer</span>
        <span>Items</span>
        <span>Placed</span>
        <span className="text-right">Total</span>
        <span className="text-center">Status</span>
      </div>

      {orders.map((order) => {
        const placedAt = formatOrderPlacedAt(order.placedAt);
        return (
          <div
            key={order.orderNumber}
            className={`${tableRowGridClassName} border-b border-hairline-soft/70 px-2.5 py-3.5`}
          >
            <span className="text-[12.5px] font-semibold text-ink">{order.orderNumber}</span>
            <div>
              <div className="text-[12.5px] font-medium text-ink">{order.customerName}</div>
              <div className="mt-0.5 text-[11px] text-ink-muted">{order.district}</div>
            </div>
            <div>
              <div className="text-[12.5px] text-ink-soft">{order.itemSummary}</div>
              <div className="mt-0.5 text-[11px] text-ink-muted">
                {order.lineCount === 1 ? "1 item" : `${order.lineCount} items`} · {order.paymentLabel}
              </div>
            </div>
            <div>
              <div className="text-[12px] text-ink">{placedAt.date}</div>
              <div className="mt-0.5 text-[11px] text-ink-muted">{placedAt.time}</div>
            </div>
            <span className="text-right text-[13px] font-semibold text-ink">{formatMoney(order.totalAmount)}</span>
            <span className="justify-self-center">
              <span className="rounded-pill bg-hairline-soft px-3 py-1.5 text-[10.5px] font-semibold text-ink-muted">
                {capitalizeStatus(order.status)}
              </span>
            </span>
          </div>
        );
      })}

      {orders.length === 0 ? (
        <div className="py-15 text-center text-[13px] text-ink-muted">No orders match this filter.</div>
      ) : null}

      {totalPages > 1 ? (
        <nav aria-label="Pagination" className="flex items-center justify-center gap-1.5 pt-5.5">
          {currentPage > 0 ? (
            <Link
              href={buildOrdersHref(status, currentPage - 1)}
              aria-label="Previous page"
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
                aria-label={`Page ${pageNumber}`}
                className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline text-[12px] transition-colors duration-200 hover:border-deep"
              >
                {pageNumber}
              </Link>
            ),
          )}

          {currentPage < totalPages - 1 ? (
            <Link
              href={buildOrdersHref(status, currentPage + 1)}
              aria-label="Next page"
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
