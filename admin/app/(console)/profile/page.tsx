import { apiFetch } from "@/lib/api/apiClient";
import type { CurrentUserResponse } from "@/lib/api/types";
import { getDictionary, type Dictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";

function roleLabel(dictionary: Dictionary, role: CurrentUserResponse["role"]): string {
  return role === "ADMIN" ? dictionary.common.administrator : dictionary.common.customer;
}

export default async function ProfilePage() {
  const [currentUser, dictionary] = await Promise.all([
    apiFetch<CurrentUserResponse>("/auth/me", { cache: "no-store" }),
    getLocale().then(getDictionary),
  ]);

  return (
    <div className="max-w-[560px] rounded-2xl border border-hairline-soft bg-white p-6.5">
      <h1 className="mb-6.5 text-[20px] font-semibold text-ink">{dictionary.profile.yourProfile}</h1>

      <div className="flex flex-col gap-5">
        <div>
          <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
            {dictionary.profile.fullName}
          </div>
          <div className="text-[15px] font-medium text-ink">{currentUser.fullName}</div>
        </div>
        <div>
          <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
            {dictionary.profile.emailAddress}
          </div>
          <div className="text-[15px] font-medium text-ink">{currentUser.email}</div>
        </div>
        <div>
          <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
            {dictionary.profile.accountType}
          </div>
          <div className="text-[15px] font-medium text-ink">{roleLabel(dictionary, currentUser.role)}</div>
        </div>
      </div>
    </div>
  );
}
