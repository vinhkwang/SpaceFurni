"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { archiveProductAction } from "@/lib/products/productActions";
import { useDictionary } from "@/lib/i18n/LocaleProvider";
import { useToast } from "@/components/ui/Toast";

type ProductRowActionsProps = {
  productId: string;
};

const editButtonClassName =
  "flex h-7.5 items-center justify-center rounded-lg border border-hairline px-3 text-[11px] font-medium text-ink-muted transition-colors duration-200 hover:border-deep hover:text-ink";

const deleteButtonClassName =
  "flex h-7.5 items-center justify-center rounded-lg border border-hairline px-3 text-[11px] font-medium text-terracotta transition-colors duration-200 hover:border-terracotta disabled:cursor-not-allowed disabled:opacity-40";

export function ProductRowActions({ productId }: ProductRowActionsProps) {
  const router = useRouter();
  const { showToast } = useToast();
  const dictionary = useDictionary();
  const [isPending, setIsPending] = useState(false);

  async function deleteProduct() {
    setIsPending(true);
    const result = await archiveProductAction(productId);
    setIsPending(false);
    if (result.success) {
      showToast(dictionary.products.productDeleted);
      router.refresh();
    }
  }

  return (
    <div className="flex items-center justify-end gap-1.5">
      <Link href={`/products/${productId}/edit`} className={editButtonClassName}>
        {dictionary.common.edit}
      </Link>
      <button type="button" disabled={isPending} onClick={deleteProduct} className={deleteButtonClassName}>
        {dictionary.common.delete}
      </button>
    </div>
  );
}
