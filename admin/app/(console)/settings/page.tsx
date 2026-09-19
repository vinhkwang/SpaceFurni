import { apiFetch } from "@/lib/api/apiClient";
import type { PlatformSettingsResponse } from "@/lib/api/types";
import { SettingsForm } from "@/components/settings/SettingsForm";

export default async function SettingsPage() {
  const settings = await apiFetch<PlatformSettingsResponse>("/admin/settings", { cache: "no-store" });

  return <SettingsForm settings={settings} />;
}

