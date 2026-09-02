import Link from "next/link";

type CartIndicatorProps = {
  itemCount: number;
};

export function CartIndicator({ itemCount }: CartIndicatorProps) {
  return (
    <Link
      href="/cart"
      title="Shopping cart"
      className="flex h-[46px] items-center gap-[9px] rounded-pill bg-surface px-4 transition-colors duration-200 hover:bg-deep hover:text-white"
    >
      <svg
        viewBox="0 0 24 24"
        aria-hidden
        className="h-[15px] w-[15px] stroke-current"
        fill="none"
        strokeWidth={1.8}
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
        <path d="M3 6h18" />
        <path d="M16 10a4 4 0 0 1-8 0" />
      </svg>
      <span className="text-[11px] font-medium uppercase tracking-[0.1em]">Cart</span>
      <span className="flex h-5 min-w-5 items-center justify-center rounded-pill bg-terracotta px-1.5 text-[10.5px] font-semibold text-white">
        {itemCount}
      </span>
    </Link>
  );
}
