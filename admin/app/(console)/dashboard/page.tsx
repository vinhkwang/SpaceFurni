import { apiFetch } from "@/lib/api/apiClient";
import type { AdminSummaryResponse } from "@/lib/api/types";
import { StatCard } from "@/components/dashboard/StatCard";

function PublishedProductsIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <rect x="4" y="4" width="7" height="7" rx="1.4" />
      <rect x="13" y="4" width="7" height="7" rx="1.4" />
      <rect x="4" y="13" width="7" height="7" rx="1.4" />
      <rect x="13" y="13" width="7" height="7" rx="1.4" />
    </svg>
  );
}

function OrdersTodayIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <rect x="4" y="5" width="16" height="15" rx="2" />
      <path d="M4 9.5h16M8 3v3.5M16 3v3.5" />
    </svg>
  );
}

function PendingOrdersIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3.2 2" />
    </svg>
  );
}

function LowStockIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 3.5 21 19.5H3z" />
      <path d="M12 10v4M12 16.5v.1" />
    </svg>
  );
}

function NotPartOfMvpScopePanel({ title, subtitle }: { title: string; subtitle: string }) {
  return (
    <div className="flex flex-col rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div className="mb-6.5">
        <div className="text-[15px] font-semibold text-ink">{title}</div>
        <div className="mt-1 text-[11.5px] text-ink-muted">{subtitle}</div>
      </div>
      <div className="flex flex-1 items-center justify-center text-[12.5px] text-ink-muted">Not part of MVP scope</div>
    </div>
  );
}

export default async function DashboardPage() {
  const summary = await apiFetch<AdminSummaryResponse>("/admin/summary", { cache: "no-store" });

  return (
    <div className="flex flex-col gap-4.5">
      <div className="grid grid-cols-4 gap-4.5">
        <StatCard label="Published products" value={summary.publishedProductCount} icon={<PublishedProductsIcon />} />
        <StatCard label="Orders today" value={summary.ordersTodayCount} icon={<OrdersTodayIcon />} />
        <StatCard label="Pending orders" value={summary.pendingOrdersCount} icon={<PendingOrdersIcon />} />
        <StatCard label="Low stock products" value={summary.lowStockProductCount} icon={<LowStockIcon />} />
      </div>
      <div className="grid grid-cols-[1fr_380px] gap-4.5">
        <NotPartOfMvpScopePanel title="Revenue" subtitle="Last 12 months" />
        <NotPartOfMvpScopePanel title="Top departments" subtitle="Share of revenue this month" />
      </div>
    </div>
  );
}
