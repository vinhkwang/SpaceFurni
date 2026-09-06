"use client";

import { usePathname } from "next/navigation";

type AdminTopBarProps = {
  userFullName: string;
  userRole: string;
};

type RouteMeta = {
  title: string;
  subtitle: string;
};

const ROUTE_META: Record<string, RouteMeta> = {
  "/dashboard": { title: "Dashboard", subtitle: "An overview of the store" },
  "/products": { title: "Products", subtitle: "Catalogue and stock levels" },
  "/orders": { title: "Orders", subtitle: "Every order placed on the storefront" },
  "/customers": { title: "Customers", subtitle: "Everyone who has bought from the store" },
  "/messages": { title: "Messages", subtitle: "Customer enquiries" },
  "/settings": { title: "Settings", subtitle: "Store details, delivery rules and team access" },
};

const DEFAULT_ROUTE_META: RouteMeta = { title: "Admin console", subtitle: "" };

function resolveRouteMeta(pathname: string): RouteMeta {
  const matchedRoute = Object.keys(ROUTE_META).find(
    (route) => pathname === route || pathname.startsWith(`${route}/`),
  );
  return matchedRoute ? ROUTE_META[matchedRoute] : DEFAULT_ROUTE_META;
}

function initialsFor(fullName: string): string {
  return fullName
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((namePart) => namePart[0]?.toUpperCase())
    .join("");
}

export function AdminTopBar({ userFullName, userRole }: AdminTopBarProps) {
  const pathname = usePathname();
  const { title, subtitle } = resolveRouteMeta(pathname);

  return (
    <header className="flex h-[82px] shrink-0 items-center justify-between border-b border-hairline bg-white px-8.5">
      <div>
        <div className="text-[18px] font-semibold tracking-[-0.005em] text-ink">{title}</div>
        {subtitle ? <div className="mt-[3px] text-[11.5px] text-ink-muted">{subtitle}</div> : null}
      </div>

      <div className="flex items-center gap-3.5 border-l border-hairline pl-3.5">
        <span className="flex h-9.5 w-9.5 items-center justify-center rounded-pill bg-deep text-[12.5px] font-semibold text-white">
          {initialsFor(userFullName)}
        </span>
        <div>
          <div className="text-[12.5px] font-semibold text-ink">{userFullName}</div>
          <div className="text-[10.5px] text-ink-muted">{userRole}</div>
        </div>
      </div>
    </header>
  );
}
