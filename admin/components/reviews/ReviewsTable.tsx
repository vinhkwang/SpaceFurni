import Link from "next/link";
import type { AdminReviewRowResponse, ReviewStatus } from "@/lib/api/types";
import { formatOrderPlacedAt } from "@/lib/formatting/formatOrderPlacedAt";
import { REVIEW_STATUS_PRESENTATION } from "@/lib/reviews/reviewStatusPresentation";
import { ReviewRowActions } from "@/components/reviews/ReviewRowActions";

type ReviewsTableProps = {
  reviews: AdminReviewRowResponse[];
  currentPage: number;
  totalPages: number;
  status: ReviewStatus | undefined;
};

const tableRowGridClassName = "grid grid-cols-[130px_130px_90px_1.6fr_110px_100px_90px] items-center gap-3.5";

export function buildReviewsHref(status: ReviewStatus | undefined, page: number): string {
  const params = new URLSearchParams();
  if (status) {
    params.set("status", status);
  }
  if (page > 0) {
    params.set("page", String(page));
  }
  const queryString = params.toString();
  return queryString ? `/reviews?${queryString}` : "/reviews";
}

function truncatedId(id: string): string {
  return id.slice(0, 8);
}

function starGlyphs(rating: number): string {
  return "★".repeat(rating) + "☆".repeat(5 - rating);
}

function ChevronIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className={`h-[7px] w-[7px] stroke-current ${className ?? ""}`}
      fill="none"
      strokeWidth={3.5}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="m9 5 7 7-7 7" />
    </svg>
  );
}

function pageNumbers(totalPages: number): number[] {
  return Array.from({ length: totalPages }, (_, pageOffset) => pageOffset + 1);
}

export function ReviewsTable({ reviews, currentPage, totalPages, status }: ReviewsTableProps) {
  return (
    <div>
      <div
        className={`${tableRowGridClassName} border-b border-hairline-soft px-2.5 pb-3.5 text-[10px] uppercase tracking-[0.14em] text-ink-muted`}
      >
        <span>Product</span>
        <span>Customer</span>
        <span>Rating</span>
        <span>Comment</span>
        <span>Submitted</span>
        <span className="text-center">Status</span>
        <span className="text-right">Actions</span>
      </div>

      {reviews.map((review) => {
        const submittedAt = formatOrderPlacedAt(review.createdAt);
        const statusPresentation = REVIEW_STATUS_PRESENTATION[review.status];
        return (
          <div
            key={review.id}
            className={`${tableRowGridClassName} border-b border-hairline-soft/70 px-2.5 py-3.5`}
          >
            <span className="font-mono text-[12px] text-ink-soft">{truncatedId(review.productId)}</span>
            <span className="font-mono text-[12px] text-ink-soft">{truncatedId(review.userId)}</span>
            <span className="text-[13px] tracking-[0.05em] text-brass">{starGlyphs(review.rating)}</span>
            <span className="truncate text-[12.5px] text-ink-soft">{review.comment ?? "—"}</span>
            <div>
              <div className="text-[12px] text-ink">{submittedAt.date}</div>
              <div className="mt-0.5 text-[11px] text-ink-muted">{submittedAt.time}</div>
            </div>
            <span className="justify-self-center">
              <span
                className="rounded-pill px-3 py-1.5 text-[10.5px] font-semibold"
                style={{ backgroundColor: statusPresentation.backgroundColor, color: statusPresentation.textColor }}
              >
                {statusPresentation.label}
              </span>
            </span>
            <ReviewRowActions reviewId={review.id} status={review.status} />
          </div>
        );
      })}

      {reviews.length === 0 ? (
        <div className="py-15 text-center text-[13px] text-ink-muted">No reviews match this filter.</div>
      ) : null}

      {totalPages > 1 ? (
        <nav aria-label="Pagination" className="flex items-center justify-center gap-1.5 pt-5.5">
          {currentPage > 0 ? (
            <Link
              href={buildReviewsHref(status, currentPage - 1)}
              aria-label="Previous page"
              className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline text-ink-muted transition-colors duration-200 hover:border-deep hover:text-ink"
            >
              <ChevronIcon className="rotate-180" />
            </Link>
          ) : (
            <span
              aria-hidden
              className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline-soft text-ink-muted/40"
            >
              <ChevronIcon className="rotate-180" />
            </span>
          )}

          {pageNumbers(totalPages).map((pageNumber) =>
            pageNumber - 1 === currentPage ? (
              <span
                key={pageNumber}
                aria-current="page"
                className="flex h-8.5 w-8.5 items-center justify-center rounded-xl bg-deep text-[12px] font-semibold text-white"
              >
                {pageNumber}
              </span>
            ) : (
              <Link
                key={pageNumber}
                href={buildReviewsHref(status, pageNumber - 1)}
                aria-label={`Page ${pageNumber}`}
                className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline text-[12px] transition-colors duration-200 hover:border-deep"
              >
                {pageNumber}
              </Link>
            ),
          )}

          {currentPage < totalPages - 1 ? (
            <Link
              href={buildReviewsHref(status, currentPage + 1)}
              aria-label="Next page"
              className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline text-ink-muted transition-colors duration-200 hover:border-deep hover:text-ink"
            >
              <ChevronIcon />
            </Link>
          ) : (
            <span
              aria-hidden
              className="flex h-8.5 w-8.5 items-center justify-center rounded-xl border border-hairline-soft text-ink-muted/40"
            >
              <ChevronIcon />
            </span>
          )}
        </nav>
      ) : null}
    </div>
  );
}
