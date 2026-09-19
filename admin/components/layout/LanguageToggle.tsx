"use client";

import { useRouter } from "next/navigation";
import { useLocale } from "@/lib/i18n/LocaleProvider";
import { LOCALE_COOKIE_NAME, type Locale } from "@/lib/i18n/localeConstants";

const LOCALE_COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 365;

const segmentClassName =
  "flex h-8 w-9 cursor-pointer items-center justify-center rounded-pill text-[10px] font-semibold uppercase tracking-[0.08em] transition-colors duration-200";

export function LanguageToggle() {
  const router = useRouter();
  const { locale, dictionary } = useLocale();

  function selectLocale(nextLocale: Locale) {
    if (nextLocale === locale) {
      return;
    }
    document.cookie = `${LOCALE_COOKIE_NAME}=${nextLocale}; path=/; max-age=${LOCALE_COOKIE_MAX_AGE_SECONDS}`;
    router.refresh();
  }

  return (
    <div
      role="group"
      aria-label={dictionary.languageToggle.switchLanguageAria}
      className="flex h-10 items-center gap-0.5 rounded-pill bg-surface-raised p-1"
    >
      <button
        type="button"
        aria-pressed={locale === "en"}
        onClick={() => selectLocale("en")}
        className={`${segmentClassName} ${
          locale === "en" ? "bg-white text-ink shadow-sm" : "text-ink-muted hover:text-ink"
        }`}
      >
        {dictionary.languageToggle.english}
      </button>
      <button
        type="button"
        aria-pressed={locale === "vi"}
        onClick={() => selectLocale("vi")}
        className={`${segmentClassName} ${
          locale === "vi" ? "bg-white text-ink shadow-sm" : "text-ink-muted hover:text-ink"
        }`}
      >
        {dictionary.languageToggle.vietnamese}
      </button>
    </div>
  );
}
