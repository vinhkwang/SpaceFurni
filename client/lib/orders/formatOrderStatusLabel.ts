import type { OrderStatus } from "@/lib/api/types";

const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: "Pending",
  PAID: "Paid",
  PACKING: "Packing",
  DELIVERED: "Delivered",
  CANCELLED: "Cancelled",
};

export function formatOrderStatusLabel(status: OrderStatus): string {
  return ORDER_STATUS_LABELS[status];
}
