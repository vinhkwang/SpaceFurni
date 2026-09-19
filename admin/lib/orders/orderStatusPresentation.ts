import type { OrderStatus } from "@/lib/api/types";
import type { Dictionary } from "@/lib/i18n/dictionaries/en";

export type OrderStatusPresentation = {
  label: string;
  backgroundColor: string;
  textColor: string;
};

const ORDER_STATUS_COLORS: Record<OrderStatus, { backgroundColor: string; textColor: string }> = {
  PENDING: { backgroundColor: "rgba(26, 24, 21, .07)", textColor: "#55504A" },
  PAID: { backgroundColor: "rgba(75, 122, 75, .08)", textColor: "#4B7A4B" },
  PACKING: { backgroundColor: "rgba(201, 156, 100, .20)", textColor: "#8A6528" },
  DELIVERED: { backgroundColor: "rgba(75, 122, 75, .12)", textColor: "#3F6B3F" },
  CANCELLED: { backgroundColor: "rgba(184, 67, 28, .10)", textColor: "#B8431C" },
};

export function orderStatusPresentation(dictionary: Dictionary, status: OrderStatus): OrderStatusPresentation {
  return { ...ORDER_STATUS_COLORS[status], label: dictionary.orders.statusLabels[status] };
}
