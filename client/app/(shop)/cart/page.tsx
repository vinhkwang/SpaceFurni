import { apiFetch } from "@/lib/api/apiClient";
import type { CartResponse } from "@/lib/api/types";
import { CartLines } from "@/components/cart/CartLines";
import { Container } from "@/components/ui/Container";

export const metadata = {
  title: "Your cart",
};

export default async function CartPage() {
  const cart = await apiFetch<CartResponse>("/cart", { cache: "no-store" });

  return (
    <main className="py-8.5">
      <Container>
        <h1 className="mb-8.5 text-[38px] font-medium tracking-[-0.02em]">Your cart</h1>
        <CartLines cart={cart} />
      </Container>
    </main>
  );
}
