function requireUrlEnv(key: string, value: string | undefined): string {
  if (!value) {
    throw new Error(`Missing required environment variable: ${key}`);
  }
  try {
    new URL(value);
  } catch {
    throw new Error(`Environment variable ${key} is not a valid URL: ${value}`);
  }
  return value;
}

export const publicApiBaseUrl = requireUrlEnv(
  "NEXT_PUBLIC_API_BASE_URL",
  process.env.NEXT_PUBLIC_API_BASE_URL,
);

export function internalApiBaseUrl(): string {
  return requireUrlEnv("API_INTERNAL_BASE_URL", process.env.API_INTERNAL_BASE_URL);
}
