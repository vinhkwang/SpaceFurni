import Image from "next/image";
import Link from "next/link";
import type { ProductSummaryResponse } from "@/lib/api/types";
import { formatMoney } from "@/lib/formatting/formatMoney";

type RelatedProductsProps = {
  relatedProducts: ProductSummaryResponse[];
};

const arrowRightIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-2.5 w-2.5 flex-none stroke-current" fill="none" strokeWidth={2.5} strokeLinecap="round" strokeLinejoin="round">
    <path d="M5 12h14M13 6l6 6-6 6" />
  </svg>
);

export function RelatedProducts({ relatedProducts }: RelatedProductsProps) {
  if (relatedProducts.length === 0) {
    return null;
  }

  return (
    <section aria-labelledby="related-products-heading">
      <div className="mb-6.5 flex items-end justify-between">
        <h2 id="related-products-heading" className="text-[29px] font-medium tracking-[-0.015em]">
          Goes well with
        </h2>
        <Link
          href="/products"
          className="flex items-center gap-2.5 text-[11px] font-semibold uppercase tracking-[0.14em] text-ink-muted transition-colors duration-200 hover:text-terracotta"
        >
          View all
          {arrowRightIcon}
        </Link>
      </div>

      <div className="grid grid-cols-1 gap-4.5 md:grid-cols-3">
        {relatedProducts.map((product) => (
          <Link
            key={product.id}
            href={`/products/${product.slug}`}
            className="flex items-center gap-5 rounded-[15px] border border-hairline-soft bg-white py-4 pr-5 pl-4 transition duration-300 hover:-translate-y-1 hover:border-hairline hover:shadow-2xl"
          >
            <div className="relative h-24 w-[110px] flex-none rounded-[11px] bg-surface-warm">
              {product.primaryImageUrl ? (
                <Image
                  src={product.primaryImageUrl}
                  alt={product.name}
                  fill
                  sizes="110px"
                  className="object-contain p-3 mix-blend-multiply"
                />
              ) : null}
            </div>
            <div className="min-w-0 flex-1">
              <div className="mb-[7px] text-[9.5px] uppercase tracking-[0.16em] text-ink-muted">
                {product.categoryName}
              </div>
              <div className="mb-2 truncate text-[14px] font-medium">{product.name}</div>
              <div className="text-[14px] font-semibold">{formatMoney(product.priceAmount)}</div>
            </div>
            {arrowRightIcon}
          </Link>
        ))}
      </div>
    </section>
  );
}
