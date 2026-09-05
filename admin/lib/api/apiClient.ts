import { internalApiBaseUrl } from "@/lib/config/environment";
import { ApiError, type ApiErrorDetails } from "@/lib/api/ApiError";
import { getSessionToken } from "@/lib/auth/session";

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
  headers?: Record<string, string>;
};

async function resolveRequestHeaders(): Promise<Record<string, string>> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  const sessionToken = await getSessionToken();
  if (sessionToken) {
    headers.Authorization = `Bearer ${sessionToken}`;
  }

  return headers;
}

export async function apiFetch<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
  const headers = { ...(await resolveRequestHeaders()), ...options.headers };

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
