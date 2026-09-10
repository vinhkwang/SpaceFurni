import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { isAccessTokenExpiringSoon } from "@/lib/auth/jwt";
import { ADMIN_REFRESH_TOKEN_COOKIE_NAME, ADMIN_SESSION_COOKIE_NAME } from "@/lib/auth/session";
import { internalApiBaseUrl } from "@/lib/config/environment";

const HTTP_ONLY_COOKIE_OPTIONS = {
  httpOnly: true,
  secure: true,
  sameSite: "lax" as const,
  path: "/",
};

type RefreshedTokens = {
  accessToken: string;
  refreshToken: string;
};

async function refreshAccessToken(refreshToken: string): Promise<RefreshedTokens | null> {
  try {
    const response = await fetch(`${internalApiBaseUrl()}/api/v1/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
      cache: "no-store",
    });
    const envelope = (await response.json()) as
      | { success: true; data: RefreshedTokens }
      | { success: false };
    return envelope.success ? envelope.data : null;
  } catch {
    return null;
  }
}

function redirectToLogin(request: NextRequest): NextResponse {
  const loginUrl = new URL("/login", request.url);
  loginUrl.searchParams.set("redirect", request.nextUrl.pathname + request.nextUrl.search);
  const response = NextResponse.redirect(loginUrl);
  response.cookies.delete(ADMIN_SESSION_COOKIE_NAME);
  response.cookies.delete(ADMIN_REFRESH_TOKEN_COOKIE_NAME);
  return response;
}

export async function proxy(request: NextRequest): Promise<NextResponse> {
  const accessToken = request.cookies.get(ADMIN_SESSION_COOKIE_NAME)?.value;
  const refreshToken = request.cookies.get(ADMIN_REFRESH_TOKEN_COOKIE_NAME)?.value;

  if (accessToken && !isAccessTokenExpiringSoon(accessToken)) {
    return NextResponse.next();
  }

  if (!refreshToken) {
    return redirectToLogin(request);
  }

  const refreshed = await refreshAccessToken(refreshToken);
  if (!refreshed) {
    return redirectToLogin(request);
  }

  request.cookies.set(ADMIN_SESSION_COOKIE_NAME, refreshed.accessToken);
  request.cookies.set(ADMIN_REFRESH_TOKEN_COOKIE_NAME, refreshed.refreshToken);

  const response = NextResponse.next({ request: { headers: request.headers } });
  response.cookies.set(ADMIN_SESSION_COOKIE_NAME, refreshed.accessToken, HTTP_ONLY_COOKIE_OPTIONS);
  response.cookies.set(ADMIN_REFRESH_TOKEN_COOKIE_NAME, refreshed.refreshToken, HTTP_ONLY_COOKIE_OPTIONS);
  return response;
}

export const config = {
  matcher: ["/((?!login|api|_next/static|_next/image|images|favicon.ico).*)"],
};
