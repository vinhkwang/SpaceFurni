import Image from "next/image";

type WorkshopStatistic = {
  value: string;
  label: string;
};

const workshopStatistics: WorkshopStatistic[] = [
  { value: "240", label: "Pieces in stock" },
  { value: "10 yr", label: "Frame guarantee" },
  { value: "4.8", label: "Average rating" },
];

export function WorkshopBand() {
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
        <p className="text-[10.5px] uppercase tracking-[0.22em] text-terracotta">Our workshop</p>
        <h2
          id="workshop-band-heading"
          className="text-pretty text-[33px] font-medium leading-[1.14] tracking-[-0.015em]"
        >
          Made in Hanoi. Built to be sat on.
        </h2>
        <p className="max-w-[420px] text-[13.5px] leading-[1.75] text-ink-soft">
          We joint our frames from kiln-dried acacia and upholster in Vietnamese linen. Nothing ships
          flat-packed in a box you have to fight — it arrives finished, and we carry it in.
        </p>
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
