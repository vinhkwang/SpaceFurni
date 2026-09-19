import Link from "next/link";
import { notFound } from "next/navigation";
import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import type { ProductDetailResponse } from "@/lib/api/types";
import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { RecentlyViewedRail } from "@/components/discovery/RecentlyViewedRail";
import { RecommendationRail } from "@/components/discovery/RecommendationRail";
import { RecordRecentlyViewed } from "@/components/discovery/RecordRecentlyViewed";
import { fetchRecentlyViewedProducts, fetchRecommendedProducts } from "@/lib/discovery/discoveryApi";
import { ProductGallery } from "@/components/product/ProductGallery";
import { ProductInformationTabs } from "@/components/product/ProductInformationTabs";
import { ProductPurchasePanel } from "@/components/product/ProductPurchasePanel";
import { RelatedProducts } from "@/components/product/RelatedProducts";
import { RatingHistogram } from "@/components/reviews/RatingHistogram";
import { ReviewForm } from "@/components/reviews/ReviewForm";
import { ReviewList } from "@/components/reviews/ReviewList";
import { fetchRatingHistogram, fetchReviews, findEligibleOrderItemId } from "@/lib/reviews/reviewsApi";
import { Container } from "@/components/ui/Container";

function firstSearchParamValue(rawValue: string | string[] | undefined): string | undefined {
  return Array.isArray(rawValue) ? rawValue[0] : rawValue;
}

function toReviewsPageIndex(rawPage: string | undefined): number {
  const parsedPage = Number(rawPage);
  if (!Number.isInteger(parsedPage) || parsedPage < 1) {
    return 0;
  }
  return parsedPage - 1;
}

const chevronIcon = (
  <svg
    viewBox="0 0 24 24"
    aria-hidden
    className="h-[7px] w-[7px] stroke-current"
    fill="none"
    strokeWidth={3.5}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="m9 5 7 7-7 7" />
  </svg>
);

export default async function ProductDetailPage({
  params,
  searchParams,
}: PageProps<"/products/[productSlug]">) {
  const { productSlug } = await params;
  const resolvedSearchParams = await searchParams;
  const reviewsPageIndex = toReviewsPageIndex(firstSearchParamValue(resolvedSearchParams.reviewsPage));

  let product: ProductDetailResponse;
  try {
    product = await apiFetch<ProductDetailResponse>(`/products/${productSlug}`);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }

  const [recentlyViewedProducts, recommendedProducts, ratingHistogram, reviewPage, eligibleOrderItemId, locale] =
    await Promise.all([
      fetchRecentlyViewedProducts(product.id),
      fetchRecommendedProducts(product.id),
      fetchRatingHistogram(product.id),
      fetchReviews(product.id, reviewsPageIndex),
      findEligibleOrderItemId(product.id),
      getLocale(),
    ]);
  const dictionary = getDictionary(locale);

  return (
    <main className="pb-22">
      <RecordRecentlyViewed productId={product.id} />
      <Container alignToNavLabel className="pt-7.5">
        <nav
          aria-label={dictionary.catalog.breadcrumbAriaLabel}
          className="mb-6.5 flex items-center gap-2.5 text-[11px] uppercase tracking-[0.1em] text-ink-muted"
        >
          <Link href="/" className="transition-colors duration-200 hover:text-terracotta">
            {dictionary.common.home}
          </Link>
          {chevronIcon}
          <span>{product.categoryName}</span>
          {chevronIcon}
          <span className="text-ink">{product.name}</span>
        </nav>
      </Container>

      <Container alignToNavLabel className="grid grid-cols-1 items-start gap-14 lg:grid-cols-[1fr_470px]">
        <ProductGallery images={product.imageUrls} productName={product.name} badge={product.badge} />
        <ProductPurchasePanel product={product} />
      </Container>

      <Container alignToNavLabel className="mt-16">
        <ProductInformationTabs product={product} />
      </Container>

      <Container alignToNavLabel className="mt-20 max-w-[900px]">
        <h2 className="mb-6.5 text-[29px] font-medium tracking-[-0.015em]">{dictionary.product.reviewsHeading}</h2>
        <RatingHistogram
          ratingAverage={product.ratingAverage}
          reviewCount={product.reviewCount}
          histogram={ratingHistogram}
        />
        {eligibleOrderItemId === null ? null : (
          <div className="mt-7.5">
            <ReviewForm productId={product.id} productSlug={productSlug} orderItemId={eligibleOrderItemId} />
          </div>
        )}
        <div className="mt-9">
          <ReviewList reviewPage={reviewPage} productSlug={productSlug} />
        </div>
      </Container>

      <Container alignToNavLabel className="mt-20">
        <RelatedProducts relatedProducts={product.relatedProducts} />
      </Container>

      <Container alignToNavLabel className="mt-20">
        <RecommendationRail products={recommendedProducts} />
      </Container>

      <Container alignToNavLabel className="mt-20">
        <RecentlyViewedRail products={recentlyViewedProducts} />
      </Container>
    </main>
  );
}
