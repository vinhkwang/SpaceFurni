import { cookies } from "next/headers";
import { apiFetch, GUEST_CART_TOKEN_COOKIE_NAME } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import type { AuthenticationResponse, CartResponse } from "@/lib/api/types";
import { setSessionCookies } from "@/lib/auth/session";

type RegisterRequestBody = {
  fullName: string;
  email: string;
  password: string;
};

async function mergeGuestCartIntoSignedInCart(): Promise<void> {
  const cookieStore = await cookies();
  if (!cookieStore.get(GUEST_CART_TOKEN_COOKIE_NAME)) {
    return;
  }
  await apiFetch<CartResponse>("/cart/merge", { method: "POST", cache: "no-store" });
  cookieStore.delete(GUEST_CART_TOKEN_COOKIE_NAME);
}

export async function POST(request: Request): Promise<Response> {
  const body = (await request.json()) as RegisterRequestBody;

  try {
    const authentication = await apiFetch<AuthenticationResponse>("/auth/register", {
      method: "POST",
      body: { fullName: body.fullName, email: body.email, password: body.password },
      cache: "no-store",
    });
    await setSessionCookies(authentication.accessToken, authentication.refreshToken);
    await mergeGuestCartIntoSignedInCart();
    return Response.json({ success: true, data: null, error: null });
  } catch (error) {
    if (error instanceof ApiError) {
      return Response.json(
        { success: false, data: null, error: { code: error.code, message: error.message, details: error.details } },
        { status: error.status },
      );
    }
    throw error;
  }
}
