import Image from "next/image";
import Link from "next/link";
import type { CategoryTreeResponse } from "@/lib/api/types";
import {
  buildProductListingHref,
  type ProductListingFilters,
} from "@/lib/catalog/productListingUrl";

type SubCategoryFilterProps = {
  departmentSlug: string;
  subCategories: CategoryTreeResponse[];
  activeSubCategorySlug: string | undefined;
  filters: ProductListingFilters;
};

const chipClassName =
  "flex flex-1 items-center gap-3.5 rounded-[14px] border bg-white px-4.5 py-3 transition duration-300";

const activeChipClassName = "border-deep shadow-sm";

const inactiveChipClassName = "border-hairline-soft hover:border-ink-muted";

function itemCountLabel(productCount: number): string {
  return productCount === 1 ? "1 item" : `${productCount} items`;
}

export function SubCategoryFilter({
  departmentSlug,
  subCategories,
  activeSubCategorySlug,
  filters,
}: SubCategoryFilterProps) {
  if (subCategories.length === 0) {
    return null;
  }

  const totalProductCount = subCategories.reduce(
    (runningTotal, subCategory) => runningTotal + subCategory.productCount,
    0,
  );

  return (
    <nav aria-label="Filter by subcategory" className="flex flex-wrap gap-3">
      <Link
        href={buildProductListingHref(departmentSlug, {
          ...filters,
          sub: undefined,
          page: undefined,
        })}
        aria-current={activeSubCategorySlug ? undefined : "page"}
        className={`${chipClassName} ${activeSubCategorySlug ? inactiveChipClassName : activeChipClassName}`}
      >
        <span className="flex h-[46px] w-[54px] flex-none items-center justify-center rounded-[9px] bg-surface-warm text-[15px] font-medium">
          {subCategories.length}
        </span>
        <span>
          <span className="block text-[12.5px] font-semibold tracking-[0.05em]">All</span>
          <span className="mt-[3px] block text-[10.5px] text-ink-muted">
            {itemCountLabel(totalProductCount)}
          </span>
        </span>
      </Link>

      {subCategories.map((subCategory) => {
        const isActive = subCategory.slug === activeSubCategorySlug;

        return (
          <Link
            key={subCategory.id}
            href={buildProductListingHref(departmentSlug, {
              ...filters,
              sub: subCategory.slug,
              page: undefined,
            })}
            aria-current={isActive ? "page" : undefined}
            className={`${chipClassName} ${isActive ? activeChipClassName : inactiveChipClassName}`}
          >
            <span className="relative flex h-[46px] w-[54px] flex-none items-center justify-center overflow-hidden rounded-[9px] bg-surface-warm p-[5px]">
              {subCategory.imageUrl ? (
                <Image
                  src={subCategory.imageUrl}
                  alt=""
                  fill
                  sizes="54px"
                  className="object-contain mix-blend-multiply"
                />
              ) : (
                <span className="text-[13px] font-medium text-ink-muted">
                  {subCategory.name.charAt(0)}
                </span>
              )}
            </span>
            <span>
              <span className="block text-[12.5px] font-semibold tracking-[0.05em]">
                {subCategory.name}
              </span>
              <span className="mt-[3px] block text-[10.5px] text-ink-muted">
                {itemCountLabel(subCategory.productCount)}
              </span>
            </span>
          </Link>
        );
      })}
    </nav>
  );
}
