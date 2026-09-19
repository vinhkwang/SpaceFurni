import { redirect } from "next/navigation";
import { apiFetch } from "@/lib/api/apiClient";
import { getSessionToken } from "@/lib/auth/session";
import type { OrderSummaryResponse, PageResponse } from "@/lib/api/types";
import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
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

  const [orderHistory, dictionary] = await Promise.all([
    apiFetch<PageResponse<OrderSummaryResponse>>("/orders", { cache: "no-store" }),
    getLocale().then(getDictionary),
  ]);

  return (
    <main className="py-8.5">
      <Container>
        <h1 className="mb-8.5 text-[38px] font-medium tracking-[-0.02em]">{dictionary.account.yourOrders}</h1>

        {orderHistory.content.length === 0 ? (
          <p className="text-[13px] text-ink-soft">{dictionary.account.noOrdersYet}</p>
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
