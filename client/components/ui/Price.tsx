import { formatMoney } from "@/lib/formatting/formatMoney";

type PriceSize = "medium" | "large";

type PriceProps = {
  amount: number;
  compareAtAmount?: number | null;
  size?: PriceSize;
};

const currentAmountClassNames: Record<PriceSize, string> = {
  medium: "text-[15px] font-semibold",
  large: "text-[29px] font-semibold tracking-[-0.02em]",
};

const compareAtAmountClassNames: Record<PriceSize, string> = {
  medium: "text-[11.5px]",
  large: "text-[14px]",
};

const gapClassNames: Record<PriceSize, string> = {
  medium: "gap-[9px]",
  large: "gap-3",
};

export function Price({ amount, compareAtAmount, size = "medium" }: PriceProps) {
  return (
    <span className={`inline-flex items-baseline ${gapClassNames[size]}`}>
      <span className={currentAmountClassNames[size]}>{formatMoney(amount)}</span>
      {typeof compareAtAmount === "number" ? (
        <s className={`${compareAtAmountClassNames[size]} text-ink-muted`}>
          {formatMoney(compareAtAmount)}
        </s>
      ) : null}
    </span>
  );
}
