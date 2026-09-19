import type { ReviewStatus } from "@/lib/api/types";
import type { Dictionary } from "@/lib/i18n/dictionaries/en";

export type ReviewStatusPresentation = {
  label: string;
  backgroundColor: string;
  textColor: string;
};

const REVIEW_STATUS_COLORS: Record<ReviewStatus, { backgroundColor: string; textColor: string }> = {
  PUBLISHED: { backgroundColor: "rgba(75, 122, 75, .08)", textColor: "#4B7A4B" },
  HIDDEN: { backgroundColor: "rgba(184, 67, 28, .10)", textColor: "#B8431C" },
};

export function reviewStatusPresentation(dictionary: Dictionary, status: ReviewStatus): ReviewStatusPresentation {
  return { ...REVIEW_STATUS_COLORS[status], label: dictionary.reviews.statusLabels[status] };
}
