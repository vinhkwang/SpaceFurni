import Link from "next/link";
import type { OrderSummaryResponse } from "@/lib/api/types";
import { formatMoney } from "@/lib/formatting/formatMoney";
import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { formatOrderPlacedAt } from "@/lib/orders/formatOrderPlacedAt";
import { formatOrderStatusLabel } from "@/lib/orders/formatOrderStatusLabel";

type OrderHistoryRowProps = {
  order: OrderSummaryResponse;
};

export async function OrderHistoryRow({ order }: OrderHistoryRowProps) {
  const dictionary = getDictionary(await getLocale());
  const placedAtParts = formatOrderPlacedAt(order.placedAt);
  return (
    <Link
      href={`/account/orders/${order.orderNumber}`}
      className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-hairline bg-white px-6.5 py-5 transition-colors duration-200 hover:border-terracotta"
    >
      <div>
        <div className="text-[13px] font-semibold text-ink">#{order.orderNumber}</div>
        <div className="mt-1 text-[11.5px] text-ink-muted">
          {placedAtParts.date} · {dictionary.orders.itemCount(order.itemCount)}
        </div>
      </div>
      <div className="flex items-center gap-6.5">
        <span className="text-[11px] font-semibold uppercase tracking-[0.1em] text-ink-soft">
          {formatOrderStatusLabel(dictionary, order.status)}
        </span>
        <span className="text-[14px] font-semibold text-ink">{formatMoney(order.totalAmount)}</span>
      </div>
    </Link>
  );
}
