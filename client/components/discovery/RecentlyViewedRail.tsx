import type { ProductSummaryResponse } from "@/lib/api/types";
import { ProductCard } from "@/components/product/ProductCard";

type RecentlyViewedRailProps = {
  products: ProductSummaryResponse[];
};

export function RecentlyViewedRail({ products }: RecentlyViewedRailProps) {
  if (products.length === 0) {
    return null;
  }

  return (
    <section aria-labelledby="recently-viewed-heading">
      <h2 id="recently-viewed-heading" className="mb-6.5 text-[29px] font-medium tracking-[-0.015em]">
        Recently viewed
      </h2>
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </section>
  );
}
