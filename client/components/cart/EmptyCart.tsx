import Link from "next/link";

const bagIcon = (
  <svg
    viewBox="0 0 24 24"
    aria-hidden
    className="h-6 w-6 stroke-current text-ink-muted"
    fill="none"
    strokeWidth={1.8}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M6 8h12l-1 12H7L6 8Z" />
    <path d="M9 8V6a3 3 0 0 1 6 0v2" />
  </svg>
);

export function EmptyCart() {
  return (
    <div className="flex flex-col items-center rounded-[18px] border border-hairline bg-white px-6 py-22.5 text-center">
      <div className="mb-6 flex h-[74px] w-[74px] items-center justify-center rounded-full bg-surface">
        {bagIcon}
      </div>
      <p className="mb-2.5 text-[22px] font-medium">Your cart is empty</p>
      <p className="mb-7 text-[13px] text-ink-muted">
        Everything you add will wait here for 30 days.
      </p>
      <Link
        href="/"
        className="inline-flex h-[50px] items-center rounded-pill bg-deep px-7.5 text-[11.5px] font-semibold uppercase tracking-[0.14em] text-white transition-colors duration-200 hover:bg-terracotta"
      >
        Start shopping
      </Link>
    </div>
  );
}
