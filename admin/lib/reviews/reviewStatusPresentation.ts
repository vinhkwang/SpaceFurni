import type { ReviewStatus } from "@/lib/api/types";

export type ReviewStatusPresentation = {
  label: string;
  backgroundColor: string;
  textColor: string;
};

export const REVIEW_STATUS_PRESENTATION: Record<ReviewStatus, ReviewStatusPresentation> = {
  PUBLISHED: { label: "Published", backgroundColor: "rgba(75, 122, 75, .08)", textColor: "#4B7A4B" },
  HIDDEN: { label: "Hidden", backgroundColor: "rgba(184, 67, 28, .10)", textColor: "#B8431C" },
};
