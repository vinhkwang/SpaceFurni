import Link from "next/link";
import { apiFetch } from "@/lib/api/apiClient";
import type { AdminCustomerListResponse, CustomerTier } from "@/lib/api/types";
import { getDictionary, type Dictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { StatCard } from "@/components/dashboard/StatCard";
import { CustomerTable, buildCustomersHref } from "@/components/customers/CustomerTable";

const PAGE_SIZE = 20;

const CUSTOMER_TIER_VALUES: CustomerTier[] = ["NEW", "RETURNING", "VIP"];

function tierFilters(dictionary: Dictionary): { value: CustomerTier | undefined; label: string }[] {
  return [
    { value: undefined, label: dictionary.common.all },
    { value: "NEW", label: dictionary.customers.tierLabels.NEW },
    { value: "RETURNING", label: dictionary.customers.tierLabels.RETURNING },
    { value: "VIP", label: dictionary.customers.tierLabels.VIP },
  ];
}

function firstSearchParamValue(rawValue: string | string[] | undefined): string | undefined {
  return Array.isArray(rawValue) ? rawValue[0] : rawValue;
}

function toPageIndex(rawPage: string | undefined): number {
  const parsedPage = Number(rawPage);
  if (!Number.isInteger(parsedPage) || parsedPage < 0) {
    return 0;
  }
  return parsedPage;
}

function resolveTierFilter(rawTier: string | undefined): CustomerTier | undefined {
  return CUSTOMER_TIER_VALUES.find((tier) => tier === rawTier);
}

function filterCount(tierCounts: Partial<Record<CustomerTier, number>>, tier: CustomerTier | undefined): number {
  if (tier === undefined) {
    return CUSTOMER_TIER_VALUES.reduce((total, customerTier) => total + (tierCounts[customerTier] ?? 0), 0);
  }
  return tierCounts[tier] ?? 0;
}

function totalCustomerCount(tierCounts: Partial<Record<CustomerTier, number>>): number {
  return filterCount(tierCounts, undefined);
}

function SearchIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className="h-3.5 w-3.5 shrink-0 stroke-current text-ink-muted"
      fill="none"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="10.5" cy="10.5" r="6.5" />
      <path d="m20 20-4.3-4.3" />
    </svg>
  );
}

function ExportIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className="h-3.5 w-3.5 shrink-0 stroke-current"
      fill="none"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M12 4v11M8 11l4 4 4-4" />
      <path d="M4 17v3h16v-3" />
    </svg>
  );
}

function TotalCustomersIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="9" cy="8" r="3" />
      <path d="M3 20c0-3.3 2.7-6 6-6s6 2.7 6 6" />
      <circle cx="17" cy="9" r="2.4" />
      <path d="M15.5 14.2c2.3.5 4 2.4 4.5 5.8" />
    </svg>
  );
}

function NewCustomerIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="10" cy="9" r="3.3" />
      <path d="M3.5 20c0-3.4 2.9-6.2 6.5-6.2s6.5 2.8 6.5 6.2" />
      <path d="M18.5 6v6M15.5 9h6" />
    </svg>
  );
}

function ReturningCustomerIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 12a8 8 0 0 1 13.7-5.7L20 8.5" />
      <path d="M20 4v4.5h-4.5" />
      <path d="M20 12a8 8 0 0 1-13.7 5.7L4 15.5" />
      <path d="M4 20v-4.5h4.5" />
    </svg>
  );
}

function VipCustomerIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="m4 8 3.2 2.4L12 5l4.8 5.4L20 8l-1.6 9H5.6z" />
    </svg>
  );
}

export default async function CustomersPage({ searchParams }: PageProps<"/customers">) {
  const [resolvedSearchParams, dictionary] = await Promise.all([searchParams, getLocale().then(getDictionary)]);
  const tierFilter = resolveTierFilter(firstSearchParamValue(resolvedSearchParams.tier));
  const query = firstSearchParamValue(resolvedSearchParams.q) ?? "";
  const pageIndex = toPageIndex(firstSearchParamValue(resolvedSearchParams.page));

  const apiQuery = new URLSearchParams({ page: String(pageIndex), size: String(PAGE_SIZE) });
  if (tierFilter) {
    apiQuery.set("tier", tierFilter);
  }
  if (query) {
    apiQuery.set("q", query);
  }

  const customerList = await apiFetch<AdminCustomerListResponse>(`/admin/customers?${apiQuery.toString()}`, {
    cache: "no-store",
  });

  const exportQuery = new URLSearchParams();
  if (tierFilter) {
    exportQuery.set("tier", tierFilter);
  }
  if (query) {
    exportQuery.set("q", query);
  }
  const exportHref = exportQuery.toString()
    ? `/api/admin/customers/export?${exportQuery.toString()}`
    : "/api/admin/customers/export";

  return (
    <div className="flex flex-col gap-4.5">
      <div className="grid grid-cols-4 gap-4.5">
        <StatCard
          label={dictionary.customers.statTotalCustomers}
          value={totalCustomerCount(customerList.tierCounts)}
          icon={<TotalCustomersIcon />}
        />
        <StatCard
          label={dictionary.customers.tierLabels.NEW}
          value={customerList.tierCounts.NEW ?? 0}
          icon={<NewCustomerIcon />}
        />
        <StatCard
          label={dictionary.customers.tierLabels.RETURNING}
          value={customerList.tierCounts.RETURNING ?? 0}
          icon={<ReturningCustomerIcon />}
        />
        <StatCard
          label={dictionary.customers.tierLabels.VIP}
          value={customerList.tierCounts.VIP ?? 0}
          icon={<VipCustomerIcon />}
        />
      </div>

      <div className="rounded-2xl border border-hairline-soft bg-white p-6.5">
        <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
          <div className="flex flex-wrap gap-2">
            {tierFilters(dictionary).map((filter) => (
              <Link
                key={filter.value ?? "all"}
                href={buildCustomersHref(filter.value, query, 0)}
                className={`flex h-10 items-center gap-2 rounded-pill border px-4.5 text-[11.5px] transition-colors duration-200 ${
                  filter.value === tierFilter
                    ? "border-deep bg-deep text-white"
                    : "border-hairline text-ink hover:border-hairline-soft"
                }`}
              >
                {filter.label}
                <span className="opacity-60">{filterCount(customerList.tierCounts, filter.value)}</span>
              </Link>
            ))}
          </div>

          <div className="flex items-center gap-3">
            <form className="flex h-11 w-64 items-center gap-2.5 rounded-pill bg-surface-raised px-4.5">
              {tierFilter ? <input type="hidden" name="tier" value={tierFilter} /> : null}
              <SearchIcon />
              <input
                type="text"
                name="q"
                defaultValue={query}
                placeholder={dictionary.customers.searchPlaceholder}
                aria-label={dictionary.customers.searchAriaLabel}
                className="flex-1 bg-transparent text-[12.5px] text-ink outline-none placeholder:text-ink-muted"
              />
            </form>
            <a
              href={exportHref}
              className="flex h-11 items-center gap-2 rounded-pill border border-hairline px-4.5 text-[11px] font-semibold uppercase tracking-[0.13em] text-ink transition-colors duration-200 hover:border-deep"
            >
              <ExportIcon />
              {dictionary.customers.exportCsv}
            </a>
          </div>
        </div>

        <div className="mb-3 text-[11.5px] text-ink-muted">
          {dictionary.customers.resultCount(customerList.customers.totalElements, query)}
        </div>

        <CustomerTable
          customers={customerList.customers.content}
          currentPage={customerList.customers.page}
          totalPages={customerList.customers.totalPages}
          tier={tierFilter}
          query={query}
          dictionary={dictionary}
        />
      </div>
    </div>
  );
}
