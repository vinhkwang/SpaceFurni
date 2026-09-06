import Image from "next/image";
import type { AdminOrderLineResponse } from "@/lib/api/types";
import { formatMoney } from "@/lib/formatting/formatMoney";

type OrderLinesProps = {
  lines: AdminOrderLineResponse[];
};

function ImagePlaceholderIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className="h-5 w-5 stroke-current text-ink-muted"
      fill="none"
      strokeWidth={1.5}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <rect x="3.5" y="4.5" width="17" height="15" rx="2" />
      <circle cx="8.5" cy="9.5" r="1.5" />
      <path d="m4.5 16.5 4.5-4.5 3 3 3.5-4 4.5 5.5" />
    </svg>
  );
}

export function OrderLines({ lines }: OrderLinesProps) {
  return (
    <div>
      {lines.map((line, lineIndex) => (
        <div key={lineIndex} className="flex items-center gap-5 border-t border-hairline-soft py-4">
          <div className="relative flex h-[74px] w-[88px] flex-none items-center justify-center rounded-xl bg-surface-raised p-2.5">
            {line.imageUrl ? (
              <Image
                src={line.imageUrl}
                alt=""
                fill
                sizes="88px"
                className="object-contain p-2.5 mix-blend-multiply"
              />
            ) : (
              <ImagePlaceholderIcon />
            )}
          </div>
          <div className="flex-1">
            <div className="text-[14px] font-medium text-ink">{line.productName}</div>
            <div className="mt-1.5 text-[11.5px] text-ink-muted">{formatMoney(line.unitPriceAmount)} each</div>
          </div>
          <span className="text-[12.5px] text-ink-soft">× {line.quantity}</span>
          <span className="w-[130px] text-right text-[14px] font-semibold text-ink">
            {formatMoney(line.lineTotalAmount)}
          </span>
        </div>
      ))}
    </div>
  );
}
