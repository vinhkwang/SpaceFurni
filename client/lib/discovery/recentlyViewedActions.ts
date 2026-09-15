"use server";

import { cookies } from "next/headers";
import { apiFetch, GUEST_CART_TOKEN_COOKIE_NAME } from "@/lib/api/apiClient";
import type { RecordRecentlyViewedResponse } from "@/lib/api/types";

const GUEST_CART_TOKEN_COOKIE_OPTIONS = {
  httpOnly: true,
  secure: true,
  sameSite: "lax" as const,
  path: "/",
};

async function persistGuestToken(guestToken: string | null): Promise<void> {
  if (guestToken === null) {
    return;
  }
  const cookieStore = await cookies();
  cookieStore.set(GUEST_CART_TOKEN_COOKIE_NAME, guestToken, GUEST_CART_TOKEN_COOKIE_OPTIONS);
}

export async function recordRecentlyViewedAction(productId: string): Promise<void> {
  try {
    const response = await apiFetch<RecordRecentlyViewedResponse>(`/recently-viewed/${productId}`, {
      method: "POST",
      cache: "no-store",
    });
    await persistGuestToken(response.guestToken);
  } catch (error) {
    void error;
  }
}
