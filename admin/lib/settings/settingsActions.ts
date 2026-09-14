"use server";

import { revalidatePath } from "next/cache";
import { ApiError } from "@/lib/api/ApiError";
import { apiFetch } from "@/lib/api/apiClient";

export type PlatformSettingsFormValues = {
  freeDeliveryThresholdAmount: number;
  standardDeliveryFeeAmount: number;
  nextDayDeliveryFeeAmount: number;
  lowStockThresholdUnits: number;
};

export type UpdateSettingsResult = { success: true } | { success: false; errorMessage: string };

export async function updateSettingsAction(values: PlatformSettingsFormValues): Promise<UpdateSettingsResult> {
  try {
    await apiFetch<void>("/admin/settings", { method: "PATCH", body: values, cache: "no-store" });
    revalidatePath("/settings");
    return { success: true };
  } catch (error) {
    return {
      success: false,
      errorMessage: error instanceof ApiError ? error.message : "Something went wrong. Try again.",
    };
  }
}
