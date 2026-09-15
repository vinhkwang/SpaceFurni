import type { ProductSummaryResponse } from "@/lib/api/types";
import { ProductCard } from "@/components/product/ProductCard";

type RecommendationRailProps = {
  products: ProductSummaryResponse[];
};

export function RecommendationRail({ products }: RecommendationRailProps) {
  if (products.length === 0) {
    return null;
  }

  return (
    <section aria-labelledby="recommendation-rail-heading">
      <h2 id="recommendation-rail-heading" className="mb-6.5 text-[29px] font-medium tracking-[-0.015em]">
        Customers also bought
      </h2>
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </section>
  );
}
