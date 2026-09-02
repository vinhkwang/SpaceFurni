import Image from "next/image";
import Link from "next/link";
import { apiFetch } from "@/lib/api/apiClient";
import type { CartResponse, CategoryTreeResponse } from "@/lib/api/types";
import { CartIndicator } from "@/components/layout/CartIndicator";
import { MegaNavigation } from "@/components/layout/MegaNavigation";
import { SearchBar } from "@/components/layout/SearchBar";
import { Container } from "@/components/ui/Container";

const headerPillClassName =
  "flex h-[46px] items-center gap-[9px] rounded-pill bg-surface transition-colors duration-200 hover:bg-deep hover:text-white";

function totalCartItemCount(cart: CartResponse): number {
  return cart.lines.reduce((runningTotal, line) => runningTotal + line.quantity, 0);
}

export async function ShopHeader() {
  const [categories, cart] = await Promise.all([
    apiFetch<CategoryTreeResponse[]>("/categories"),
    apiFetch<CartResponse>("/cart"),
  ]);

  return (
    <header className="relative z-40 bg-canvas">
      <Container className="flex h-28 items-center justify-between gap-10">
        <Link href="/" className="flex items-center gap-3.5">
          <span className="flex h-[34px] w-[88px] items-start overflow-hidden">
            <Image
              src="/images/logo.png"
              alt="SpaceFurni"
              width={88}
              height={55}
              priority
              className="w-[88px]"
            />
          </span>
          <span className="flex flex-col gap-[3px] border-l border-hairline pl-3.5">
            <span className="flex gap-[0.34em] text-[19px] leading-none tracking-[0.14em]">
              <span className="font-bold">SPACE</span>
              <span className="font-light text-ink-soft">FURNI</span>
            </span>
            <span className="text-[9.5px] uppercase tracking-[0.14em] text-ink-muted">
              Furniture for real homes
            </span>
          </span>
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
        </div>
      </Container>

      <Container className="pb-1">
        <MegaNavigation categories={categories} />
      </Container>
    </header>
  );
}
