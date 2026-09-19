import type { ReactNode } from "react";
import { apiFetch } from "@/lib/api/apiClient";
import type { AdminSummaryResponse, CurrentUserResponse } from "@/lib/api/types";
import { getDictionary, type Dictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { AdminSidebar } from "@/components/layout/AdminSidebar";
import { AdminTopBar } from "@/components/layout/AdminTopBar";
import { ToastProvider } from "@/components/ui/Toast";

function roleLabel(dictionary: Dictionary, role: string): string {
  return role === "ADMIN" ? dictionary.common.administrator : dictionary.common.customer;
}

export default async function ConsoleLayout({ children }: { children: ReactNode }) {
  const [summary, currentUser, locale] = await Promise.all([
    apiFetch<AdminSummaryResponse>("/admin/summary", { cache: "no-store" }),
    apiFetch<CurrentUserResponse>("/auth/me", { cache: "no-store" }),
    getLocale(),
  ]);
  const dictionary = getDictionary(locale);

  return (
    <ToastProvider>
      <div className="flex min-h-screen bg-surface">
        <AdminSidebar
          publishedProductCount={summary.publishedProductCount}
          pendingOrderCount={summary.pendingOrdersCount}
        />
        <div className="flex min-w-0 flex-1 flex-col">
          <AdminTopBar currentUser={currentUser} userRole={roleLabel(dictionary, currentUser.role)} />
          <main className="flex-1 bg-canvas px-8.5 py-7.5">{children}</main>
        </div>
      </div>
    </ToastProvider>
  );
}
