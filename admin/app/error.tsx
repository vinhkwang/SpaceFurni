"use client";

export default function RootError({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-canvas px-6.5 text-center">
      <div className="text-[16px] font-semibold text-ink">SpaceFurni Admin failed to load</div>
      <p className="max-w-[360px] text-[13px] leading-relaxed text-ink-muted">
        The server did not respond as expected. Try again, or come back in a moment.
      </p>
      <button
        type="button"
        onClick={reset}
        className="flex h-12 items-center rounded-pill bg-deep px-7 text-[11px] font-semibold uppercase tracking-[0.13em] text-white transition-colors duration-200 hover:bg-terracotta"
      >
        Try again
      </button>
    </div>
  );
}
