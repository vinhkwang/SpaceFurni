"use server";

import { revalidatePath } from "next/cache";
import { ApiError } from "@/lib/api/ApiError";
import { apiFetch } from "@/lib/api/apiClient";
import type { ProductStatus } from "@/lib/api/types";

export type ProductFormValues = {
  title: string;
  departmentSlug: string;
  subCategorySlug: string;
  price: number;
  stock: number;
  description: string;
  imageUrl: string;
  status: ProductStatus;
};

export type ProductActionResult =
  | { success: true; productId: string }
  | { success: false; isConflict: boolean; errorMessage: string };

function toAdminProductRequestBody(values: ProductFormValues, version: number | null) {
  return {
    title: values.title,
    departmentSlug: values.departmentSlug,
    subCategorySlug: values.subCategorySlug,
    price: values.price,
    stock: values.stock,
    shortDescription: values.description,
    longDescription: values.description,
    imageUrl: values.imageUrl,
    status: values.status,
    version,
  };
}

function productActionFailureMessage(error: unknown): string {
  return error instanceof ApiError ? error.message : "Something went wrong. Try again.";
}

export async function createProductAction(values: ProductFormValues): Promise<ProductActionResult> {
  try {
    const productId = await apiFetch<string>("/admin/products", {
      method: "POST",
      body: toAdminProductRequestBody(values, null),
      cache: "no-store",
    });
    revalidatePath("/products");
    return { success: true, productId };
  } catch (error) {
    return { success: false, isConflict: false, errorMessage: productActionFailureMessage(error) };
  }
}

export async function updateProductAction(
  productId: string,
  values: ProductFormValues,
  version: number,
): Promise<ProductActionResult> {
  try {
    await apiFetch<void>(`/admin/products/${productId}`, {
      method: "PUT",
      body: toAdminProductRequestBody(values, version),
      cache: "no-store",
    });
    revalidatePath("/products");
    return { success: true, productId };
  } catch (error) {
    return {
      success: false,
      isConflict: error instanceof ApiError && error.status === 409,
      errorMessage: productActionFailureMessage(error),
    };
  }
}
