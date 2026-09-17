"use server";

import { revalidatePath } from "next/cache";
import { ApiError } from "@/lib/api/ApiError";
import { apiFetch } from "@/lib/api/apiClient";
import type { ReviewStatus } from "@/lib/api/types";

export type UpdateReviewStatusResult = { success: true } | { success: false; errorMessage: string };

export async function updateReviewStatusAction(reviewId: string, status: ReviewStatus): Promise<UpdateReviewStatusResult> {
  try {
    await apiFetch<void>(`/admin/reviews/${reviewId}/status?status=${status}`, {
      method: "PATCH",
      cache: "no-store",
    });
    revalidatePath("/reviews");
    return { success: true };
  } catch (error) {
    return {
      success: false,
      errorMessage: error instanceof ApiError ? error.message : "Something went wrong. Try again.",
    };
  }
}
