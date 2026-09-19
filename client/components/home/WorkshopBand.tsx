import Image from "next/image";
import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";

export async function WorkshopBand() {
  const dictionary = getDictionary(await getLocale());
  const workshopStatistics = dictionary.home.workshopStatistics;

  return (
    <section
      aria-labelledby="workshop-band-heading"
      className="grid overflow-hidden rounded-[18px] border border-hairline-soft md:grid-cols-2"
    >
      <div className="relative min-h-[280px] md:min-h-[420px]">
        <Image
          src="/images/room-living.jpg"
          alt=""
          fill
          sizes="(min-width: 768px) 50vw, 100vw"
          className="object-cover"
        />
      </div>
      <div className="flex flex-col justify-center gap-[18px] bg-surface px-8 py-14 md:px-[60px] md:py-14">
        <p className="text-[10.5px] uppercase tracking-[0.22em] text-terracotta">{dictionary.home.workshopEyebrow}</p>
        <h2
          id="workshop-band-heading"
          className="text-pretty text-[33px] font-medium leading-[1.14] tracking-[-0.015em]"
        >
          {dictionary.home.workshopTitle}
        </h2>
        <p className="max-w-[420px] text-[13.5px] leading-[1.75] text-ink-soft">{dictionary.home.workshopBody}</p>
        <dl className="mt-3.5 grid grid-cols-3 gap-[22px] border-t border-hairline pt-[26px]">
          {workshopStatistics.map((statistic) => (
            <div key={statistic.label} className="flex flex-col-reverse">
              <dt className="mt-[5px] text-[10.5px] uppercase tracking-[0.13em] text-ink-muted">
                {statistic.label}
              </dt>
              <dd className="text-[27px] font-semibold tracking-[-0.02em]">{statistic.value}</dd>
            </div>
          ))}
        </dl>
      </div>
    </section>
  );
}
