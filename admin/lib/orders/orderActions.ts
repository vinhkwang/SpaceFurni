"use server";

import { revalidatePath } from "next/cache";
import { ApiError } from "@/lib/api/ApiError";
import { apiFetch } from "@/lib/api/apiClient";
import type { OrderStatus } from "@/lib/api/types";

export type OrderStatusTransitionResult = { success: true } | { success: false; isConflict: boolean; errorMessage: string };

function orderActionFailureMessage(error: unknown): string {
  return error instanceof ApiError ? error.message : "Something went wrong. Try again.";
}

export async function transitionOrderStatusAction(
  orderNumber: string,
  status: OrderStatus,
  version: number,
): Promise<OrderStatusTransitionResult> {
  try {
    await apiFetch<void>(`/admin/orders/${orderNumber}/status`, {
      method: "PATCH",
      body: { status, version },
      cache: "no-store",
    });
    revalidatePath(`/orders/${orderNumber}`);
    revalidatePath("/orders");
    return { success: true };
  } catch (error) {
    return {
      success: false,
      isConflict: error instanceof ApiError && error.status === 409,
      errorMessage: orderActionFailureMessage(error),
    };
  }
}

export type ProcessRefundResult = { success: true } | { success: false; errorMessage: string };

export async function processRefundAction(orderNumber: string, amountVnd: number): Promise<ProcessRefundResult> {
  try {
    await apiFetch<void>(`/admin/orders/${orderNumber}/refund`, {
      method: "POST",
      body: { amountVnd },
      cache: "no-store",
    });
    revalidatePath(`/orders/${orderNumber}`);
    revalidatePath("/orders");
    return { success: true };
  } catch (error) {
    return { success: false, errorMessage: orderActionFailureMessage(error) };
  }
}
