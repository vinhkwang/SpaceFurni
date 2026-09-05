"use client";

import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import type { AdminProductDetailResponse, CategoryTreeResponse, ProductStatus } from "@/lib/api/types";
import { createProductAction, updateProductAction, type ProductFormValues } from "@/lib/products/productActions";
import { StorefrontPreviewCard } from "@/components/products/StorefrontPreviewCard";

type ProductFormProps = {
  departments: CategoryTreeResponse[];
  product?: AdminProductDetailResponse;
};

const PICKABLE_IMAGE_URLS = [
  "/images/p-cloud-sofa.jpg",
  "/images/p-claire-sofa.jpg",
  "/images/p-axis-table.jpg",
  "/images/p-anita-shelf.jpg",
  "/images/p-tub-chair.png",
  "/images/p-trolley.png",
  "/images/p-bedside.png",
  "/images/p-desk.png",
];

const STATUS_OPTIONS: { value: ProductStatus; label: string }[] = [
  { value: "PUBLISHED", label: "Published" },
  { value: "DRAFT", label: "Draft" },
];

const fieldClassName =
  "h-12.5 rounded-xl border border-hairline bg-canvas px-4 text-[13.5px] text-ink outline-none transition-colors duration-200 placeholder:text-ink-muted focus:border-deep";

function firstSubCategorySlug(department: CategoryTreeResponse | undefined): string {
  return department?.subCategories[0]?.slug ?? "";
}

export function ProductForm({ departments, product }: ProductFormProps) {
  const router = useRouter();
  const isEditMode = product !== undefined;

  const initialDepartment =
    departments.find((department) => department.slug === product?.departmentSlug) ?? departments[0];

  const [title, setTitle] = useState(product?.title ?? "");
  const [departmentSlug, setDepartmentSlug] = useState(initialDepartment?.slug ?? "");
  const [subCategorySlug, setSubCategorySlug] = useState(
    product?.subCategorySlug ?? firstSubCategorySlug(initialDepartment),
  );
  const [price, setPrice] = useState(product ? String(product.price) : "");
  const [stock, setStock] = useState(product ? String(product.stock) : "");
  const [description, setDescription] = useState(product?.longDescription ?? "");
  const [imageUrl, setImageUrl] = useState(product?.imageUrl ?? PICKABLE_IMAGE_URLS[0]);
  const [status, setStatus] = useState<ProductStatus>(product?.status === "PUBLISHED" ? "PUBLISHED" : "DRAFT");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [isConflict, setIsConflict] = useState(false);

  const selectedDepartment = departments.find((department) => department.slug === departmentSlug);
  const subCategoryOptions = selectedDepartment?.subCategories ?? [];
  const selectedSubCategory = subCategoryOptions.find((subCategory) => subCategory.slug === subCategorySlug);

  function handleDepartmentChange(nextDepartmentSlug: string) {
    setDepartmentSlug(nextDepartmentSlug);
    const nextDepartment = departments.find((department) => department.slug === nextDepartmentSlug);
    setSubCategorySlug(firstSubCategorySlug(nextDepartment));
  }

  async function submitProduct(submitEvent: FormEvent<HTMLFormElement>) {
    submitEvent.preventDefault();
    setIsSubmitting(true);
    setErrorMessage("");
    setIsConflict(false);

    const values: ProductFormValues = {
      title,
      departmentSlug,
      subCategorySlug,
      price: Number(price),
      stock: Number(stock),
      description,
      imageUrl,
      status,
    };

    const result =
      isEditMode && product
        ? await updateProductAction(product.id, values, product.version)
        : await createProductAction(values);

    if (result.success) {
      router.push("/products");
      router.refresh();
      return;
    }

    setIsSubmitting(false);
    setErrorMessage(result.errorMessage);
    setIsConflict(result.isConflict);
  }

  return (
    <div className="grid grid-cols-1 items-start gap-4.5 lg:grid-cols-[1fr_396px]">
      <form onSubmit={submitProduct} className="rounded-2xl border border-hairline-soft bg-white p-7.5">
        <div className="mb-7 flex items-center justify-between">
          <div className="text-[16px] font-semibold text-ink">{isEditMode ? "Edit product" : "New product"}</div>
          <div className="text-[11.5px] text-ink-muted">Fields marked · are required</div>
        </div>

        {isConflict ? (
          <div className="mb-6 flex items-center justify-between gap-4 rounded-xl bg-terracotta/10 px-4.5 py-3.5 text-[12.5px] text-terracotta">
            <span>This product changed while you were editing.</span>
            <button
              type="button"
              onClick={() => router.refresh()}
              className="font-semibold underline underline-offset-2"
            >
              Reload
            </button>
          </div>
        ) : errorMessage ? (
          <p role="alert" className="mb-6 rounded-xl bg-terracotta/10 px-4.5 py-3.5 text-[12.5px] text-terracotta">
            {errorMessage}
          </p>
        ) : null}

        <div className="grid grid-cols-2 gap-4.5">
          <label className="col-span-2 flex flex-col gap-2">
            <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Product title ·</span>
            <input
              required
              value={title}
              onChange={(changeEvent) => setTitle(changeEvent.target.value)}
              placeholder="e.g. Halden Tub Chair"
              className={fieldClassName}
            />
          </label>

          <label className="flex flex-col gap-2">
            <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Department ·</span>
            <select
              required
              value={departmentSlug}
              onChange={(changeEvent) => handleDepartmentChange(changeEvent.target.value)}
              className={fieldClassName}
            >
              {departments.map((department) => (
                <option key={department.slug} value={department.slug}>
                  {department.name}
                </option>
              ))}
            </select>
          </label>

          <label className="flex flex-col gap-2">
            <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Sub-category ·</span>
            <select
              required
              value={subCategorySlug}
              onChange={(changeEvent) => setSubCategorySlug(changeEvent.target.value)}
              className={fieldClassName}
            >
              {subCategoryOptions.map((subCategory) => (
                <option key={subCategory.slug} value={subCategory.slug}>
                  {subCategory.name}
                </option>
              ))}
            </select>
          </label>

          <label className="flex flex-col gap-2">
            <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Price (₫) ·</span>
            <input
              required
              type="number"
              min={1}
              value={price}
              onChange={(changeEvent) => setPrice(changeEvent.target.value)}
              placeholder="7200000"
              className={fieldClassName}
            />
          </label>

          <label className="flex flex-col gap-2">
            <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Stock on hand ·</span>
            <input
              required
              type="number"
              min={0}
              value={stock}
              onChange={(changeEvent) => setStock(changeEvent.target.value)}
              placeholder="12"
              className={fieldClassName}
            />
          </label>

          <label className="col-span-2 flex flex-col gap-2">
            <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Description</span>
            <textarea
              value={description}
              onChange={(changeEvent) => setDescription(changeEvent.target.value)}
              placeholder="Two or three sentences on materials, comfort and scale…"
              rows={4}
              className={`${fieldClassName} h-auto resize-none py-3.5`}
            />
          </label>

          <div className="col-span-2 flex flex-col gap-3">
            <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Product image ·</span>
            <div className="grid grid-cols-8 gap-2.5">
              {PICKABLE_IMAGE_URLS.map((pickableImageUrl) => (
                <button
                  key={pickableImageUrl}
                  type="button"
                  onClick={() => setImageUrl(pickableImageUrl)}
                  aria-pressed={imageUrl === pickableImageUrl}
                  className={`relative aspect-square rounded-xl border bg-surface-raised p-2 transition-colors duration-200 ${
                    imageUrl === pickableImageUrl
                      ? "border-deep ring-2 ring-deep/15"
                      : "border-hairline-soft hover:border-hairline"
                  }`}
                >
                  <Image src={pickableImageUrl} alt="" fill sizes="80px" className="object-contain p-1.5 mix-blend-multiply" />
                </button>
              ))}
            </div>
          </div>

          <div className="col-span-2 flex items-center justify-between border-t border-hairline-soft pt-5.5">
            <div className="flex gap-2">
              {STATUS_OPTIONS.map((statusOption) => (
                <button
                  key={statusOption.value}
                  type="button"
                  onClick={() => setStatus(statusOption.value)}
                  aria-pressed={status === statusOption.value}
                  className={`flex h-10.5 items-center rounded-pill border px-5 text-[11.5px] transition-colors duration-200 ${
                    status === statusOption.value ? "border-deep bg-deep text-white" : "border-hairline text-ink"
                  }`}
                >
                  {statusOption.label}
                </button>
              ))}
            </div>

            <div className="flex gap-2.5">
              <Link
                href="/products"
                className="flex h-12 items-center rounded-pill border border-hairline px-5.5 text-[11px] font-semibold uppercase tracking-[0.13em] text-ink transition-colors duration-200 hover:bg-surface"
              >
                Cancel
              </Link>
              <button
                type="submit"
                disabled={isSubmitting}
                className="flex h-12 items-center rounded-pill bg-deep px-7 text-[11px] font-semibold uppercase tracking-[0.13em] text-white transition-colors duration-200 hover:bg-terracotta disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isEditMode ? "Save changes" : "Publish product"}
              </button>
            </div>
          </div>
        </div>
      </form>

      <StorefrontPreviewCard
        title={title}
        subCategoryName={selectedSubCategory?.name ?? ""}
        departmentName={selectedDepartment?.name ?? ""}
        priceAmount={Number(price) || 0}
        imageUrl={imageUrl}
      />
    </div>
  );
}
