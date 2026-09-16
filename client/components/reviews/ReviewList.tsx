import Link from "next/link";
import { Rating } from "@/components/ui/Rating";
import type { PageResponse, ReviewResponse } from "@/lib/api/types";

type ReviewListProps = {
  reviewPage: PageResponse<ReviewResponse>;
  productSlug: string;
};

const reviewDateFormatter = new Intl.DateTimeFormat("en-GB", { day: "numeric", month: "short", year: "numeric" });

const chevronIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-3 w-3 stroke-current" fill="none" strokeWidth={2.5} strokeLinecap="round" strokeLinejoin="round">
    <path d="m9 5 7 7-7 7" />
  </svg>
);

export function ReviewList({ reviewPage, productSlug }: ReviewListProps) {
  if (reviewPage.content.length === 0) {
    return <p className="text-[13px] text-ink-soft">No reviews yet — be the first to share your thoughts.</p>;
  }

  const currentPageNumber = reviewPage.page + 1;

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-6">
        {reviewPage.content.map((review) => (
          <div key={review.id} className="border-b border-hairline-soft pb-6 last:border-b-0 last:pb-0">
            <div className="mb-2 flex items-center justify-between">
              <Rating value={review.rating} />
              <span className="text-[11px] text-ink-muted">{reviewDateFormatter.format(new Date(review.createdAt))}</span>
            </div>
            {review.comment === null ? null : (
              <p className="text-[13px] leading-[1.7] text-ink-soft">{review.comment}</p>
            )}
          </div>
        ))}
      </div>

      {reviewPage.totalPages > 1 ? (
        <nav aria-label="Review pagination" className="flex items-center justify-center gap-4">
          {currentPageNumber > 1 ? (
            <Link
              href={`/products/${productSlug}?reviewsPage=${currentPageNumber - 1}`}
              aria-label="Previous reviews"
              className="flex h-9 w-9 items-center justify-center rounded-full border border-hairline-soft transition duration-250 hover:bg-surface"
            >
              <span className="rotate-180">{chevronIcon}</span>
            </Link>
          ) : null}

          <span className="text-[11.5px] uppercase tracking-[0.1em] text-ink-muted">
            Page {currentPageNumber} of {reviewPage.totalPages}
          </span>

          {currentPageNumber < reviewPage.totalPages ? (
            <Link
              href={`/products/${productSlug}?reviewsPage=${currentPageNumber + 1}`}
              aria-label="Next reviews"
              className="flex h-9 w-9 items-center justify-center rounded-full border border-hairline-soft transition duration-250 hover:bg-surface"
            >
              {chevronIcon}
            </Link>
          ) : null}
        </nav>
      ) : null}
    </div>
  );
}
