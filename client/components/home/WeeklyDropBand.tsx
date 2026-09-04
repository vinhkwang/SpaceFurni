import Image from "next/image";
import Link from "next/link";

export function WeeklyDropBand() {
  return (
    <section
      aria-labelledby="weekly-drop-heading"
      className="relative min-h-[352px] overflow-hidden rounded-[18px] bg-surface"
    >
      <Image
        src="/images/banner-wide.jpg"
        alt=""
        fill
        sizes="100vw"
        className="object-cover object-right"
      />
      <span className="absolute inset-0 bg-linear-to-r from-surface from-0% via-surface/90 via-[44%] to-transparent to-[68%]" />
      <div className="relative flex min-h-[352px] max-w-[440px] flex-col items-start justify-center gap-4 px-8 py-14 sm:px-16">
        <p className="text-[10.5px] uppercase tracking-[0.22em] text-terracotta">Every Thursday</p>
        <h2
          id="weekly-drop-heading"
          className="text-pretty text-[40px] font-medium leading-[1.08] tracking-[-0.02em]"
        >
          A new collection every week
        </h2>
        <p className="text-[13.5px] leading-[1.7] text-ink-soft">
          Small drops, made in limited runs. We design one capsule a week so your home never looks
          like a catalogue.
        </p>
        <Link
          href="/products"
          className="mt-1.5 flex h-12 items-center gap-3 rounded-pill bg-deep px-7 text-[11.5px] font-semibold uppercase tracking-[0.14em] text-white transition-all duration-300 hover:gap-[18px] hover:bg-terracotta"
        >
          Discover this week
          <svg
            viewBox="0 0 24 24"
            aria-hidden
            className="h-2.5 w-2.5 stroke-current"
            fill="none"
            strokeWidth={3}
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M4 12h15m-6-7 7 7-7 7" />
          </svg>
        </Link>
      </div>
    </section>
  );
}
