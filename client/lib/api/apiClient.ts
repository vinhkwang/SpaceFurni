import { cookies } from "next/headers";
import { internalApiBaseUrl } from "@/lib/config/environment";
import { ApiError, type ApiErrorDetails } from "@/lib/api/ApiError";

const SESSION_COOKIE_NAME = "spacefurni_session";
const GUEST_CART_TOKEN_COOKIE_NAME = "spacefurni_guest_cart_token";
const GUEST_TOKEN_REQUEST_HEADER = "X-Guest-Token";

type ApiSuccessEnvelope<T> = {
  success: true;
  data: T;
  error: null;
};

type ApiFailureEnvelope = {
  success: false;
  data: null;
  error: {
    code: string;
    message: string;
    details: ApiErrorDetails | null;
  };
};

type ApiEnvelope<T> = ApiSuccessEnvelope<T> | ApiFailureEnvelope;

export type ApiRequestMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

export type ApiRequestOptions = {
  method?: ApiRequestMethod;
  body?: unknown;
  cache?: RequestCache;
  next?: NextFetchRequestConfig;
};

function isCartRequestPath(path: string): boolean {
  return path.startsWith("/cart");
}

async function resolveRequestHeaders(path: string): Promise<Record<string, string>> {
  const cookieStore = await cookies();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  const sessionToken = cookieStore.get(SESSION_COOKIE_NAME)?.value;
  if (sessionToken) {
    headers.Authorization = `Bearer ${sessionToken}`;
  }

  if (isCartRequestPath(path)) {
    const guestCartToken = cookieStore.get(GUEST_CART_TOKEN_COOKIE_NAME)?.value;
    if (guestCartToken) {
      headers[GUEST_TOKEN_REQUEST_HEADER] = guestCartToken;
    }
  }

  return headers;
}

export async function apiFetch<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
  const headers = await resolveRequestHeaders(path);

  const response = await fetch(`${internalApiBaseUrl()}/api/v1${path}`, {
    method: options.method ?? "GET",
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
    cache: options.cache,
    next: options.next,
  });

  const envelope = (await response.json()) as ApiEnvelope<T>;

  if (!envelope.success) {
    throw new ApiError(envelope.error.code, envelope.error.message, response.status, envelope.error.details);
  }

  return envelope.data;
}
