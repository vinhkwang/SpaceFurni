import Image from "next/image";
import Link from "next/link";
import type { ProductSummaryResponse } from "@/lib/api/types";
import { Badge } from "@/components/ui/Badge";
import { Price } from "@/components/ui/Price";
import { Rating } from "@/components/ui/Rating";

type ProductCardProps = {
  product: ProductSummaryResponse;
};

export function ProductCard({ product }: ProductCardProps) {
  const productHref = `/products/${product.slug}`;

  return (
    <article className="group relative overflow-hidden rounded-2xl border border-hairline-soft bg-white transition duration-300 hover:-translate-y-1.5 hover:border-hairline hover:shadow-2xl">
      <Link
        href={productHref}
        className="relative flex aspect-[4/3.2] items-center justify-center bg-surface-warm"
      >
        {product.primaryImageUrl ? (
          <Image
            src={product.primaryImageUrl}
            alt={product.name}
            fill
            sizes="(min-width: 1024px) 25vw, (min-width: 640px) 50vw, 100vw"
            className="object-contain p-6 mix-blend-multiply transition-transform duration-600 group-hover:scale-105"
          />
        ) : null}
        {product.badge ? (
          <span className="absolute left-3.5 top-3.5">
            <Badge variant={product.badge.variant}>{product.badge.label}</Badge>
          </span>
        ) : null}
        <span
          aria-hidden
          className="absolute right-3 top-3 flex h-8 w-8 items-center justify-center rounded-full border border-hairline-soft bg-white text-ink-muted opacity-60"
        >
          <svg
            viewBox="0 0 24 24"
            className="h-[11px] w-[11px] stroke-current"
            fill="none"
            strokeWidth={2}
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21.2l8.8-8.8a5.5 5.5 0 0 0 0-7.8z" />
          </svg>
        </span>
      </Link>

      <div className="flex flex-col gap-[9px] px-5 pb-5 pt-[18px]">
        <div className="flex items-center justify-between">
          <span className="text-[9.5px] uppercase tracking-[0.16em] text-ink-muted">
            {product.categoryName}
          </span>
          {product.ratingAverage === null ? null : (
            <Rating value={product.ratingAverage} variant="compact" />
          )}
        </div>

        <Link
          href={productHref}
          className="text-[14.5px] font-medium tracking-[0.005em] transition-colors duration-200 hover:text-terracotta"
        >
          {product.name}
        </Link>

        <Price amount={product.priceAmount} compareAtAmount={product.compareAtPriceAmount} />

        <button
          type="button"
          disabled
          className="mt-[5px] flex h-[42px] cursor-not-allowed items-center justify-center gap-2.5 rounded-pill border border-hairline text-[10.5px] font-semibold uppercase tracking-[0.14em] opacity-50"
        >
          Add to cart
        </button>
      </div>
    </article>
  );
}
