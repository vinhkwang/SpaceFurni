import { apiFetch } from "@/lib/api/apiClient";
import type { CategoryTreeResponse } from "@/lib/api/types";
import { ProductForm } from "@/components/products/ProductForm";

export default async function NewProductPage() {
  const departments = await apiFetch<CategoryTreeResponse[]>("/categories");

  return <ProductForm departments={departments} />;
}
