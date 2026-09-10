import Link from "next/link";
import { apiFetch } from "@/lib/api/apiClient";
import { getSessionToken } from "@/lib/auth/session";
import type { CartResponse, CategoryTreeResponse, CurrentUserResponse } from "@/lib/api/types";
import { CartIndicator } from "@/components/layout/CartIndicator";
import { MegaNavigation } from "@/components/layout/MegaNavigation";
import { SearchBar } from "@/components/layout/SearchBar";
import { Container } from "@/components/ui/Container";
import { LogoLockup } from "@/components/ui/LogoLockup";

const headerPillClassName =
  "flex h-[46px] items-center gap-[9px] rounded-pill bg-surface transition-colors duration-200 hover:bg-deep hover:text-white";

function totalCartItemCount(cart: CartResponse): number {
  return cart.lines.reduce((runningTotal, line) => runningTotal + line.quantity, 0);
}

function userInitials(fullName: string): string {
  const initials = fullName
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .map((namePart) => namePart.charAt(0).toUpperCase());
  if (initials.length === 0) {
    return "?";
  }
  return initials.length === 1 ? initials[0] : `${initials[0]}${initials[initials.length - 1]}`;
}

async function fetchCurrentUser(): Promise<CurrentUserResponse | null> {
  const sessionToken = await getSessionToken();
  if (!sessionToken) {
    return null;
  }
  try {
    return await apiFetch<CurrentUserResponse>("/auth/me");
  } catch {
    return null;
  }
}

export async function ShopHeader() {
  const [categories, cart, currentUser] = await Promise.all([
    apiFetch<CategoryTreeResponse[]>("/categories"),
    apiFetch<CartResponse>("/cart"),
    fetchCurrentUser(),
  ]);

  return (
    <header className="relative z-40 bg-canvas">
      <Container className="flex h-28 items-center justify-between gap-10">
        <Link href="/">
          <LogoLockup />
        </Link>

        <SearchBar />

        <div className="flex items-center gap-2.5">
          <Link href="/wishlist" title="Saved items" className={`${headerPillClassName} px-4`}>
            <svg
              viewBox="0 0 24 24"
              aria-hidden
              className="h-3.5 w-3.5 stroke-current"
              fill="none"
              strokeWidth={1.8}
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21.2l8.8-8.8a5.5 5.5 0 0 0 0-7.8z" />
            </svg>
            <span className="text-[11px] font-medium uppercase tracking-[0.1em]">Saved</span>
          </Link>

          <CartIndicator itemCount={totalCartItemCount(cart)} />

          {currentUser ? (
            <Link href="/" title={currentUser.fullName} className={`${headerPillClassName} px-2`}>
              <span className="flex h-[30px] w-[30px] items-center justify-center rounded-full bg-white text-[11px] font-semibold text-deep">
                {userInitials(currentUser.fullName)}
              </span>
            </Link>
          ) : (
            <Link href="/login" className={`${headerPillClassName} pl-2 pr-5`}>
              <span className="flex h-[30px] w-[30px] items-center justify-center rounded-full bg-white text-deep">
                <svg
                  viewBox="0 0 24 24"
                  aria-hidden
                  className="h-3 w-3 stroke-current"
                  fill="none"
                  strokeWidth={1.8}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <path d="M12 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z" />
                </svg>
              </span>
              <span className="text-[11px] font-medium uppercase tracking-[0.1em]">Sign in</span>
            </Link>
          )}
        </div>
      </Container>

      <Container className="pb-1">
        <MegaNavigation categories={categories} />
      </Container>
    </header>
  );
}
