import Link from "next/link";
import { apiFetch } from "@/lib/api/apiClient";
import type { AdminReviewRowResponse, PageResponse, ReviewStatus } from "@/lib/api/types";
import { ReviewsTable, buildReviewsHref } from "@/components/reviews/ReviewsTable";

const PAGE_SIZE = 20;

const REVIEW_STATUS_VALUES: ReviewStatus[] = ["PUBLISHED", "HIDDEN"];

const STATUS_FILTERS: { value: ReviewStatus | undefined; label: string }[] = [
  { value: undefined, label: "All" },
  { value: "PUBLISHED", label: "Published" },
  { value: "HIDDEN", label: "Hidden" },
];

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

function resolveStatusFilter(rawStatus: string | undefined): ReviewStatus | undefined {
  return REVIEW_STATUS_VALUES.find((reviewStatus) => reviewStatus === rawStatus);
}

export default async function ReviewsPage({ searchParams }: PageProps<"/reviews">) {
  const resolvedSearchParams = await searchParams;
  const statusFilter = resolveStatusFilter(firstSearchParamValue(resolvedSearchParams.status));
  const pageIndex = toPageIndex(firstSearchParamValue(resolvedSearchParams.page));

  const apiQuery = new URLSearchParams({ page: String(pageIndex), size: String(PAGE_SIZE) });
  if (statusFilter) {
    apiQuery.set("status", statusFilter);
  }

  const reviewPage = await apiFetch<PageResponse<AdminReviewRowResponse>>(`/admin/reviews?${apiQuery.toString()}`, {
    cache: "no-store",
  });

  return (
    <div className="rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div className="mb-6 flex flex-wrap gap-2">
        {STATUS_FILTERS.map((filter) => (
          <Link
            key={filter.label}
            href={buildReviewsHref(filter.value, 0)}
            className={`flex h-10 items-center gap-2 rounded-pill border px-4.5 text-[11.5px] transition-colors duration-200 ${
              filter.value === statusFilter
                ? "border-deep bg-deep text-white"
                : "border-hairline text-ink hover:border-hairline-soft"
            }`}
          >
            {filter.label}
          </Link>
        ))}
      </div>

      <ReviewsTable
        reviews={reviewPage.content}
        currentPage={reviewPage.page}
        totalPages={reviewPage.totalPages}
        status={statusFilter}
      />
    </div>
  );
}
