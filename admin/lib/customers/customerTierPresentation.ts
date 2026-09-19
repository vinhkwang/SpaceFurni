import type { CustomerTier } from "@/lib/api/types";
import type { Dictionary } from "@/lib/i18n/dictionaries/en";

export type CustomerTierPresentation = {
  label: string;
  backgroundColor: string;
  textColor: string;
};

const CUSTOMER_TIER_COLORS: Record<CustomerTier, { backgroundColor: string; textColor: string }> = {
  NEW: { backgroundColor: "rgba(26, 24, 21, .07)", textColor: "#55504A" },
  RETURNING: { backgroundColor: "rgba(201, 156, 100, .20)", textColor: "#8A6528" },
  VIP: { backgroundColor: "rgba(75, 122, 75, .12)", textColor: "#3F6B3F" },
};

export function customerTierPresentation(dictionary: Dictionary, tier: CustomerTier): CustomerTierPresentation {
  return { ...CUSTOMER_TIER_COLORS[tier], label: dictionary.customers.tierLabels[tier] };
}
