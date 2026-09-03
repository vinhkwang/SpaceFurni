import Image from "next/image";
import Link from "next/link";
import { apiFetch } from "@/lib/api/apiClient";
import type { CategoryTreeResponse } from "@/lib/api/types";

function productCountLabel(productCount: number): string {
  return productCount === 1 ? "1 item" : `${productCount} items`;
}

function totalProductCount(departments: CategoryTreeResponse[]): number {
  return departments.reduce((runningTotal, department) => runningTotal + department.productCount, 0);
}

function departmentSummaryLine(departments: CategoryTreeResponse[]): string {
  return `${departments.length} rooms, ${totalProductCount(departments)} pieces in stock — every item photographed in our Thanh Xuan showroom.`;
}

export async function DepartmentGrid() {
  const departments = await apiFetch<CategoryTreeResponse[]>("/categories");

  return (
    <section aria-labelledby="department-grid-heading">
      <div className="mb-[30px] flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="mb-3 text-[10.5px] uppercase tracking-[0.22em] text-terracotta">
            Browse the store
          </p>
          <h2
            id="department-grid-heading"
            className="text-[36px] font-medium leading-[1.1] tracking-[-0.015em]"
          >
            Shop by department
          </h2>
        </div>
        <p className="max-w-[330px] text-[12.5px] leading-[1.65] text-ink-muted md:text-right">
          {departmentSummaryLine(departments)}
        </p>
      </div>

      <div className="grid grid-cols-2 gap-3.5 md:grid-cols-3 lg:grid-cols-5">
        {departments.map((department) => (
          <Link
            key={department.id}
            href={`/category/${department.slug}`}
            className="group relative block aspect-[3/4] overflow-hidden rounded-[14px] bg-surface transition duration-350 ease-out hover:-translate-y-[7px] hover:shadow-2xl"
          >
            {department.imageUrl ? (
              <Image
                src={department.imageUrl}
                alt=""
                fill
                sizes="(min-width: 1024px) 20vw, (min-width: 768px) 33vw, 50vw"
                className="object-cover transition-transform duration-1000 group-hover:scale-[1.07]"
              />
            ) : null}
            <span className="absolute inset-0 bg-linear-to-b from-transparent from-[38%] to-deep/85" />
            <span className="absolute inset-x-[18px] bottom-[18px]">
              <span className="mb-[5px] block text-[13.5px] font-semibold uppercase tracking-[0.09em] text-white">
                {department.name}
              </span>
              <span className="flex items-center justify-between">
                <span className="text-[10.5px] tracking-[0.1em] text-white/70">
                  {productCountLabel(department.productCount)}
                </span>
                <span
                  aria-hidden
                  className="flex h-[26px] w-[26px] items-center justify-center rounded-full border border-white/30 bg-white/20 text-white"
                >
                  <svg
                    viewBox="0 0 24 24"
                    className="h-[9px] w-[9px] stroke-current"
                    fill="none"
                    strokeWidth={3}
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M4 12h15m-6-7 7 7-7 7" />
                  </svg>
                </span>
              </span>
            </span>
          </Link>
        ))}
      </div>
    </section>
  );
}
