import { notFound } from "next/navigation";
import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import type { AdminProductDetailResponse, CategoryTreeResponse } from "@/lib/api/types";
import { ProductForm } from "@/components/products/ProductForm";

export default async function EditProductPage({ params }: PageProps<"/products/[productId]/edit">) {
  const { productId } = await params;

  let product: AdminProductDetailResponse;
  try {
    product = await apiFetch<AdminProductDetailResponse>(`/admin/products/${productId}`, { cache: "no-store" });
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }

  const departments = await apiFetch<CategoryTreeResponse[]>("/categories");

  return <ProductForm departments={departments} product={product} />;
}
