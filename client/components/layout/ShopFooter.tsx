import Image from "next/image";
import Link from "next/link";
import { getDictionary, type Dictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { Container } from "@/components/ui/Container";

type FooterLink = {
  label: string;
  href: string | null;
};

function aboutLinks(dictionary: Dictionary): FooterLink[] {
  return [
    { label: dictionary.footer.ourStory, href: null },
    { label: dictionary.footer.theWorkshop, href: null },
    { label: dictionary.footer.showroom, href: null },
    { label: dictionary.footer.materials, href: null },
    { label: dictionary.footer.journal, href: null },
    { label: dictionary.footer.careers, href: null },
  ];
}

function customerServiceLinks(dictionary: Dictionary): FooterLink[] {
  return [
    { label: dictionary.footer.trackMyOrder, href: "/account/orders" },
    { label: dictionary.footer.deliveryAndAssembly, href: null },
    { label: dictionary.footer.returns, href: null },
    { label: dictionary.footer.careAndRepair, href: null },
    { label: dictionary.footer.wishlist, href: null },
    { label: dictionary.footer.termsOfUse, href: null },
  ];
}

const socialAccounts = [
  { name: "Facebook", initials: "Fb" },
  { name: "Instagram", initials: "Ig" },
  { name: "Twitter", initials: "Tw" },
];

const acceptedPaymentMethods = ["Visa", "Mastercard", "JCB"] as const;

const columnHeadingClassName =
  "text-[11px] font-semibold uppercase tracking-[0.18em] text-ink";

const inertLinkClassName = "text-[12.5px] text-ink-soft";

const activeLinkClassName =
  "text-[12.5px] text-ink-soft transition-colors duration-200 hover:text-terracotta";

const inertPillClassName =
  "flex h-[46px] cursor-not-allowed items-center justify-center gap-2.5 rounded-pill text-[10.5px] font-semibold uppercase tracking-[0.13em] opacity-50";

function renderFooterLink(link: FooterLink) {
  if (!link.href) {
    return (
      <span key={link.label} className={inertLinkClassName}>
        {link.label}
      </span>
    );
  }
  return (
    <Link key={link.label} href={link.href} className={activeLinkClassName}>
      {link.label}
    </Link>
  );
}

export async function ShopFooter() {
  const locale = await getLocale();
  const dictionary = getDictionary(locale);

  return (
    <footer className="mt-24">
      <Container className="flex flex-col items-start justify-between gap-10 border-y border-hairline py-14 lg:flex-row lg:items-center">
        <div>
          <p className="mb-3 text-[10.5px] uppercase tracking-[0.22em] text-terracotta">
            {dictionary.footer.newsletterEyebrow}
          </p>
          <h2 className="mb-2 text-[27px] font-medium tracking-[-0.015em]">
            {dictionary.footer.newsletterHeading}
          </h2>
          <p className="text-[12.5px] text-ink-muted">{dictionary.footer.newsletterBody}</p>
        </div>
        <div className="flex w-full shrink-0 flex-col gap-2.5 sm:w-auto sm:flex-row">
          <input
            type="email"
            disabled
            placeholder={dictionary.footer.emailPlaceholder}
            aria-label={dictionary.footer.emailPlaceholder}
            className="h-14 w-full cursor-not-allowed rounded-pill border border-hairline bg-white px-[22px] text-[13px] opacity-50 sm:w-[300px]"
          />
          <button
            type="button"
            disabled
            className="flex h-14 cursor-not-allowed items-center justify-center gap-3 rounded-pill bg-deep px-[30px] text-[11.5px] font-semibold uppercase tracking-[0.14em] text-white opacity-50"
          >
            {dictionary.footer.subscribe}
          </button>
        </div>
      </Container>

      <div className="bg-surface">
        <Container className="grid gap-12 pb-11 pt-15 lg:grid-cols-[1.3fr_1fr_1fr_1.1fr]">
          <div>
            <div className="mb-5 flex items-center gap-3">
              <span className="flex h-[30px] w-[78px] items-start overflow-hidden">
                <Image
                  src="/images/logo.png"
                  alt="SpaceFurni"
                  width={78}
                  height={49}
                  className="w-[78px]"
                />
              </span>
              <span className="flex gap-[0.34em] text-[16px] tracking-[0.14em]">
                <span className="font-bold">SPACE</span>
                <span className="font-light text-ink-soft">FURNI</span>
              </span>
            </div>
            <p className="mb-6 max-w-[290px] text-[12.5px] leading-[1.75] text-ink-soft">
              {dictionary.footer.tagline}
            </p>
            <div className="flex gap-2">
              {socialAccounts.map((account) => (
                <span
                  key={account.name}
                  aria-label={account.name}
                  className="flex h-9 w-9 items-center justify-center rounded-full bg-white text-[11px] font-semibold text-ink-soft"
                >
                  {account.initials}
                </span>
              ))}
            </div>
          </div>

          <div className="flex flex-col gap-3.5">
            <h3 className={`${columnHeadingClassName} mb-1.5`}>{dictionary.footer.aboutUs}</h3>
            {aboutLinks(dictionary).map(renderFooterLink)}
          </div>

          <div className="flex flex-col gap-3.5">
            <h3 className={`${columnHeadingClassName} mb-1.5`}>{dictionary.footer.customerService}</h3>
            {customerServiceLinks(dictionary).map(renderFooterLink)}
          </div>

          <div>
            <h3 className={`${columnHeadingClassName} mb-5`}>{dictionary.footer.talkToUs}</h3>
            <div className="mb-6 flex flex-col gap-2.5">
              <button type="button" disabled className={`${inertPillClassName} bg-deep text-white`}>
                {dictionary.footer.callUsNow}
              </button>
              <button
                type="button"
                disabled
                className={`${inertPillClassName} border border-hairline text-ink`}
              >
                {dictionary.footer.quickService}
              </button>
            </div>
            <div className="flex flex-col gap-3">
              {[dictionary.footer.customerServiceDesk, dictionary.footer.shoppingAssistant].map((desk) => (
                <div key={desk}>
                  <p className="text-[12.5px] font-semibold">{desk}</p>
                  <p className="mt-[3px] text-[11.5px] text-ink-muted">
                    <span className="text-success">● {dictionary.footer.openClosesAt}</span>
                  </p>
                </div>
              ))}
            </div>
          </div>
        </Container>

        <Container className="flex flex-col items-center justify-between gap-4 border-t border-hairline-soft pb-7 pt-5 sm:flex-row">
          <p className="text-[11.5px] text-ink-muted">{dictionary.footer.copyright}</p>
          <div className="flex items-center gap-3.5">
            {acceptedPaymentMethods.map((method) => (
              <span
                key={method}
                className="rounded-[4px] border border-hairline-soft px-2 py-1 text-[9.5px] uppercase tracking-[0.1em] text-ink-muted"
              >
                {method}
              </span>
            ))}
            <span className="rounded-[4px] border border-hairline-soft px-2 py-1 text-[9.5px] uppercase tracking-[0.1em] text-ink-muted">
              {dictionary.footer.cash}
            </span>
          </div>
        </Container>
      </div>
    </footer>
  );
}
