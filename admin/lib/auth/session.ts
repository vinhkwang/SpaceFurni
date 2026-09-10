import { cookies } from "next/headers";

export const ADMIN_SESSION_COOKIE_NAME = "spacefurni_admin_session";
export const ADMIN_REFRESH_TOKEN_COOKIE_NAME = "spacefurni_admin_refresh_token";

const HTTP_ONLY_COOKIE_OPTIONS = {
  httpOnly: true,
  secure: true,
  sameSite: "lax" as const,
  path: "/",
};

export async function setSessionCookies(accessToken: string, refreshToken: string): Promise<void> {
  const cookieStore = await cookies();
  cookieStore.set(ADMIN_SESSION_COOKIE_NAME, accessToken, HTTP_ONLY_COOKIE_OPTIONS);
  cookieStore.set(ADMIN_REFRESH_TOKEN_COOKIE_NAME, refreshToken, HTTP_ONLY_COOKIE_OPTIONS);
}

export async function clearSessionCookies(): Promise<void> {
  const cookieStore = await cookies();
  cookieStore.delete(ADMIN_SESSION_COOKIE_NAME);
  cookieStore.delete(ADMIN_REFRESH_TOKEN_COOKIE_NAME);
}

export async function getSessionToken(): Promise<string | undefined> {
  const cookieStore = await cookies();
  return cookieStore.get(ADMIN_SESSION_COOKIE_NAME)?.value;
}

export async function getRefreshToken(): Promise<string | undefined> {
  const cookieStore = await cookies();
  return cookieStore.get(ADMIN_REFRESH_TOKEN_COOKIE_NAME)?.value;
}
