"use client";

import { useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { submitReviewAction } from "@/lib/reviews/reviewActions";

type ReviewFormProps = {
  productId: string;
  productSlug: string;
  orderItemId: string;
};

const starPositions = [1, 2, 3, 4, 5];

const starOutlinePath =
  "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";

export function ReviewForm({ productId, productSlug, orderItemId }: ReviewFormProps) {
  const router = useRouter();
  const [rating, setRating] = useState(0);
  const [hoveredRating, setHoveredRating] = useState(0);
  const [comment, setComment] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [isSubmitting, startSubmission] = useTransition();

  if (isSubmitted) {
    return <p className="text-[13px] text-ink-soft">Thanks for sharing your review.</p>;
  }

  function submitReview(): void {
    if (rating === 0) {
      setErrorMessage("Pick a star rating first.");
      return;
    }
    startSubmission(async () => {
      const result = await submitReviewAction(productId, productSlug, orderItemId, rating, comment);
      if (!result.success) {
        setErrorMessage(result.errorMessage);
        return;
      }
      setErrorMessage(null);
      setIsSubmitted(true);
      router.refresh();
    });
  }

  const displayedRating = hoveredRating || rating;

  return (
    <div className="flex flex-col gap-3.5 rounded-2xl border border-hairline bg-surface p-4.5">
      <p className="text-[12.5px] font-medium text-ink">You bought this — how did it hold up?</p>

      <div className="flex items-center gap-1" role="radiogroup" aria-label="Rating">
        {starPositions.map((position) => (
          <button
            key={position}
            type="button"
            role="radio"
            aria-checked={rating === position}
            aria-label={`${position} star${position === 1 ? "" : "s"}`}
            onMouseEnter={() => setHoveredRating(position)}
            onMouseLeave={() => setHoveredRating(0)}
            onClick={() => setRating(position)}
            className="cursor-pointer p-0.5 text-brass"
          >
            <svg
              viewBox="0 0 24 24"
              aria-hidden
              className={`h-[18px] w-[18px] ${position <= displayedRating ? "fill-current" : "fill-hairline"}`}
            >
              <path d={starOutlinePath} />
            </svg>
          </button>
        ))}
      </div>

      <textarea
        value={comment}
        onChange={(event) => setComment(event.target.value)}
        placeholder="Share a few words about the piece (optional)"
        rows={3}
        maxLength={2000}
        className="w-full rounded-xl border border-hairline bg-white px-3.5 py-3 text-[12.5px] text-ink outline-none focus:border-terracotta"
      />

      {errorMessage === null ? null : <p className="text-[11.5px] text-terracotta">{errorMessage}</p>}

      <button
        type="button"
        onClick={submitReview}
        disabled={isSubmitting}
        className="flex h-[44px] w-full cursor-pointer items-center justify-center rounded-pill bg-deep text-[11px] font-semibold uppercase tracking-[0.13em] text-white transition-colors duration-300 hover:bg-terracotta disabled:cursor-not-allowed disabled:opacity-50"
      >
        {isSubmitting ? "Submitting…" : "Submit review"}
      </button>
    </div>
  );
}
