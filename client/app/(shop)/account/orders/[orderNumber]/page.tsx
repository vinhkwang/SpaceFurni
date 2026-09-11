import { notFound, redirect } from "next/navigation";
import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import { getSessionToken } from "@/lib/auth/session";
import type { OrderResponse } from "@/lib/api/types";
import { formatMoney } from "@/lib/formatting/formatMoney";
import { formatOrderPlacedAt } from "@/lib/orders/formatOrderPlacedAt";
import { formatOrderStatusLabel } from "@/lib/orders/formatOrderStatusLabel";
import { CancelOrderButton } from "@/components/orders/CancelOrderButton";
import { OrderTimeline } from "@/components/orders/OrderTimeline";
import { Container } from "@/components/ui/Container";

export const metadata = {
  title: "Order detail",
};

async function fetchOrder(orderNumber: string): Promise<OrderResponse> {
  try {
    return await apiFetch<OrderResponse>(`/orders/${orderNumber}`, { cache: "no-store" });
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }
}

export default async function OrderDetailPage({ params }: PageProps<"/account/orders/[orderNumber]">) {
  const { orderNumber } = await params;

  const sessionToken = await getSessionToken();
  if (!sessionToken) {
    redirect("/login");
  }

  const order = await fetchOrder(orderNumber);
  const placedAtParts = formatOrderPlacedAt(order.placedAt);

  return (
    <main className="py-8.5">
      <Container>
        <div className="mb-8.5 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-[38px] font-medium tracking-[-0.02em]">#{order.orderNumber}</h1>
            <p className="mt-1.5 text-[12.5px] text-ink-muted">
              Placed {placedAtParts.date} · {placedAtParts.time}
            </p>
          </div>
          <span className="text-[11px] font-semibold uppercase tracking-[0.14em] text-ink-soft">
            {formatOrderStatusLabel(order.status)}
          </span>
        </div>

        <div className="grid grid-cols-1 items-start gap-6.5 lg:grid-cols-[1fr_360px]">
          <div className="flex flex-col gap-6.5">
            <div className="rounded-2xl border border-hairline bg-white p-6.5">
              <div className="mb-5 text-[15px] font-semibold text-ink">Items</div>
              <div className="flex flex-col gap-4">
                {order.items.map((item) => (
                  <div key={item.productId} className="flex items-center justify-between gap-4 text-[13px]">
                    <div>
                      <div className="font-medium text-ink">{item.productName}</div>
                      <div className="mt-0.5 text-[11.5px] text-ink-muted">
                        {item.sku} · Qty {item.quantity}
                      </div>
                    </div>
                    <div className="font-semibold text-ink">{formatMoney(item.lineTotalAmount)}</div>
                  </div>
                ))}
              </div>

              <div className="mt-6 flex flex-col gap-2.5 border-t border-hairline pt-5 text-[13px]">
                <div className="flex justify-between">
                  <span className="text-ink-soft">Subtotal</span>
                  <span className="font-semibold">{formatMoney(order.subtotalAmount)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-ink-soft">Delivery</span>
                  <span className="font-semibold">{formatMoney(order.shippingAmount)}</span>
                </div>
                {order.discountAmount === 0 ? null : (
                  <div className="flex justify-between text-terracotta">
                    <span>Discount</span>
                    <span className="font-semibold">−{formatMoney(order.discountAmount)}</span>
                  </div>
                )}
                <div className="flex items-baseline justify-between pt-2 text-[15px]">
                  <span className="font-semibold uppercase tracking-[0.1em]">Total</span>
                  <span className="font-semibold">{formatMoney(order.totalAmount)}</span>
                </div>
              </div>
            </div>

            <div className="rounded-2xl border border-hairline bg-white p-6.5">
              <div className="mb-5 text-[15px] font-semibold text-ink">Delivery details</div>
              <div className="flex flex-col gap-1.5 text-[13px] text-ink-soft">
                <div className="font-medium text-ink">{order.deliveryDetails.fullName}</div>
                <div>{order.deliveryDetails.phone}</div>
                <div>
                  {order.deliveryDetails.street}, {order.deliveryDetails.district}, {order.deliveryDetails.city}
                </div>
                {order.deliveryDetails.note === null ? null : <div>{order.deliveryDetails.note}</div>}
              </div>
            </div>
          </div>

          <div className="flex flex-col gap-6.5">
            <OrderTimeline steps={order.timeline} />
            {order.cancellable ? <CancelOrderButton orderId={order.id} /> : null}
          </div>
        </div>
      </Container>
    </main>
  );
}
