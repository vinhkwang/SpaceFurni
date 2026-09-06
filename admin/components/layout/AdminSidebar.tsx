"use client";

import Image from "next/image";
import Link from "next/link";
import { useRouter, usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { publicStorefrontUrl } from "@/lib/config/environment";

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

function StoreIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-3.5 w-3.5 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 9.5 5.5 4h13L20 9.5" />
      <path d="M4 9.5a2.5 2.5 0 0 0 5 0 2.5 2.5 0 0 0 5 0 2.5 2.5 0 0 0 5 0" />
      <path d="M5 10v10h14V10" />
    </svg>
  );
}

function SignOutIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-3.5 w-3.5 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M9 4H5v16h4" />
      <path d="M12 12h9M17.5 8.5 21 12l-3.5 3.5" />
    </svg>
  );
}

function isNavItemActive(pathname: string, href: string): boolean {
  return pathname === href || pathname.startsWith(`${href}/`);
}

export function AdminSidebar({ publishedProductCount, pendingOrderCount }: AdminSidebarProps) {
  const pathname = usePathname();
  const router = useRouter();

  const navItems: NavItem[] = [
    { label: "Dashboard", href: "/dashboard", icon: <DashboardIcon />, count: null },
    { label: "Products", href: "/products", icon: <ProductsIcon />, count: publishedProductCount },
    { label: "Orders", href: "/orders", icon: <OrdersIcon />, count: pendingOrderCount },
    { label: "Customers", href: "/customers", icon: <CustomersIcon />, count: null },
    { label: "Messages", href: "/messages", icon: <MessagesIcon />, count: null },
    { label: "Settings", href: "/settings", icon: <SettingsIcon />, count: null },
  ];

  async function signOut() {
    await fetch("/api/session/logout", { method: "POST" });
    router.replace("/login");
    router.refresh();
  }

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
          <div className="mt-[3px] text-[9px] uppercase tracking-[0.16em] text-white/40">Admin console</div>
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

      <div className="mt-auto flex flex-col gap-[3px] border-t border-white/10 pt-5.5">
        <a
          href={publicStorefrontUrl}
          className="flex h-11 items-center gap-3.5 rounded-xl px-4 text-[12.5px] text-white/60 transition-colors duration-200 hover:bg-white/7 hover:text-white"
        >
          <StoreIcon />
          Back to store
        </a>
        <button
          type="button"
          onClick={signOut}
          className="flex h-11 cursor-pointer items-center gap-3.5 rounded-xl px-4 text-[12.5px] text-white/60 transition-colors duration-200 hover:bg-white/7 hover:text-white"
        >
          <SignOutIcon />
          Sign out
        </button>
      </div>
    </aside>
  );
}
