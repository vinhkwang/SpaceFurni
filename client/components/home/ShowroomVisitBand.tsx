import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";

const iconClassName = "h-3 w-3 stroke-current";

function CalendarCheckIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className={iconClassName}
      fill="none"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <rect x="3.5" y="5" width="17" height="15" rx="2" />
      <path d="M3.5 9.5h17M8 3v3.5M16 3v3.5" />
      <path d="m8.5 13.5 2 2 4.5-4.5" />
    </svg>
  );
}

function LocationArrowIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className="h-2.5 w-2.5 stroke-current"
      fill="none"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M20 4 3.5 10.5 11 13l2.5 7.5z" />
    </svg>
  );
}

function PhoneIcon() {
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
      <path d="M6 3.5h3.2l1.6 4-2 1.6a12 12 0 0 0 5.1 5.1l1.6-2 4 1.6V17a2 2 0 0 1-2 2C10.6 19 4 12.4 4 5.5a2 2 0 0 1 2-2Z" />
    </svg>
  );
}

export async function ShowroomVisitBand() {
  const dictionary = getDictionary(await getLocale());
  const showroomSteps = dictionary.home.showroomSteps;
  const directionsQuery = encodeURIComponent(
    `${dictionary.home.showroomAddressLine1}, ${dictionary.home.showroomAddressLine2}`,
  );

  return (
    <section
      aria-labelledby="showroom-band-heading"
      className="grid overflow-hidden rounded-[18px] bg-deep text-canvas md:grid-cols-[1.05fr_1fr]"
    >
      <div className="flex flex-col gap-4.5 border-b border-canvas/12 px-8 py-13 md:border-b-0 md:border-r md:px-14 md:py-13">
        <p className="flex items-center gap-2.75 text-[10.5px] uppercase tracking-[0.24em] text-sand">
          <span aria-hidden className="h-px w-5.5 bg-sand/60" />
          {dictionary.home.showroomEyebrow}
        </p>
        <h2 id="showroom-band-heading" className="text-pretty text-[31px] font-medium leading-[1.14] tracking-[-0.015em] text-white">
          {dictionary.home.showroomTitle}
        </h2>
        <p className="max-w-[400px] text-[13.5px] leading-[1.75] text-canvas/72">{dictionary.home.showroomBody}</p>
        <div className="mt-1.5 flex gap-11">
          <div>
            <p className="mb-2 text-[9.5px] uppercase tracking-[0.2em] text-ink-muted">
              {dictionary.home.showroomAddressLabel}
            </p>
            <p className="text-[13px] leading-[1.6] text-canvas">
              {dictionary.home.showroomAddressLine1}
              <br />
              {dictionary.home.showroomAddressLine2}
            </p>
          </div>
          <div>
            <p className="mb-2 text-[9.5px] uppercase tracking-[0.2em] text-ink-muted">
              {dictionary.home.showroomOpenLabel}
            </p>
            <p className="text-[13px] leading-[1.6] text-canvas">
              {dictionary.home.showroomOpenLine1}
              <br />
              {dictionary.home.showroomOpenLine2}
            </p>
          </div>
        </div>
        <div className="mt-4 flex gap-2.5">
          <button
            type="button"
            disabled
            className="flex h-12 cursor-not-allowed items-center gap-3 rounded-pill bg-canvas px-7 text-[11px] font-semibold uppercase tracking-[0.14em] text-ink opacity-50"
          >
            <CalendarCheckIcon />
            {dictionary.home.showroomBookVisit}
          </button>
          <a
            href={`https://www.google.com/maps/search/?api=1&query=${directionsQuery}`}
            target="_blank"
            rel="noopener noreferrer"
            className="flex h-12 items-center gap-2.75 rounded-pill border border-canvas/28 px-6.5 text-[11px] font-semibold uppercase tracking-[0.14em] text-canvas transition-colors duration-300 hover:border-canvas/50 hover:bg-canvas/10"
          >
            <LocationArrowIcon />
            {dictionary.home.showroomDirections}
          </a>
        </div>
      </div>
      <div className="flex flex-col gap-5 px-8 py-13 md:px-14 md:py-13">
        <p className="text-[9.5px] uppercase tracking-[0.24em] text-ink-muted">{dictionary.home.showroomProcessEyebrow}</p>
        {showroomSteps.map((step, stepIndex) => (
          <div
            key={step.title}
            className={`flex items-start gap-5 ${
              stepIndex === showroomSteps.length - 1 ? "" : "border-b border-canvas/12 pb-5"
            }`}
          >
            <span className="pt-[3px] text-[11px] font-semibold tracking-[0.14em] text-brass">
              {`0${stepIndex + 1}`}
            </span>
            <div>
              <p className="mb-1.5 text-[15px] font-medium text-white">{step.title}</p>
              <p className="text-[12.5px] leading-[1.65] text-canvas/66">{step.body}</p>
            </div>
          </div>
        ))}
        <div className="mt-auto flex items-center gap-3.25 pt-6.5">
          <span className="flex h-9.5 w-9.5 flex-none items-center justify-center rounded-full border border-canvas/20 bg-canvas/10 text-sand">
            <PhoneIcon />
          </span>
          <div>
            <p className="mb-1 text-[9.5px] uppercase tracking-[0.2em] text-ink-muted">
              {dictionary.home.showroomTalkToDesigner}
            </p>
            <p className="text-[14px] font-semibold tracking-[0.02em] text-white">{dictionary.home.showroomPhoneNumber}</p>
          </div>
        </div>
      </div>
    </section>
  );
}
