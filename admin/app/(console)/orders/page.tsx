import Link from "next/link";
import { apiFetch } from "@/lib/api/apiClient";
import type { AdminOrderListResponse, OrderStatus } from "@/lib/api/types";
import { StatCard } from "@/components/dashboard/StatCard";
import { OrderTable, buildOrdersHref } from "@/components/orders/OrderTable";

const PAGE_SIZE = 20;

const ORDER_STATUS_VALUES: OrderStatus[] = ["PENDING", "PAID", "PACKING", "DELIVERED", "CANCELLED"];

const STATUS_FILTERS: { value: OrderStatus | undefined; label: string }[] = [
  { value: undefined, label: "All" },
  { value: "PENDING", label: "Pending" },
  { value: "PAID", label: "Paid" },
  { value: "PACKING", label: "Packing" },
  { value: "DELIVERED", label: "Delivered" },
  { value: "CANCELLED", label: "Cancelled" },
];

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

function resolveStatusFilter(rawStatus: string | undefined): OrderStatus | undefined {
  return ORDER_STATUS_VALUES.find((orderStatus) => orderStatus === rawStatus);
}

function filterCount(statusCounts: Partial<Record<OrderStatus, number>>, status: OrderStatus | undefined): number {
  if (status === undefined) {
    return ORDER_STATUS_VALUES.reduce((total, orderStatus) => total + (statusCounts[orderStatus] ?? 0), 0);
  }
  return statusCounts[status] ?? 0;
}

function PendingIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3.2 2" />
    </svg>
  );
}

function PaidIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="8.5" />
      <path d="M9 12.3l2 2 4.2-4.6" />
    </svg>
  );
}

function PackingIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 8.5 12 4l8 4.5v7L12 20l-8-4.5z" />
      <path d="M4 8.5 12 13l8-4.5M12 13v7" />
    </svg>
  );
}

function DeliveredIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="8.5" />
      <path d="m8.2 12.3 2.6 2.6 5-5.2" />
    </svg>
  );
}

export default async function OrdersPage({ searchParams }: PageProps<"/orders">) {
  const resolvedSearchParams = await searchParams;
  const statusFilter = resolveStatusFilter(firstSearchParamValue(resolvedSearchParams.status));
  const pageIndex = toPageIndex(firstSearchParamValue(resolvedSearchParams.page));

  const apiQuery = new URLSearchParams({ page: String(pageIndex), size: String(PAGE_SIZE) });
  if (statusFilter) {
    apiQuery.set("status", statusFilter);
  }

  const orderList = await apiFetch<AdminOrderListResponse>(`/admin/orders?${apiQuery.toString()}`, {
    cache: "no-store",
  });

  return (
    <div className="flex flex-col gap-4.5">
      <div className="grid grid-cols-4 gap-4.5">
        <StatCard label="Pending" value={orderList.statusCounts.PENDING ?? 0} icon={<PendingIcon />} />
        <StatCard label="Paid" value={orderList.statusCounts.PAID ?? 0} icon={<PaidIcon />} />
        <StatCard label="Packing" value={orderList.statusCounts.PACKING ?? 0} icon={<PackingIcon />} />
        <StatCard label="Delivered" value={orderList.statusCounts.DELIVERED ?? 0} icon={<DeliveredIcon />} />
      </div>

      <div className="rounded-2xl border border-hairline-soft bg-white p-6.5">
        <div className="mb-6 flex flex-wrap gap-2">
          {STATUS_FILTERS.map((filter) => (
            <Link
              key={filter.label}
              href={buildOrdersHref(filter.value, 0)}
              className={`flex h-10 items-center gap-2 rounded-pill border px-4.5 text-[11.5px] transition-colors duration-200 ${
                filter.value === statusFilter
                  ? "border-deep bg-deep text-white"
                  : "border-hairline text-ink hover:border-hairline-soft"
              }`}
            >
              {filter.label}
              <span className="opacity-60">{filterCount(orderList.statusCounts, filter.value)}</span>
            </Link>
          ))}
        </div>

        <OrderTable
          orders={orderList.orders.content}
          currentPage={orderList.orders.page}
          totalPages={orderList.orders.totalPages}
          status={statusFilter}
        />
      </div>
    </div>
  );
}
