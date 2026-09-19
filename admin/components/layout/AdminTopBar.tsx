"use client";

import { usePathname } from "next/navigation";
import type { CurrentUserResponse } from "@/lib/api/types";
import { useDictionary } from "@/lib/i18n/LocaleProvider";
import type { Dictionary } from "@/lib/i18n/dictionaries/en";
import { AdminAccountMenu } from "@/components/layout/AdminAccountMenu";
import { LanguageToggle } from "@/components/layout/LanguageToggle";

type AdminTopBarProps = {
  currentUser: CurrentUserResponse;
  userRole: string;
};

type RouteMeta = {
  title: string;
  subtitle: string;
};

function resolveRouteMeta(dictionary: Dictionary, pathname: string): RouteMeta {
  const routeMetaByPath: Record<string, RouteMeta> = {
    "/dashboard": dictionary.topbar.routeMeta.dashboard,
    "/products": dictionary.topbar.routeMeta.products,
    "/orders": dictionary.topbar.routeMeta.orders,
    "/customers": dictionary.topbar.routeMeta.customers,
    "/reviews": dictionary.topbar.routeMeta.reviews,
    "/messages": dictionary.topbar.routeMeta.messages,
    "/settings": dictionary.topbar.routeMeta.settings,
    "/profile": dictionary.topbar.routeMeta.profile,
  };
  const matchedRoute = Object.keys(routeMetaByPath).find(
    (route) => pathname === route || pathname.startsWith(`${route}/`),
  );
  return matchedRoute ? routeMetaByPath[matchedRoute] : { title: dictionary.topbar.defaultTitle, subtitle: "" };
}

export function AdminTopBar({ currentUser, userRole }: AdminTopBarProps) {
  const pathname = usePathname();
  const dictionary = useDictionary();
  const { title, subtitle } = resolveRouteMeta(dictionary, pathname);

  return (
    <header className="flex h-[82px] shrink-0 items-center justify-between border-b border-hairline bg-white px-8.5">
      <div>
        <div className="text-[18px] font-semibold tracking-[-0.005em] text-ink">{title}</div>
        {subtitle ? <div className="mt-[3px] text-[11.5px] text-ink-muted">{subtitle}</div> : null}
      </div>

      <div className="flex items-center gap-4.5">
        <LanguageToggle />
        <AdminAccountMenu currentUser={currentUser} userRole={userRole} />
      </div>
    </header>
  );
}
