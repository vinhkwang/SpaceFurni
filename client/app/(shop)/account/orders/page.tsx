import { redirect } from "next/navigation";
import { apiFetch } from "@/lib/api/apiClient";
import { getSessionToken } from "@/lib/auth/session";
import type { OrderSummaryResponse, PageResponse } from "@/lib/api/types";
import { OrderHistoryRow } from "@/components/orders/OrderHistoryRow";
import { Container } from "@/components/ui/Container";

export const metadata = {
  title: "Your orders",
};

export default async function OrderHistoryPage() {
  const sessionToken = await getSessionToken();
  if (!sessionToken) {
    redirect("/login");
  }

  const orderHistory = await apiFetch<PageResponse<OrderSummaryResponse>>("/orders", { cache: "no-store" });

  return (
    <main className="py-8.5">
      <Container>
        <h1 className="mb-8.5 text-[38px] font-medium tracking-[-0.02em]">Your orders</h1>

        {orderHistory.content.length === 0 ? (
          <p className="text-[13px] text-ink-soft">You haven&apos;t placed any orders yet.</p>
        ) : (
          <div className="flex flex-col gap-3.5">
            {orderHistory.content.map((order) => (
              <OrderHistoryRow key={order.id} order={order} />
            ))}
          </div>
        )}
      </Container>
    </main>
  );
}
