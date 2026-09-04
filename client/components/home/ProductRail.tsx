import Link from "next/link";
import { apiFetch } from "@/lib/api/apiClient";
import type { PageResponse, ProductSummaryResponse } from "@/lib/api/types";
import { ProductCard } from "@/components/product/ProductCard";

export type ProductRailSortKey = "newest" | "priceAsc" | "priceDesc" | "rating";

type ProductRailProps = {
  eyebrow: string;
  title: string;
  sort: ProductRailSortKey;
  shopAllHref: string;
};

const RAIL_PRODUCT_COUNT = 4;

async function fetchRailProducts(sort: ProductRailSortKey): Promise<ProductSummaryResponse[]> {
  const page = await apiFetch<PageResponse<ProductSummaryResponse>>(
    `/products?sort=${sort}&size=${RAIL_PRODUCT_COUNT}`,
  );
  return page.content;
}

export async function ProductRail({ eyebrow, title, sort, shopAllHref }: ProductRailProps) {
  const products = await fetchRailProducts(sort);

  if (products.length === 0) {
    return null;
  }

  const headingId = `product-rail-${sort}-heading`;

  return (
    <section aria-labelledby={headingId}>
      <div className="mb-7 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="mb-3 text-[10.5px] uppercase tracking-[0.22em] text-terracotta">{eyebrow}</p>
          <h2 id={headingId} className="text-[36px] font-medium leading-[1.1] tracking-[-0.015em]">
            {title}
          </h2>
        </div>
        <Link
          href={shopAllHref}
          className="flex h-[46px] w-fit items-center gap-3 rounded-pill border border-hairline px-6 text-[11px] font-semibold uppercase tracking-[0.14em] transition-all duration-300 hover:gap-[18px] hover:border-deep hover:bg-deep hover:text-white"
        >
          Shop all
          <svg
            viewBox="0 0 24 24"
            aria-hidden
            className="h-[9px] w-[9px] stroke-current"
            fill="none"
            strokeWidth={3}
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M4 12h15m-6-7 7 7-7 7" />
          </svg>
        </Link>
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </section>
  );
}
