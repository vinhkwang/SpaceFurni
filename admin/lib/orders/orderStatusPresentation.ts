import type { OrderStatus } from "@/lib/api/types";

export type OrderStatusPresentation = {
  label: string;
  backgroundColor: string;
  textColor: string;
};

export const ORDER_STATUS_PRESENTATION: Record<OrderStatus, OrderStatusPresentation> = {
  PENDING: { label: "Pending", backgroundColor: "rgba(26, 24, 21, .07)", textColor: "#55504A" },
  PAID: { label: "Paid", backgroundColor: "rgba(75, 122, 75, .08)", textColor: "#4B7A4B" },
  PACKING: { label: "Packing", backgroundColor: "rgba(201, 156, 100, .20)", textColor: "#8A6528" },
  DELIVERED: { label: "Delivered", backgroundColor: "rgba(75, 122, 75, .12)", textColor: "#3F6B3F" },
  CANCELLED: { label: "Cancelled", backgroundColor: "rgba(184, 67, 28, .10)", textColor: "#B8431C" },
};
