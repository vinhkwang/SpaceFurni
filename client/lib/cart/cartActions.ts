"use server";

import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";
import { apiFetch, GUEST_CART_TOKEN_COOKIE_NAME } from "@/lib/api/apiClient";
import type { CartResponse } from "@/lib/api/types";

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

async function afterCartMutation(cart: CartResponse): Promise<CartResponse> {
  await persistGuestCartToken(cart);
  revalidatePath("/", "layout");
  return cart;
}

export async function addCartLineAction(productId: string, quantity: number): Promise<CartResponse> {
  const cart = await apiFetch<CartResponse>("/cart/items", {
    method: "POST",
    body: { productId, quantity },
    cache: "no-store",
  });
  return afterCartMutation(cart);
}

export async function updateCartLineQuantityAction(productId: string, quantity: number): Promise<CartResponse> {
  const cart = await apiFetch<CartResponse>(`/cart/items/${productId}`, {
    method: "PATCH",
    body: { quantity },
    cache: "no-store",
  });
  return afterCartMutation(cart);
}

export async function removeCartLineAction(productId: string): Promise<CartResponse> {
  const cart = await apiFetch<CartResponse>(`/cart/items/${productId}`, {
    method: "DELETE",
    cache: "no-store",
  });
  return afterCartMutation(cart);
}
