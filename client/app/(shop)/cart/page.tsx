import { apiFetch } from "@/lib/api/apiClient";
import type { CartResponse } from "@/lib/api/types";
import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { CartLines } from "@/components/cart/CartLines";
import { OrderSummaryPanel } from "@/components/cart/OrderSummaryPanel";
import { Container } from "@/components/ui/Container";

export const metadata = {
  title: "Your cart",
};

function toHighlightProductId(rawValue: string | string[] | undefined): string | undefined {
  return Array.isArray(rawValue) ? rawValue[0] : rawValue;
}

export default async function CartPage(props: PageProps<"/cart">) {
  const resolvedSearchParams = await props.searchParams;
  const highlightProductId = toHighlightProductId(resolvedSearchParams.highlightProductId);

  const [cart, dictionary] = await Promise.all([
    apiFetch<CartResponse>("/cart", { cache: "no-store" }),
    getLocale().then(getDictionary),
  ]);
  const hasLines = cart.lines.length > 0;

  return (
    <main className="py-8.5">
      <Container>
        <h1 className="mb-8.5 text-[38px] font-medium tracking-[-0.02em]">{dictionary.cart.yourCart}</h1>
        <div
          className={
            hasLines
              ? "grid grid-cols-1 items-start gap-6.5 lg:grid-cols-[1fr_400px]"
              : "grid grid-cols-1"
          }
        >
          <CartLines cart={cart} highlightProductId={highlightProductId} />
          {hasLines ? (
            <div className="lg:sticky lg:top-5">
              <OrderSummaryPanel cart={cart} checkoutHref="/checkout" />
            </div>
          ) : null}
        </div>
      </Container>
    </main>
  );
}
