import Image from "next/image";
import { formatMoney } from "@/lib/formatting/formatMoney";

type StorefrontPreviewCardProps = {
  title: string;
  subCategoryName: string;
  departmentName: string;
  priceAmount: number;
  imageUrl: string;
};

export function StorefrontPreviewCard({
  title,
  subCategoryName,
  departmentName,
  priceAmount,
  imageUrl,
}: StorefrontPreviewCardProps) {
  const previewTitle = title.trim() || "Product title";
  const previewSubtitle = [subCategoryName.trim() || "Sub-category", departmentName.trim()]
    .filter(Boolean)
    .join(" · ");

  return (
    <aside className="sticky top-5 rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div className="mb-5 text-[10.5px] uppercase tracking-[0.18em] text-ink-muted">Storefront preview</div>

      <div className="overflow-hidden rounded-2xl border border-hairline-soft">
        <div className="relative flex aspect-[4/3.2] items-center justify-center bg-surface-warm">
          <Image src={imageUrl} alt="" fill sizes="360px" className="object-contain p-6.5 mix-blend-multiply" />
        </div>
        <div className="flex flex-col gap-2.5 px-5 pb-5 pt-4.5">
          <span className="text-[9.5px] uppercase tracking-[0.16em] text-ink-muted">{previewSubtitle}</span>
          <div className="text-[14.5px] font-medium text-ink">{previewTitle}</div>
          <div className="text-[15px] font-semibold text-ink">{formatMoney(priceAmount)}</div>
          <button
            type="button"
            disabled
            className="mt-[5px] flex h-[42px] cursor-not-allowed items-center justify-center gap-2.5 rounded-pill border border-hairline text-[10.5px] font-semibold uppercase tracking-[0.14em] opacity-50"
          >
            Add to cart
          </button>
        </div>
      </div>

      <div className="mt-5.5 flex flex-col gap-3 border-t border-hairline-soft pt-5">
        <p className="text-[11.5px] leading-[1.55] text-ink-muted">
          Photos on a white background sit best in the grid — the storefront blends them into the card.
        </p>
        <p className="text-[11.5px] leading-[1.55] text-ink-muted">
          The title and sub-category are what customers search on. Keep them plain.
        </p>
      </div>
    </aside>
  );
}
