import type { Locale } from "@/lib/i18n/locale";
import { en } from "@/lib/i18n/dictionaries/en";
import { vi } from "@/lib/i18n/dictionaries/vi";
import type { Dictionary } from "@/lib/i18n/dictionaries/en";

export type { Dictionary };

const dictionariesByLocale: Record<Locale, Dictionary> = { en, vi };

export function getDictionary(locale: Locale): Dictionary {
  return dictionariesByLocale[locale];
}
