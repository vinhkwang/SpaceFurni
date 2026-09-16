import { apiFetch } from "@/lib/api/apiClient";
import { getSessionToken } from "@/lib/auth/session";
import type { OrderResponse, OrderSummaryResponse, PageResponse, RatingHistogramResponse, ReviewResponse } from "@/lib/api/types";

const REVIEWS_PAGE_SIZE = 10;
const ORDER_HISTORY_LOOKUP_SIZE = 50;

export async function fetchReviews(productId: string, page = 0): Promise<PageResponse<ReviewResponse>> {
  return apiFetch<PageResponse<ReviewResponse>>(
    `/products/${productId}/reviews?page=${page}&size=${REVIEWS_PAGE_SIZE}`,
    { cache: "no-store" },
  );
}

export async function fetchRatingHistogram(productId: string): Promise<RatingHistogramResponse> {
  return apiFetch<RatingHistogramResponse>(`/products/${productId}/reviews/histogram`, { cache: "no-store" });
}

export async function findEligibleOrderItemId(productId: string): Promise<string | null> {
  const sessionToken = await getSessionToken();
  if (!sessionToken) {
    return null;
  }

  const orderHistory = await apiFetch<PageResponse<OrderSummaryResponse>>(
    `/orders?size=${ORDER_HISTORY_LOOKUP_SIZE}`,
    { cache: "no-store" },
  );
  const deliveredOrders = orderHistory.content.filter((order) => order.status === "DELIVERED");

  for (const order of deliveredOrders) {
    const detail = await apiFetch<OrderResponse>(`/orders/${order.orderNumber}`, { cache: "no-store" });
    const matchingItem = detail.items.find((item) => item.productId === productId);
    if (matchingItem) {
      return matchingItem.id;
    }
  }

  return null;
}
