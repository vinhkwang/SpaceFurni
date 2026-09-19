import type { ReactNode } from "react";
import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";

type ServicePromise = {
  title: string;
  body: string;
  icon: ReactNode;
};

const iconClassName = "h-[17px] w-[17px] stroke-current";

const deliveryIcon = (
  <svg
    viewBox="0 0 24 24"
    aria-hidden
    className={iconClassName}
    fill="none"
    strokeWidth={1.7}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M3 7.5h9v9H3z" />
    <path d="M12 10.5h4.2l2.8 3v3H12z" />
    <circle cx="7.5" cy="18" r="1.8" />
    <circle cx="16.5" cy="18" r="1.8" />
    <path d="M1.5 10.5h3M0.8 13.5h3.7" />
  </svg>
);

const warrantyIcon = (
  <svg
    viewBox="0 0 24 24"
    aria-hidden
    className={iconClassName}
    fill="none"
    strokeWidth={1.7}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M12 3 4.5 6v6c0 4.4 3.1 7.7 7.5 9 4.4-1.3 7.5-4.6 7.5-9V6z" />
    <path d="m9 12 2.2 2.2L15.4 10" />
  </svg>
);

const customDesignIcon = (
  <svg
    viewBox="0 0 24 24"
    aria-hidden
    className={iconClassName}
    fill="none"
    strokeWidth={1.7}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M14.6 3.6 20.4 9.4 9.8 20H4v-5.8z" />
    <path d="m12.6 5.6 5.8 5.8" />
    <path d="m8.4 9.8 2.4 2.4M6 12.2l2.4 2.4" />
  </svg>
);

const servicePromiseIcons: ReactNode[] = [deliveryIcon, warrantyIcon, customDesignIcon];

export async function ServicePromiseStrip() {
  const dictionary = getDictionary(await getLocale());
  const servicePromises: ServicePromise[] = dictionary.home.servicePromises.map((promise, index) => ({
    ...promise,
    icon: servicePromiseIcons[index],
  }));

  return (
    <section
      aria-label={dictionary.home.servicePromisesAriaLabel}
      className="grid grid-cols-1 gap-4 md:grid-cols-3"
    >
      {servicePromises.map((promise) => (
        <div
          key={promise.title}
          className="flex items-center gap-5 rounded-[14px] border border-hairline-soft bg-surface px-7 py-[26px] transition duration-300 hover:border-hairline hover:bg-white hover:shadow-2xl"
        >
          <span className="flex h-[52px] w-[52px] flex-none items-center justify-center rounded-full bg-deep text-white">
            {promise.icon}
          </span>
          <div>
            <h3 className="mb-[5px] text-[13.5px] font-semibold tracking-[0.03em]">
              {promise.title}
            </h3>
            <p className="text-[12px] leading-[1.5] text-ink-muted">{promise.body}</p>
          </div>
        </div>
      ))}
    </section>
  );
}
