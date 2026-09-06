import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import type { AuthenticationResponse, CurrentUserResponse } from "@/lib/api/types";
import { clearSessionCookie, setSessionCookie } from "@/lib/auth/session";

type LoginRequestBody = {
  email: string;
  password: string;
};

function forbiddenEnvelopeResponse(): Response {
  return Response.json(
    {
      success: false,
      data: null,
      error: {
        code: "FORBIDDEN",
        message: "This account does not have admin access.",
        details: null,
      },
    },
    { status: 403 },
  );
}

export async function POST(request: Request): Promise<Response> {
  const body = (await request.json()) as LoginRequestBody;

  try {
    const authentication = await apiFetch<AuthenticationResponse>("/auth/login", {
      method: "POST",
      body: { email: body.email, password: body.password },
      cache: "no-store",
    });
    await setSessionCookie(authentication.accessToken);
  } catch (error) {
    if (error instanceof ApiError) {
      return Response.json(
        { success: false, data: null, error: { code: error.code, message: error.message, details: error.details } },
        { status: error.status },
      );
    }
    throw error;
  }

  try {
    const currentUser = await apiFetch<CurrentUserResponse>("/auth/me", { cache: "no-store" });
    if (currentUser.role !== "ADMIN") {
      await clearSessionCookie();
      return forbiddenEnvelopeResponse();
    }
  } catch (error) {
    await clearSessionCookie();
    if (error instanceof ApiError) {
      return Response.json(
        { success: false, data: null, error: { code: error.code, message: error.message, details: error.details } },
        { status: error.status },
      );
    }
    throw error;
  }

  return Response.json({ success: true, data: null, error: null });
}
