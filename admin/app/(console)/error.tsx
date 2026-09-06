"use client";

export default function ConsoleError({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <div className="flex h-full min-h-[420px] flex-col items-center justify-center gap-4 rounded-2xl border border-hairline-soft bg-white px-6.5 text-center">
      <div className="text-[14px] font-semibold text-ink">Something went wrong loading this page</div>
      <p className="max-w-[360px] text-[12.5px] leading-relaxed text-ink-muted">
        The request to the server failed. Try again, or come back in a moment.
      </p>
      <button
        type="button"
        onClick={reset}
        className="flex h-11 items-center rounded-pill bg-deep px-6 text-[11px] font-semibold uppercase tracking-[0.13em] text-white transition-colors duration-200 hover:bg-terracotta"
      >
        Try again
      </button>
    </div>
  );
}
