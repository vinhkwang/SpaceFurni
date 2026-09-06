"use client";

function PrintIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className="h-3 w-3 stroke-current"
      fill="none"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M6 9V3h12v6" />
      <path d="M6 18H4a1 1 0 0 1-1-1v-6a1 1 0 0 1 1-1h16a1 1 0 0 1 1 1v6a1 1 0 0 1-1 1h-2" />
      <path d="M6 14h12v7H6z" />
    </svg>
  );
}

export function PrintPackingSlipButton() {
  return (
    <button
      type="button"
      onClick={() => window.print()}
      className="flex h-10 shrink-0 cursor-pointer items-center gap-2.5 rounded-pill border border-hairline px-4.5 text-[10.5px] font-semibold uppercase tracking-[0.12em] transition-colors duration-200 hover:border-deep hover:bg-deep hover:text-white"
    >
      <PrintIcon />
      Packing slip
    </button>
  );
}
