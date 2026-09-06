import Link from "next/link";
import { notFound } from "next/navigation";
import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import type { AdminOrderDetailResponse, PaymentMethod, PaymentStatus } from "@/lib/api/types";
import { formatMoney } from "@/lib/formatting/formatMoney";
import { ORDER_STATUS_PRESENTATION } from "@/lib/orders/orderStatusPresentation";
import { OrderLines } from "@/components/orders/OrderLines";
import { OrderTimeline } from "@/components/orders/OrderTimeline";
import { OrderStatusActions } from "@/components/orders/OrderStatusActions";
import { OrderCustomerPanel } from "@/components/orders/OrderCustomerPanel";
import { PrintPackingSlipButton } from "@/components/orders/PrintPackingSlipButton";

const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  CARD: "Card",
  CASH_ON_DELIVERY: "Cash on delivery",
  BANK_TRANSFER: "Bank transfer",
};

const PAYMENT_STATUS_LABELS: Record<PaymentStatus, string> = {
  PENDING: "Pending",
  AUTHORISED: "Authorised",
  CAPTURED: "Captured",
  FAILED: "Failed",
};

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

export default async function OrderDetailPage({ params }: PageProps<"/orders/[orderNumber]">) {
  const { orderNumber } = await params;

  let order: AdminOrderDetailResponse;
  try {
    order = await apiFetch<AdminOrderDetailResponse>(`/admin/orders/${orderNumber}`, { cache: "no-store" });
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }

  const statusPresentation = ORDER_STATUS_PRESENTATION[order.status];
  const paymentLabel = `${PAYMENT_METHOD_LABELS[order.paymentMethod]} · ${PAYMENT_STATUS_LABELS[order.paymentStatus]}`;

  return (
    <div className="grid grid-cols-[1fr_380px] items-start gap-4.5">
      <div className="flex flex-col gap-4.5">
        <div className="rounded-2xl border border-hairline-soft bg-white p-7.5">
          <div className="mb-6.5 flex items-center gap-3.5">
            <Link
              href="/orders"
              aria-label="Back to orders"
              className="flex h-9 w-9 items-center justify-center rounded-xl bg-surface-raised transition-colors duration-200 hover:bg-deep hover:text-white"
            >
              <BackArrowIcon />
            </Link>
            <div className="flex-1">
              <div className="flex items-center gap-3">
                <span className="text-[18px] font-semibold text-ink">{order.orderNumber}</span>
                <span
                  className="rounded-pill px-3 py-1.5 text-[10.5px] font-semibold"
                  style={{ backgroundColor: statusPresentation.backgroundColor, color: statusPresentation.textColor }}
                >
                  {statusPresentation.label}
                </span>
              </div>
              <div className="mt-1 text-[11.5px] text-ink-muted">{paymentLabel}</div>
            </div>
            <PrintPackingSlipButton />
          </div>

          <OrderLines lines={order.lines} />

          <div className="mt-5 flex flex-col gap-3 border-t border-hairline pt-5">
            <div className="flex justify-between text-[13px]">
              <span className="text-ink-soft">Subtotal</span>
              <span className="font-semibold text-ink">{formatMoney(order.subtotalAmount)}</span>
            </div>
            <div className="flex justify-between text-[13px]">
              <span className="text-ink-soft">Delivery & assembly</span>
              <span className="font-semibold text-ink">{formatMoney(order.shippingAmount)}</span>
            </div>
            <div className="flex items-baseline justify-between border-t border-hairline-soft pt-3">
              <span className="text-[12px] font-semibold uppercase tracking-[0.14em] text-ink">Total</span>
              <span className="text-[21px] font-semibold tracking-[-0.02em] text-ink">
                {formatMoney(order.totalAmount)}
              </span>
            </div>
          </div>
        </div>

        <OrderTimeline steps={order.timeline} />
      </div>

      <div className="flex flex-col gap-4.5">
        <OrderStatusActions orderNumber={order.orderNumber} currentStatus={order.status} version={order.version} />
        <OrderCustomerPanel
          customer={order.customer}
          deliveryAddress={order.deliveryAddress}
          deliveryWindow={order.deliveryWindow}
          placedAt={order.placedAt}
        />
      </div>
    </div>
  );
}
