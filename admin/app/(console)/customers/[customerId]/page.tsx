import Link from "next/link";
import { notFound } from "next/navigation";
import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import type { AdminCustomerDetailResponse } from "@/lib/api/types";
import { customerTierPresentation } from "@/lib/customers/customerTierPresentation";
import { formatMoney } from "@/lib/formatting/formatMoney";
import { formatOrderPlacedAt } from "@/lib/formatting/formatOrderPlacedAt";
import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { orderStatusPresentation } from "@/lib/orders/orderStatusPresentation";

function BackArrowIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className="h-3 w-3 stroke-current"
      fill="none"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M19 12H5M11 6l-6 6 6 6" />
    </svg>
  );
}

export default async function CustomerDetailPage({ params }: PageProps<"/customers/[customerId]">) {
  const { customerId } = await params;

  let customer: AdminCustomerDetailResponse;
  try {
    customer = await apiFetch<AdminCustomerDetailResponse>(`/admin/customers/${customerId}`, { cache: "no-store" });
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }

  const dictionary = getDictionary(await getLocale());
  const tierPresentation = customerTierPresentation(dictionary, customer.tier);
  const firstOrderAt = formatOrderPlacedAt(customer.firstOrderAt);

  return (
    <div className="flex flex-col gap-4.5">
      <div className="rounded-2xl border border-hairline-soft bg-white p-7.5">
        <div className="mb-6.5 flex items-center gap-3.5">
          <Link
            href="/customers"
            aria-label={dictionary.customers.backToCustomersAriaLabel}
            className="flex h-9 w-9 items-center justify-center rounded-xl bg-surface-raised transition-colors duration-200 hover:bg-deep hover:text-white"
          >
            <BackArrowIcon />
          </Link>
          <div className="flex-1">
            <div className="flex items-center gap-3">
              <span className="text-[18px] font-semibold text-ink">{customer.fullName}</span>
              <span
                className="rounded-pill px-3 py-1.5 text-[10.5px] font-semibold"
                style={{ backgroundColor: tierPresentation.backgroundColor, color: tierPresentation.textColor }}
              >
                {tierPresentation.label}
              </span>
            </div>
            <div className="mt-1 text-[11.5px] text-ink-muted">
              {customer.email} · {customer.district}
            </div>
          </div>
        </div>

        <div className="grid grid-cols-3 gap-4.5 border-t border-hairline pt-6">
          <div>
            <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
              {dictionary.customers.columnOrders}
            </div>
            <div className="text-[20px] font-semibold text-ink">{customer.orderCount}</div>
          </div>
          <div>
            <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
              {dictionary.customers.columnLifetime}
            </div>
            <div className="text-[20px] font-semibold text-ink">{formatMoney(customer.lifetimeValueAmount)}</div>
          </div>
          <div>
            <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
              {dictionary.customers.firstOrder}
            </div>
            <div className="text-[20px] font-semibold text-ink">{firstOrderAt.date}</div>
          </div>
        </div>
      </div>

      <div className="rounded-2xl border border-hairline-soft bg-white p-7.5">
        <div className="mb-5 text-[16px] font-semibold text-ink">{dictionary.customers.recentOrders}</div>

        {customer.recentOrders.length === 0 ? (
          <div className="py-10 text-center text-[13px] text-ink-muted">{dictionary.customers.noRecentOrders}</div>
        ) : (
          <div>
            <div className="grid grid-cols-[1.2fr_1.2fr_1fr_120px] items-center gap-4 border-b border-hairline-soft px-2.5 pb-3.5 text-[10px] uppercase tracking-[0.14em] text-ink-muted">
              <span>{dictionary.orders.columnOrder}</span>
              <span>{dictionary.orders.columnPlaced}</span>
              <span className="text-right">{dictionary.orders.columnTotal}</span>
              <span className="text-center">{dictionary.orders.columnStatus}</span>
            </div>
            {customer.recentOrders.map((order) => {
              const placedAt = formatOrderPlacedAt(order.placedAt);
              const statusPresentation = orderStatusPresentation(dictionary, order.status);
              return (
                <Link
                  key={order.orderNumber}
                  href={`/orders/${order.orderNumber}`}
                  className="grid grid-cols-[1.2fr_1.2fr_1fr_120px] items-center gap-4 border-b border-hairline-soft/70 px-2.5 py-3.5 transition-colors duration-200 hover:bg-canvas"
                >
                  <span className="text-[12.5px] font-semibold text-ink">{order.orderNumber}</span>
                  <span className="text-[12.5px] text-ink-soft">
                    {placedAt.date} · {placedAt.time}
                  </span>
                  <span className="text-right text-[13px] font-semibold text-ink">
                    {formatMoney(order.totalAmount)}
                  </span>
                  <span className="justify-self-center">
                    <span
                      className="rounded-pill px-3 py-1.5 text-[10.5px] font-semibold"
                      style={{
                        backgroundColor: statusPresentation.backgroundColor,
                        color: statusPresentation.textColor,
                      }}
                    >
                      {statusPresentation.label}
                    </span>
                  </span>
                </Link>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
