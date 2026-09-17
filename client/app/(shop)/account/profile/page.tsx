import { redirect } from "next/navigation";
import { apiFetch } from "@/lib/api/apiClient";
import { getSessionToken } from "@/lib/auth/session";
import type { CurrentUserResponse } from "@/lib/api/types";
import { Container } from "@/components/ui/Container";

export const metadata = {
  title: "Your profile",
};

function roleLabel(role: CurrentUserResponse["role"]): string {
  return role === "ADMIN" ? "Administrator" : "Customer";
}

export default async function ProfilePage() {
  const sessionToken = await getSessionToken();
  if (!sessionToken) {
    redirect("/login");
  }

  const currentUser = await apiFetch<CurrentUserResponse>("/auth/me", { cache: "no-store" });

  return (
    <main className="py-8.5">
      <Container className="max-w-[560px]">
        <h1 className="mb-8.5 text-[38px] font-medium tracking-[-0.02em]">Your profile</h1>

        <div className="flex flex-col gap-5 rounded-2xl border border-hairline bg-white px-7 py-7">
          <div>
            <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
              Full name
            </div>
            <div className="text-[15px] font-medium">{currentUser.fullName}</div>
          </div>
          <div>
            <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
              Email address
            </div>
            <div className="text-[15px] font-medium">{currentUser.email}</div>
          </div>
          <div>
            <div className="mb-1.5 text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
              Account type
            </div>
            <div className="text-[15px] font-medium">{roleLabel(currentUser.role)}</div>
          </div>
        </div>
      </Container>
    </main>
  );
}
