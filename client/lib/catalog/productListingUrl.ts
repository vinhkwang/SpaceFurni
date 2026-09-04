export type ProductListingFilters = {
  sub?: string;
  sort?: string;
  minPrice?: string;
  maxPrice?: string;
  page?: string;
};

const FILTER_PARAM_ORDER = ["sub", "sort", "minPrice", "maxPrice", "page"] as const;

function isMeaningfulFilterValue(paramName: string, value: string | undefined): value is string {
  if (value === undefined || value.trim() === "") {
    return false;
  }
  return !(paramName === "page" && value === "1");
}

export function buildProductListingHref(
  departmentSlug: string,
  filters: ProductListingFilters,
): string {
  const query = new URLSearchParams();

  for (const paramName of FILTER_PARAM_ORDER) {
    const value = filters[paramName];
    if (isMeaningfulFilterValue(paramName, value)) {
      query.set(paramName, value.trim());
    }
  }

  const queryString = query.toString();
  return queryString ? `/category/${departmentSlug}?${queryString}` : `/category/${departmentSlug}`;
}

export function hasActivePriceFilter(filters: ProductListingFilters): boolean {
  return (
    isMeaningfulFilterValue("minPrice", filters.minPrice) ||
    isMeaningfulFilterValue("maxPrice", filters.maxPrice)
  );
}
