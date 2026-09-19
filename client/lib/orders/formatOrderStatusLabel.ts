import type { OrderStatus } from "@/lib/api/types";
import type { Dictionary } from "@/lib/i18n/dictionaries/en";

export function formatOrderStatusLabel(dictionary: Dictionary, status: OrderStatus): string {
  return dictionary.orders.statusLabels[status];
}
