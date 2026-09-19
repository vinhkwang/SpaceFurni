"use client";

import Image from "next/image";
import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { useDictionary } from "@/lib/i18n/LocaleProvider";

type AdminSidebarProps = {
  publishedProductCount: number;
  pendingOrderCount: number;
};

type NavItem = {
  label: string;
  href: string;
  icon: ReactNode;
  count: number | null;
};

function DashboardIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 11 12 4l8 7" />
      <path d="M6 10v9h12v-9" />
    </svg>
  );
}

function ProductsIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <rect x="4" y="4" width="7" height="7" rx="1.4" />
      <rect x="13" y="4" width="7" height="7" rx="1.4" />
      <rect x="4" y="13" width="7" height="7" rx="1.4" />
      <rect x="13" y="13" width="7" height="7" rx="1.4" />
    </svg>
  );
}

function OrdersIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M6 3h12v18l-3-2-3 2-3-2-3 2z" />
      <path d="M9 8h6M9 12h6" />
    </svg>
  );
}

function CustomersIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="9" cy="8" r="3" />
      <path d="M3 20c0-3.3 2.7-6 6-6s6 2.7 6 6" />
      <circle cx="17" cy="9" r="2.4" />
      <path d="M15.5 14.2c2.3.5 4 2.4 4.5 5.8" />
    </svg>
  );
}

function ReviewsIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="m12 4 2.4 4.9 5.4.8-3.9 3.8.9 5.4-4.8-2.5-4.8 2.5.9-5.4-3.9-3.8 5.4-.8z" />
    </svg>
  );
}

function MessagesIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 5h16v11H8l-4 4z" />
    </svg>
  );
}

function SettingsIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="3.2" />
      <path d="M12 3.5v2.4M12 18.1v2.4M20.5 12h-2.4M5.9 12H3.5M17.7 6.3l-1.7 1.7M8 16l-1.7 1.7M17.7 17.7 16 16M8 8 6.3 6.3" />
    </svg>
  );
}

function isNavItemActive(pathname: string, href: string): boolean {
  return pathname === href || pathname.startsWith(`${href}/`);
}

export function AdminSidebar({ publishedProductCount, pendingOrderCount }: AdminSidebarProps) {
  const pathname = usePathname();
  const dictionary = useDictionary();

  const navItems: NavItem[] = [
    { label: dictionary.sidebar.dashboard, href: "/dashboard", icon: <DashboardIcon />, count: null },
    { label: dictionary.sidebar.products, href: "/products", icon: <ProductsIcon />, count: publishedProductCount },
    { label: dictionary.sidebar.orders, href: "/orders", icon: <OrdersIcon />, count: pendingOrderCount },
    { label: dictionary.sidebar.customers, href: "/customers", icon: <CustomersIcon />, count: null },
    { label: dictionary.sidebar.reviews, href: "/reviews", icon: <ReviewsIcon />, count: null },
    { label: dictionary.sidebar.messages, href: "/messages", icon: <MessagesIcon />, count: null },
    { label: dictionary.sidebar.settings, href: "/settings", icon: <SettingsIcon />, count: null },
  ];

  return (
    <aside className="flex w-[262px] shrink-0 flex-col gap-6.5 bg-deep px-4.5 py-7.5">
      <div className="flex items-center gap-3 px-2.5">
        <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-canvas">
          <span className="flex h-[21px] w-14 items-start justify-center overflow-hidden">
            <Image src="/images/logo.png" alt="" width={56} height={35} className="w-14" />
          </span>
        </span>
        <div>
          <div className="flex gap-[0.3em] text-[13px] leading-none tracking-[0.14em] text-white">
            <span className="font-bold">SPACE</span>
            <span className="font-light text-white/70">FURNI</span>
          </div>
          <div className="mt-[3px] text-[9px] uppercase tracking-[0.16em] text-white/40">
            {dictionary.sidebar.adminConsole}
          </div>
        </div>
      </div>

      <nav className="flex flex-col gap-[3px]">
        {navItems.map((item) => {
          const active = isNavItemActive(pathname, item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex h-[46px] items-center gap-3.5 rounded-xl px-4 text-[12.5px] transition-colors duration-200 ${
                active ? "bg-white/10 text-white shadow-[inset_2px_0_0_var(--color-brass)]" : "text-white/60 hover:bg-white/7 hover:text-white"
              }`}
            >
              {item.icon}
              <span className="flex-1">{item.label}</span>
              {item.count !== null && item.count > 0 ? (
                <span className="flex h-5 min-w-[22px] items-center justify-center rounded-pill bg-terracotta px-1.5 text-[10px] font-semibold text-white">
                  {item.count}
                </span>
              ) : null}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
