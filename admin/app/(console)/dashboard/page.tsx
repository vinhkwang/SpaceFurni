import { apiFetch } from "@/lib/api/apiClient";
import type { AdminSummaryResponse, DepartmentRevenueShareResponse, MonthlyRevenuePointResponse } from "@/lib/api/types";
import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { StatCard } from "@/components/dashboard/StatCard";
import { RevenueChart } from "@/components/dashboard/RevenueChart";
import { DepartmentShareChart } from "@/components/dashboard/DepartmentShareChart";

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

export default async function DashboardPage() {
  const [summary, revenuePoints, departmentShares, locale] = await Promise.all([
    apiFetch<AdminSummaryResponse>("/admin/summary", { cache: "no-store" }),
    apiFetch<MonthlyRevenuePointResponse[]>("/admin/dashboard/revenue?months=12", { cache: "no-store" }),
    apiFetch<DepartmentRevenueShareResponse[]>("/admin/dashboard/department-share", { cache: "no-store" }),
    getLocale(),
  ]);
  const dictionary = getDictionary(locale);

  return (
    <div className="flex flex-col gap-4.5">
      <div className="grid grid-cols-4 gap-4.5">
        <StatCard label={dictionary.dashboard.publishedProducts} value={summary.publishedProductCount} icon={<PublishedProductsIcon />} />
        <StatCard label={dictionary.dashboard.ordersToday} value={summary.ordersTodayCount} icon={<OrdersTodayIcon />} />
        <StatCard label={dictionary.dashboard.pendingOrders} value={summary.pendingOrdersCount} icon={<PendingOrdersIcon />} />
        <StatCard label={dictionary.dashboard.lowStockProducts} value={summary.lowStockProductCount} icon={<LowStockIcon />} />
      </div>
      <div className="grid grid-cols-[1fr_380px] gap-4.5">
        <RevenueChart points={revenuePoints} dictionary={dictionary} locale={locale} />
        <DepartmentShareChart shares={departmentShares} dictionary={dictionary} />
      </div>
    </div>
  );
}
