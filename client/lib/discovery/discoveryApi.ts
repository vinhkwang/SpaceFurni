import { apiFetch } from "@/lib/api/apiClient";
import type { ProductRecommendationResponse, ProductSummaryResponse } from "@/lib/api/types";

export async function fetchRecentlyViewedProducts(excludeProductId: string): Promise<ProductSummaryResponse[]> {
  return apiFetch<ProductSummaryResponse[]>(
    `/recently-viewed?excludeProductId=${excludeProductId}`,
    { cache: "no-store" },
  );
}

export async function fetchRecommendedProducts(productId: string, limit = 8): Promise<ProductSummaryResponse[]> {
  const response = await apiFetch<ProductRecommendationResponse>(
    `/products/${productId}/recommendations?limit=${limit}`,
  );
  return response.products;
}
