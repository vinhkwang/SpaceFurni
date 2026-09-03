import Link from "next/link";
import { notFound } from "next/navigation";
import { apiFetch } from "@/lib/api/apiClient";
import { ApiError } from "@/lib/api/ApiError";
import type { ProductDetailResponse } from "@/lib/api/types";
import { ProductGallery } from "@/components/product/ProductGallery";
import { Container } from "@/components/ui/Container";

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

export default async function ProductDetailPage({ params }: PageProps<"/products/[productSlug]">) {
  const { productSlug } = await params;

  let product: ProductDetailResponse;
  try {
    product = await apiFetch<ProductDetailResponse>(`/products/${productSlug}`);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }

  return (
    <main className="pb-22">
      <Container className="pt-7.5">
        <nav
          aria-label="Breadcrumb"
          className="mb-6.5 flex items-center gap-2.5 text-[11px] uppercase tracking-[0.1em] text-ink-muted"
        >
          <Link href="/" className="transition-colors duration-200 hover:text-terracotta">
            Home
          </Link>
          {chevronIcon}
          <span>{product.categoryName}</span>
          {chevronIcon}
          <span className="text-ink">{product.name}</span>
        </nav>
      </Container>

      <Container>
        <ProductGallery images={product.imageUrls} productName={product.name} badge={product.badge} />
      </Container>
    </main>
  );
}
