import Link from "next/link";
import { notFound } from "next/navigation";
import { apiFetch } from "@/lib/api/apiClient";
import type { CategoryTreeResponse, PageResponse, ProductSummaryResponse } from "@/lib/api/types";
import { ProductCard } from "@/components/product/ProductCard";
import { Container } from "@/components/ui/Container";

const PRODUCTS_PER_PAGE = 12;

const SORT_KEYS = ["newest", "priceAsc", "priceDesc", "rating"] as const;

type SortKey = (typeof SORT_KEYS)[number];

type ProductListingSearchParams = {
  sub?: string;
  sort?: string;
  minPrice?: string;
  maxPrice?: string;
  page?: string;
};

function firstSearchParamValue(rawValue: string | string[] | undefined): string | undefined {
  return Array.isArray(rawValue) ? rawValue[0] : rawValue;
}

function toSortKey(rawSort: string | undefined): SortKey {
  return SORT_KEYS.find((sortKey) => sortKey === rawSort) ?? "newest";
}

function toPageIndex(rawPage: string | undefined): number {
  const parsedPage = Number(rawPage);
  if (!Number.isInteger(parsedPage) || parsedPage < 1) {
    return 0;
  }
  return parsedPage - 1;
}

function toPriceBound(rawPrice: string | undefined): number | undefined {
  const parsedPrice = Number(rawPrice);
  if (!Number.isFinite(parsedPrice) || parsedPrice < 0) {
    return undefined;
  }
  return parsedPrice;
}

function findDepartmentBySlug(
  departments: CategoryTreeResponse[],
  departmentSlug: string,
): CategoryTreeResponse | undefined {
  return departments.find((department) => department.slug === departmentSlug);
}

function findSubCategoryBySlug(
  department: CategoryTreeResponse,
  subCategorySlug: string | undefined,
): CategoryTreeResponse | undefined {
  if (!subCategorySlug) {
    return undefined;
  }
  return department.subCategories.find((subCategory) => subCategory.slug === subCategorySlug);
}

function buildProductQuery(
  departmentSlug: string,
  subCategorySlug: string | undefined,
  sort: SortKey,
  minPrice: number | undefined,
  maxPrice: number | undefined,
  pageIndex: number,
): string {
  const query = new URLSearchParams({
    categorySlug: departmentSlug,
    sort,
    page: String(pageIndex),
    size: String(PRODUCTS_PER_PAGE),
  });
  if (subCategorySlug) {
    query.set("subCategorySlug", subCategorySlug);
  }
  if (minPrice !== undefined) {
    query.set("minPrice", String(minPrice));
  }
  if (maxPrice !== undefined) {
    query.set("maxPrice", String(maxPrice));
  }
  return query.toString();
}

function departmentBlurb(departmentName: string): string {
  return `Everything in ${departmentName.toLowerCase()} is photographed in the showroom, priced with VAT and delivered assembled inside Hanoi.`;
}

function resultCountLabel(totalElements: number, subCategoryName: string | undefined): string {
  const pieceLabel = totalElements === 1 ? "1 piece" : `${totalElements} pieces`;
  return subCategoryName ? `${pieceLabel} in ${subCategoryName.toLowerCase()}` : pieceLabel;
}

function hasNarrowingFilterApplied(
  subCategorySlug: string | undefined,
  minPrice: number | undefined,
  maxPrice: number | undefined,
  pageIndex: number,
): boolean {
  return Boolean(subCategorySlug) || minPrice !== undefined || maxPrice !== undefined || pageIndex > 0;
}

function emptyStateHeading(hasNarrowingFilter: boolean): string {
  return hasNarrowingFilter ? "Nothing in that price range" : "Nothing in this room yet";
}

function emptyStateBody(hasNarrowingFilter: boolean): string {
  return hasNarrowingFilter
    ? "Try widening the range or clearing the filter."
    : "We are still photographing this department. Browse another room in the meantime.";
}

function buildPageHref(
  departmentSlug: string,
  searchParams: ProductListingSearchParams,
  pageNumber: number,
): string {
  const query = new URLSearchParams();
  if (searchParams.sub) {
    query.set("sub", searchParams.sub);
  }
  if (searchParams.sort) {
    query.set("sort", searchParams.sort);
  }
  if (searchParams.minPrice) {
    query.set("minPrice", searchParams.minPrice);
  }
  if (searchParams.maxPrice) {
    query.set("maxPrice", searchParams.maxPrice);
  }
  if (pageNumber > 1) {
    query.set("page", String(pageNumber));
  }
  const queryString = query.toString();
  return queryString ? `/category/${departmentSlug}?${queryString}` : `/category/${departmentSlug}`;
}

function pageNumbers(totalPages: number): number[] {
  return Array.from({ length: totalPages }, (_, pageOffset) => pageOffset + 1);
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

const paginationArrowClassName =
  "flex h-10 w-10 items-center justify-center rounded-full border border-hairline-soft transition duration-250 hover:bg-surface";

export default async function DepartmentListingPage({
  params,
  searchParams,
}: PageProps<"/category/[departmentSlug]">) {
  const { departmentSlug } = await params;
  const resolvedSearchParams = await searchParams;

  const listingSearchParams: ProductListingSearchParams = {
    sub: firstSearchParamValue(resolvedSearchParams.sub),
    sort: firstSearchParamValue(resolvedSearchParams.sort),
    minPrice: firstSearchParamValue(resolvedSearchParams.minPrice),
    maxPrice: firstSearchParamValue(resolvedSearchParams.maxPrice),
    page: firstSearchParamValue(resolvedSearchParams.page),
  };

  const departments = await apiFetch<CategoryTreeResponse[]>("/categories");
  const department = findDepartmentBySlug(departments, departmentSlug);

  if (!department) {
    notFound();
  }

  const subCategory = findSubCategoryBySlug(department, listingSearchParams.sub);
  const sort = toSortKey(listingSearchParams.sort);
  const pageIndex = toPageIndex(listingSearchParams.page);
  const minPrice = toPriceBound(listingSearchParams.minPrice);
  const maxPrice = toPriceBound(listingSearchParams.maxPrice);

  const productPage = await apiFetch<PageResponse<ProductSummaryResponse>>(
    `/products?${buildProductQuery(departmentSlug, subCategory?.slug, sort, minPrice, maxPrice, pageIndex)}`,
  );

  const currentPageNumber = productPage.page + 1;
  const hasNarrowingFilter = hasNarrowingFilterApplied(subCategory?.slug, minPrice, maxPrice, pageIndex);

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
          <span className="text-ink">{department.name}</span>
          {subCategory ? (
            <>
              {chevronIcon}
              <span className="text-ink">{subCategory.name}</span>
            </>
          ) : null}
        </nav>

        <div className="flex flex-col gap-6 border-b border-hairline pb-7.5 md:flex-row md:items-end md:justify-between md:gap-15">
          <div>
            <h1 className="mb-3 text-[42px] font-medium leading-[1.06] tracking-[-0.02em]">
              {department.name}
            </h1>
            <p className="text-[12.5px] tracking-[0.04em] text-ink-muted">
              {resultCountLabel(productPage.totalElements, subCategory?.name)}
            </p>
          </div>
          <p className="max-w-100 text-[12.5px] leading-[1.7] text-ink-soft md:text-right">
            {departmentBlurb(department.name)}
          </p>
        </div>
      </Container>

      <Container className="mt-6.5">
        {productPage.content.length === 0 ? (
          <div className="py-20 text-center">
            <p className="mb-2.5 text-[17px] font-medium">{emptyStateHeading(hasNarrowingFilter)}</p>
            <p className="mb-5.5 text-[12.5px] text-ink-muted">
              {emptyStateBody(hasNarrowingFilter)}
            </p>
            {hasNarrowingFilter ? (
              <Link
                href={`/category/${departmentSlug}`}
                className="inline-flex h-11 items-center rounded-pill bg-deep px-6.5 text-[11px] font-semibold uppercase tracking-[0.13em] text-white"
              >
                Clear filters
              </Link>
            ) : null}
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {productPage.content.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </Container>

      {productPage.totalPages > 1 ? (
        <Container className="mt-11">
          <nav aria-label="Pagination" className="flex items-center justify-center gap-2">
            {currentPageNumber > 1 ? (
              <Link
                href={buildPageHref(departmentSlug, listingSearchParams, currentPageNumber - 1)}
                aria-label="Previous page"
                className={paginationArrowClassName}
              >
                <span className="rotate-180">{chevronIcon}</span>
              </Link>
            ) : (
              <span
                aria-hidden
                className="flex h-10 w-10 items-center justify-center rounded-full border border-hairline-soft text-ink-muted"
              >
                <span className="rotate-180">{chevronIcon}</span>
              </span>
            )}

            {pageNumbers(productPage.totalPages).map((pageNumber) =>
              pageNumber === currentPageNumber ? (
                <span
                  key={pageNumber}
                  aria-current="page"
                  className="flex h-10 w-10 items-center justify-center rounded-full bg-deep text-[12.5px] font-semibold text-white"
                >
                  {pageNumber}
                </span>
              ) : (
                <Link
                  key={pageNumber}
                  href={buildPageHref(departmentSlug, listingSearchParams, pageNumber)}
                  aria-label={`Page ${pageNumber}`}
                  className="flex h-10 w-10 items-center justify-center rounded-full border border-hairline-soft text-[12.5px] transition duration-250 hover:bg-surface"
                >
                  {pageNumber}
                </Link>
              ),
            )}

            {currentPageNumber < productPage.totalPages ? (
              <Link
                href={buildPageHref(departmentSlug, listingSearchParams, currentPageNumber + 1)}
                aria-label="Next page"
                className={paginationArrowClassName}
              >
                {chevronIcon}
              </Link>
            ) : (
              <span
                aria-hidden
                className="flex h-10 w-10 items-center justify-center rounded-full border border-hairline-soft text-ink-muted"
              >
                {chevronIcon}
              </span>
            )}
          </nav>
        </Container>
      ) : null}
    </main>
  );
}
