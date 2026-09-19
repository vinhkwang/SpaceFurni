import { cookies } from "next/headers";
import { DEFAULT_LOCALE, isLocale, LOCALE_COOKIE_NAME, type Locale } from "@/lib/i18n/localeConstants";

export { DEFAULT_LOCALE, isLocale, LOCALE_COOKIE_NAME, type Locale };

export async function getLocale(): Promise<Locale> {
  const cookieStore = await cookies();
  const cookieValue = cookieStore.get(LOCALE_COOKIE_NAME)?.value;
  return isLocale(cookieValue) ? cookieValue : DEFAULT_LOCALE;
}
