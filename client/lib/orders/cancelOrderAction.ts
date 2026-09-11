"use server";

import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import type { OrderResponse } from "@/lib/api/types";

export type CancelOrderActionResult =
  | { success: true; order: OrderResponse }
  | { success: false; errorMessage: string };

export async function cancelOrderAction(orderId: string, reason: string): Promise<CancelOrderActionResult> {
  try {
    const order = await apiFetch<OrderResponse>(`/orders/${orderId}/cancel`, {
      method: "POST",
      body: { reason },
      cache: "no-store",
    });
    return { success: true, order };
  } catch (error) {
    if (error instanceof ApiError) {
      return { success: false, errorMessage: error.message };
    }
    return { success: false, errorMessage: "Something went wrong. Try again." };
  }
}
