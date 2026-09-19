import { redirect } from "next/navigation";
import { apiFetch } from "@/lib/api/apiClient";
import { getSessionToken } from "@/lib/auth/session";
import type { CurrentUserResponse } from "@/lib/api/types";
import { getDictionary, type Dictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { Container } from "@/components/ui/Container";

export const metadata = {
  title: "Your profile",
};

function roleLabel(dictionary: Dictionary, role: CurrentUserResponse["role"]): string {
  return role === "ADMIN" ? dictionary.account.administrator : dictionary.account.customer;
}

export default async function ProfilePage() {
  const sessionToken = await getSessionToken();
  if (!sessionToken) {
    redirect("/login");
  }

  const [currentUser, dictionary] = await Promise.all([
    apiFetch<CurrentUserResponse>("/auth/me", { cache: "no-store" }),
    getLocale().then(getDictionary),
  ]);

  return (
    <main className="py-8.5">
      <Container className="max-w-[560px]">
        <h1 className="mb-8.5 text-[38px] font-medium tracking-[-0.02em]">{dictionary.account.yourProfile}</h1>

        <div className="flex flex-col gap-5 rounded-2xl border border-hairline bg-white px-7 py-7">
          <div>
            <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
              {dictionary.account.fullName}
            </div>
            <div className="text-[15px] font-medium">{currentUser.fullName}</div>
          </div>
          <div>
            <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
              {dictionary.account.emailAddress}
            </div>
            <div className="text-[15px] font-medium">{currentUser.email}</div>
          </div>
          <div>
            <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
              {dictionary.account.accountType}
            </div>
            <div className="text-[15px] font-medium">{roleLabel(dictionary, currentUser.role)}</div>
          </div>
        </div>
      </Container>
    </main>
  );
}
