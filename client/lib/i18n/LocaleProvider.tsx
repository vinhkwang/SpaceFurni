"use client";

import { createContext, useContext, type ReactNode } from "react";
import type { Dictionary } from "@/lib/i18n/dictionaries/en";
import { getDictionary } from "@/lib/i18n/getDictionary";
import type { Locale } from "@/lib/i18n/localeConstants";

type LocaleContextValue = {
  locale: Locale;
  dictionary: Dictionary;
};

const LocaleContext = createContext<LocaleContextValue | null>(null);

type LocaleProviderProps = {
  locale: Locale;
  children: ReactNode;
};

export function LocaleProvider({ locale, children }: LocaleProviderProps) {
  return (
    <LocaleContext.Provider value={{ locale, dictionary: getDictionary(locale) }}>
      {children}
    </LocaleContext.Provider>
  );
}

export function useLocale(): LocaleContextValue {
  const contextValue = useContext(LocaleContext);
  if (contextValue === null) {
    throw new Error("useLocale must be used within a LocaleProvider");
  }
  return contextValue;
}

export function useDictionary(): Dictionary {
  return useLocale().dictionary;
}
