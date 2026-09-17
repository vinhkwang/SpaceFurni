"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import type { ReviewStatus } from "@/lib/api/types";
import { updateReviewStatusAction } from "@/lib/reviews/reviewActions";
import { useToast } from "@/components/ui/Toast";

type ReviewRowActionsProps = {
  reviewId: string;
  status: ReviewStatus;
};

const actionButtonClassName =
  "flex h-7.5 items-center justify-center rounded-lg border border-hairline px-3 text-[11px] font-medium text-ink-muted transition-colors duration-200 hover:border-deep hover:text-ink disabled:cursor-not-allowed disabled:opacity-40";

export function ReviewRowActions({ reviewId, status }: ReviewRowActionsProps) {
  const router = useRouter();
  const { showToast } = useToast();
  const [isPending, setIsPending] = useState(false);

  async function toggleStatus() {
    const nextStatus: ReviewStatus = status === "PUBLISHED" ? "HIDDEN" : "PUBLISHED";
    setIsPending(true);
    const result = await updateReviewStatusAction(reviewId, nextStatus);
    setIsPending(false);
    if (result.success) {
      showToast(nextStatus === "HIDDEN" ? "Review hidden." : "Review restored.");
      router.refresh();
    }
  }

  return (
    <div className="flex items-center justify-end">
      <button type="button" disabled={isPending} onClick={toggleStatus} className={actionButtonClassName}>
        {status === "PUBLISHED" ? "Hide" : "Restore"}
      </button>
    </div>
  );
}
