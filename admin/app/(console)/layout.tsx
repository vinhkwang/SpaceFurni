import type { ReactNode } from "react";
import { apiFetch } from "@/lib/api/apiClient";
import type { AdminSummaryResponse, CurrentUserResponse } from "@/lib/api/types";
import { AdminSidebar } from "@/components/layout/AdminSidebar";
import { AdminTopBar } from "@/components/layout/AdminTopBar";

function capitalizeRole(role: string): string {
  return role.charAt(0) + role.slice(1).toLowerCase();
}

export default async function ConsoleLayout({ children }: { children: ReactNode }) {
  const [summary, currentUser] = await Promise.all([
    apiFetch<AdminSummaryResponse>("/admin/summary", { cache: "no-store" }),
    apiFetch<CurrentUserResponse>("/auth/me", { cache: "no-store" }),
  ]);

  return (
    <div className="flex min-h-screen bg-surface">
      <AdminSidebar
        publishedProductCount={summary.publishedProductCount}
        pendingOrderCount={summary.pendingOrdersCount}
      />
      <div className="flex min-w-0 flex-1 flex-col">
        <AdminTopBar userFullName={currentUser.fullName} userRole={capitalizeRole(currentUser.role)} />
        <main className="flex-1 bg-canvas px-8.5 py-7.5">{children}</main>
      </div>
    </div>
  );
}
