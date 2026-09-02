import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import { clearSessionCookies, getRefreshToken } from "@/lib/auth/session";

export async function POST(): Promise<Response> {
  const refreshToken = await getRefreshToken();

  try {
    if (refreshToken) {
      await apiFetch<null>("/auth/logout", {
        method: "POST",
        body: { refreshToken },
        cache: "no-store",
      });
    }
  } catch (error) {
    if (!(error instanceof ApiError)) {
      throw error;
    }
  } finally {
    await clearSessionCookies();
  }

  return Response.json({ success: true, data: null, error: null });
}
