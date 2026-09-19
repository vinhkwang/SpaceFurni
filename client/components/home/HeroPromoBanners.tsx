import Image from "next/image";
import Link from "next/link";
import { buildProductListingHref } from "@/lib/catalog/productListingUrl";
import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";

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

export async function HeroPromoBanners() {
  const dictionary = getDictionary(await getLocale());

  return (
    <div className="flex flex-col gap-3.5">
      <Link
        href={buildProductListingHref("living-room", { sub: "sofa" })}
        className="group relative h-[300px] overflow-hidden rounded-2xl bg-surface ring-1 ring-inset ring-hairline-soft"
      >
        <Image
          src="/images/hero-sofa.jpg"
          alt=""
          fill
          sizes="486px"
          className="object-cover transition-transform duration-[1200ms] group-hover:scale-105"
        />
        <div className="absolute inset-0 bg-linear-to-b from-canvas/95 via-canvas/70 via-[40%] to-transparent" />
        <div className="absolute inset-x-7 top-6.5">
          <p className="mb-2.75 flex items-center gap-2.5 text-[10.5px] uppercase tracking-[0.24em] text-terracotta">
            <span aria-hidden className="h-px w-5.5 bg-terracotta/55" />
            {dictionary.home.promoNewArrival}
          </p>
          <p className="text-[28px] font-medium leading-[1.14] tracking-[-0.015em] text-ink">
            {dictionary.home.promoSofaTitleLine1}
            <br />
            {dictionary.home.promoSofaTitleLine2}
          </p>
        </div>
        <div className="absolute bottom-5.5 left-7 flex h-10.5 items-center gap-2.75 rounded-pill bg-canvas px-5 text-[10.5px] font-semibold uppercase tracking-[0.14em] text-ink shadow-[0_12px_26px_-16px_var(--tw-shadow-color)] shadow-ink/55 transition-colors duration-300 group-hover:bg-deep group-hover:text-white">
          {dictionary.home.promoShopSofas}
          <ArrowRightIcon className="h-[9px] w-[9px] stroke-current" />
        </div>
      </Link>

      <div className="grid flex-1 grid-cols-2 gap-3.5">
        <Link
          href="/category/work-study"
          className="group relative min-h-[196px] overflow-hidden rounded-2xl bg-surface ring-1 ring-inset ring-hairline-soft"
        >
          <Image
            src="/images/promo-office.jpg"
            alt=""
            fill
            sizes="236px"
            className="object-cover transition-transform duration-[1200ms] group-hover:scale-[1.06]"
          />
          <div className="absolute inset-0 bg-linear-to-t from-deep/86 via-deep/44 via-[38%] to-transparent to-[74%]" />
          <span className="absolute left-4.5 top-4.5 flex h-6.5 items-center rounded-pill bg-terracotta px-2.75 text-[10px] font-semibold tracking-[0.12em] text-white">
            −20%
          </span>
          <div className="absolute inset-x-5 bottom-4.5 flex items-end justify-between gap-3">
            <div>
              <p className="mb-1.75 text-[9.5px] uppercase tracking-[0.2em] text-white/72">
                {dictionary.home.promoOfficeEyebrow}
              </p>
              <p className="text-[16.5px] font-medium leading-[1.26] text-white">
                {dictionary.home.promoOfficeTitleLine1}
                <br />
                {dictionary.home.promoOfficeTitleLine2}
              </p>
            </div>
            <span className="flex h-7.5 w-7.5 flex-none items-center justify-center rounded-full border border-white/36 bg-white/18 text-white transition-colors duration-300 group-hover:bg-canvas group-hover:text-ink">
              <ArrowRightIcon className="h-[9px] w-[9px] stroke-current" />
            </span>
          </div>
        </Link>

        <Link
          href="/category/bedroom"
          className="group relative min-h-[196px] overflow-hidden rounded-2xl bg-surface ring-1 ring-inset ring-hairline-soft"
        >
          <Image
            src="/images/promo-bedroom.jpg"
            alt=""
            fill
            sizes="236px"
            className="object-cover transition-transform duration-[1200ms] group-hover:scale-[1.06]"
          />
          <div className="absolute inset-0 bg-linear-to-t from-deep/86 via-deep/44 via-[38%] to-transparent to-[74%]" />
          <span className="absolute left-4.5 top-4.5 flex h-6.5 items-center rounded-pill bg-brass px-2.75 text-[10px] font-semibold tracking-[0.12em] text-ink">
            −50%
          </span>
          <div className="absolute inset-x-5 bottom-4.5 flex items-end justify-between gap-3">
            <div>
              <p className="mb-1.75 text-[9.5px] uppercase tracking-[0.2em] text-white/72">
                {dictionary.home.promoBedroomEyebrow}
              </p>
              <p className="text-[16.5px] font-medium leading-[1.26] text-white">
                {dictionary.home.promoBedroomTitleLine1}
                <br />
                {dictionary.home.promoBedroomTitleLine2}
              </p>
            </div>
            <span className="flex h-7.5 w-7.5 flex-none items-center justify-center rounded-full border border-white/36 bg-white/18 text-white transition-colors duration-300 group-hover:bg-canvas group-hover:text-ink">
              <ArrowRightIcon className="h-[9px] w-[9px] stroke-current" />
            </span>
          </div>
        </Link>
      </div>
    </div>
  );
}
