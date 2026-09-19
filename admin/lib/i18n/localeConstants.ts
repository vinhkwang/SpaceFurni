export type Locale = "en" | "vi";

export const LOCALE_COOKIE_NAME = "spacefurni_admin_locale";
export const DEFAULT_LOCALE: Locale = "en";

export function isLocale(value: string | undefined): value is Locale {
  return value === "en" || value === "vi";
}
