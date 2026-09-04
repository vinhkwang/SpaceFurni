"use server";

import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";
import { ApiError } from "@/lib/api/ApiError";
import { apiFetch, GUEST_CART_TOKEN_COOKIE_NAME } from "@/lib/api/apiClient";
import type { CartResponse } from "@/lib/api/types";

export type CartActionResult =
  | { success: true; cart: CartResponse }
  | { success: false; errorMessage: string };

const GUEST_CART_TOKEN_COOKIE_OPTIONS = {
  httpOnly: true,
  secure: true,
  sameSite: "lax" as const,
  path: "/",
};

async function persistGuestCartToken(cart: CartResponse): Promise<void> {
  if (cart.guestToken === null) {
    return;
  }
  const cookieStore = await cookies();
  cookieStore.set(GUEST_CART_TOKEN_COOKIE_NAME, cart.guestToken, GUEST_CART_TOKEN_COOKIE_OPTIONS);
}

function cartMutationFailureMessage(error: unknown): string {
  return error instanceof ApiError ? error.message : "Something went wrong. Try again.";
}

async function runCartMutation(mutate: () => Promise<CartResponse>): Promise<CartActionResult> {
  try {
    const cart = await mutate();
    await persistGuestCartToken(cart);
    revalidatePath("/", "layout");
    return { success: true, cart };
  } catch (error) {
    return { success: false, errorMessage: cartMutationFailureMessage(error) };
  }
}

export async function addCartLineAction(productId: string, quantity: number): Promise<CartActionResult> {
  return runCartMutation(() =>
    apiFetch<CartResponse>("/cart/items", {
      method: "POST",
      body: { productId, quantity },
      cache: "no-store",
    }),
  );
}

export async function updateCartLineQuantityAction(
  productId: string,
  quantity: number,
): Promise<CartActionResult> {
  return runCartMutation(() =>
    apiFetch<CartResponse>(`/cart/items/${productId}`, {
      method: "PATCH",
      body: { quantity },
      cache: "no-store",
    }),
  );
}

export async function removeCartLineAction(productId: string): Promise<CartActionResult> {
  return runCartMutation(() =>
    apiFetch<CartResponse>(`/cart/items/${productId}`, {
      method: "DELETE",
      cache: "no-store",
    }),
  );
}

export async function applyPromotionCodeAction(code: string): Promise<CartActionResult> {
  return runCartMutation(() =>
    apiFetch<CartResponse>("/cart/promotion", {
      method: "POST",
      body: { code },
      cache: "no-store",
    }),
  );
}

export async function clearPromotionCodeAction(): Promise<CartActionResult> {
  return runCartMutation(() =>
    apiFetch<CartResponse>("/cart/promotion", {
      method: "POST",
      body: { code: null },
      cache: "no-store",
    }),
  );
}
