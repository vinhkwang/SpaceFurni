"use server";

import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import type { DeliveryWindow, OrderResponse, PaymentMethod } from "@/lib/api/types";

const IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

export type PlaceOrderDeliveryDetails = {
  fullName: string;
  phone: string;
  street: string;
  district: string;
  city: string;
  note: string | null;
};

export type PlaceOrderInput = {
  deliveryDetails: PlaceOrderDeliveryDetails;
  deliveryWindow: DeliveryWindow;
  paymentMethod: PaymentMethod;
};

export type PlaceOrderActionResult =
  | { success: true; order: OrderResponse }
  | { success: false; errorMessage: string };

function placeOrderFailureMessage(error: unknown): string {
  return error instanceof ApiError ? error.message : "Something went wrong. Try again.";
}

export async function placeOrderAction(
  idempotencyKey: string,
  input: PlaceOrderInput,
): Promise<PlaceOrderActionResult> {
  try {
    const order = await apiFetch<OrderResponse>("/orders", {
      method: "POST",
      body: input,
      cache: "no-store",
      headers: { [IDEMPOTENCY_KEY_HEADER]: idempotencyKey },
    });
    return { success: true, order };
  } catch (error) {
    return { success: false, errorMessage: placeOrderFailureMessage(error) };
  }
}
