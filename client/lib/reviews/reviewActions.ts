"use server";

import { revalidatePath } from "next/cache";
import { ApiError } from "@/lib/api/ApiError";
import { apiFetch } from "@/lib/api/apiClient";
import type { ReviewResponse } from "@/lib/api/types";

export type SubmitReviewResult =
  | { success: true; review: ReviewResponse }
  | { success: false; errorMessage: string };

function submitReviewFailureMessage(error: unknown): string {
  return error instanceof ApiError ? error.message : "Something went wrong. Try again.";
}

export async function submitReviewAction(
  productId: string,
  productSlug: string,
  orderItemId: string,
  rating: number,
  comment: string,
): Promise<SubmitReviewResult> {
  try {
    const review = await apiFetch<ReviewResponse>(`/products/${productId}/reviews`, {
      method: "POST",
      body: { orderItemId, rating, comment: comment.trim() === "" ? null : comment.trim() },
      cache: "no-store",
    });
    revalidatePath(`/products/${productSlug}`);
    return { success: true, review };
  } catch (error) {
    return { success: false, errorMessage: submitReviewFailureMessage(error) };
  }
}
