import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import type { AuthenticationResponse } from "@/lib/api/types";
import { clearSessionCookies, getRefreshToken, setSessionCookies } from "@/lib/auth/session";

export async function POST(): Promise<Response> {
  const refreshToken = await getRefreshToken();

  if (!refreshToken) {
    return Response.json(
      {
        success: false,
        data: null,
        error: { code: "UNAUTHENTICATED", message: "No active session to refresh.", details: null },
      },
      { status: 401 },
    );
  }

  try {
    const authentication = await apiFetch<AuthenticationResponse>("/auth/refresh", {
      method: "POST",
      body: { refreshToken },
      cache: "no-store",
    });
    await setSessionCookies(authentication.accessToken, authentication.refreshToken);
    return Response.json({ success: true, data: null, error: null });
  } catch (error) {
    if (error instanceof ApiError) {
      await clearSessionCookies();
      return Response.json(
        { success: false, data: null, error: { code: error.code, message: error.message, details: error.details } },
        { status: error.status },
      );
    }
    throw error;
  }
}
