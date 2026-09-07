import Image from "next/image";
import Link from "next/link";
import { buildProductListingHref } from "@/lib/catalog/productListingUrl";

function ArrowRightIcon({ className }: { className: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className={className}
      fill="none"
      strokeWidth={2.5}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M4 12h15m-6-7 7 7-7 7" />
    </svg>
  );
}

export function HeroPromoBanners() {
  return (
    <div className="flex flex-col gap-3.5">
      <Link
        href={buildProductListingHref("living-room", { sub: "sofa" })}
        className="group relative h-[300px] overflow-hidden rounded-2xl bg-surface"
      >
        <Image
          src="/images/hero-sofa.jpg"
          alt=""
          fill
          sizes="486px"
          className="object-cover transition-transform duration-[1200ms] group-hover:scale-105"
        />
        <div className="absolute inset-0 bg-linear-to-b from-white/86 from-0% via-white/20 via-[58%] to-transparent" />
        <div className="absolute inset-x-7 top-[26px]">
          <p className="mb-2.5 text-[10.5px] uppercase tracking-[0.22em] text-terracotta">
            New arrival
          </p>
          <p className="text-[27px] font-medium leading-[1.16] tracking-[-0.01em] text-ink">
            Modern sofas
            <br />
            that make the room
          </p>
        </div>
        <div className="absolute bottom-6 left-7 flex items-center gap-2.5 text-[11px] font-semibold uppercase tracking-[0.12em] text-ink">
          Shop sofas
          <ArrowRightIcon className="h-[9px] w-[9px] stroke-current" />
        </div>
      </Link>

      <div className="grid flex-1 grid-cols-2 gap-3.5">
        <Link
          href="/category/work-study"
          className="group relative min-h-[196px] overflow-hidden rounded-2xl bg-surface"
        >
          <Image
            src="/images/promo-office.jpg"
            alt=""
            fill
            sizes="236px"
            className="object-cover transition-transform duration-[1200ms] group-hover:scale-[1.06]"
          />
          <div className="absolute inset-0 bg-linear-to-b from-deep/62 from-0% to-deep/10 to-[70%]" />
          <div className="absolute inset-x-5 top-5">
            <span className="mb-2.5 inline-block rounded-pill bg-terracotta px-2.5 py-[5px] text-[10px] font-semibold tracking-[0.12em] text-white">
              −20%
            </span>
            <p className="text-[15.5px] font-medium leading-[1.3] text-white">
              Design your
              <br />
              home office
            </p>
          </div>
        </Link>

        <Link
          href="/category/bedroom"
          className="group relative min-h-[196px] overflow-hidden rounded-2xl bg-surface"
        >
          <Image
            src="/images/promo-bedroom.jpg"
            alt=""
            fill
            sizes="236px"
            className="object-cover transition-transform duration-[1200ms] group-hover:scale-[1.06]"
          />
          <div className="absolute inset-0 bg-linear-to-b from-deep/62 from-0% to-deep/10 to-[70%]" />
          <div className="absolute inset-x-5 top-5">
            <span className="mb-2.5 inline-block rounded-pill bg-brass px-2.5 py-[5px] text-[10px] font-semibold tracking-[0.12em] text-ink">
              −50%
            </span>
            <p className="text-[15.5px] font-medium leading-[1.3] text-white">
              A comfier
              <br />
              bedroom
            </p>
          </div>
        </Link>
      </div>
    </div>
  );
}
