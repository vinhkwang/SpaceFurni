"use client";

import Link from "next/link";
import { useState } from "react";
import type { CategoryTreeResponse } from "@/lib/api/types";

type MegaNavigationProps = {
  categories: CategoryTreeResponse[];
};

const navigationItemClassName =
  "mx-[3px] flex h-[38px] items-center gap-[7px] rounded-pill px-[17px] text-[11.5px] font-medium uppercase tracking-[0.13em] text-white/70 transition-colors duration-200 hover:bg-white/10 hover:text-white";

export function MegaNavigation({ categories }: MegaNavigationProps) {
  const [openCategoryId, setOpenCategoryId] = useState<string | null>(null);

  return (
    <nav className="relative flex h-14 items-center rounded-pill bg-deep pl-1 pr-2.5">
      <ul className="flex h-14 list-none items-center">
        <li className="flex h-14 items-center">
          <Link href="/" className={navigationItemClassName}>
            Homepage
          </Link>
        </li>
        {categories.map((category) => (
          <li
            key={category.id}
            className="relative flex h-14 items-center"
            onMouseEnter={() => setOpenCategoryId(category.id)}
            onMouseLeave={() => setOpenCategoryId(null)}
          >
            <Link href={`/category/${category.slug}`} className={navigationItemClassName}>
              {category.name}
              {category.subCategories.length > 0 ? (
                <svg
                  viewBox="0 0 24 24"
                  aria-hidden
                  className="h-[7px] w-[7px] stroke-current opacity-60"
                  fill="none"
                  strokeWidth={3}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="m6 9 6 6 6-6" />
                </svg>
              ) : null}
            </Link>
            {category.subCategories.length > 0 && openCategoryId === category.id ? (
              <div className="absolute left-2 top-[52px] z-70 min-w-[230px] rounded-2xl border border-hairline-soft bg-white p-2.5 shadow-2xl">
                {category.subCategories.map((subCategory) => (
                  <Link
                    key={subCategory.id}
                    href={`/category/${subCategory.slug}`}
                    className="flex items-center justify-between rounded-[9px] px-3 py-2.5 text-[12.5px] text-ink transition-colors duration-200 hover:bg-surface"
                  >
                    <span>{subCategory.name}</span>
                    <span className="text-[10.5px] text-ink-muted">
                      {subCategory.productCount}
                    </span>
                  </Link>
                ))}
              </div>
            ) : null}
          </li>
        ))}
      </ul>
    </nav>
  );
}
