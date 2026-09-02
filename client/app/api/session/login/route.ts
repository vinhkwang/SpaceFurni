import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import type { AuthenticationResponse } from "@/lib/api/types";
import { setSessionCookies } from "@/lib/auth/session";

type LoginRequestBody = {
  email: string;
  password: string;
};

export async function POST(request: Request): Promise<Response> {
  const body = (await request.json()) as LoginRequestBody;

  try {
    const authentication = await apiFetch<AuthenticationResponse>("/auth/login", {
      method: "POST",
      body: { email: body.email, password: body.password },
      cache: "no-store",
    });
    await setSessionCookies(authentication.accessToken, authentication.refreshToken);
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
