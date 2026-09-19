"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useState } from "react";
import { useDictionary } from "@/lib/i18n/LocaleProvider";

type HeroSlide = {
  imageUrl: string;
  eyebrow: string;
  title: string;
  body: string;
  callToAction: string;
  categorySlug: string;
};

const SLIDE_INTERVAL_MILLISECONDS = 6000;

const HERO_SLIDE_ASSETS = [
  { imageUrl: "/images/room-living.jpg", categorySlug: "living-room" },
  { imageUrl: "/images/room-kitchen.jpg", categorySlug: "kitchen" },
  { imageUrl: "/images/room-bedroom.png", categorySlug: "bedroom" },
];

const arrowButtonClassName =
  "flex h-11 w-11 cursor-pointer items-center justify-center rounded-full border border-canvas/34 bg-canvas/14 text-white backdrop-blur-[8px] transition-colors duration-200 hover:border-canvas hover:bg-canvas hover:text-ink";

function slideCounterLabel(activeIndex: number, slideCount: number): string {
  return `0${activeIndex + 1} / 0${slideCount}`;
}

function ArrowLeftIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className="h-[11px] w-[11px] stroke-current"
      fill="none"
      strokeWidth={2.5}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M19 12H5m6-7-7 7 7 7" />
    </svg>
  );
}

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
      <path d="M5 12h14m-6-7 7 7-7 7" />
    </svg>
  );
}

export function HeroCarousel() {
  const dictionary = useDictionary();
  const heroSlides: HeroSlide[] = HERO_SLIDE_ASSETS.map((asset, slideIndex) => ({
    ...asset,
    ...dictionary.home.heroSlides[slideIndex],
  }));
  const [activeIndex, setActiveIndex] = useState(0);
  const [isPaused, setIsPaused] = useState(false);

  useEffect(() => {
    if (isPaused) {
      return;
    }
    const advanceTimer = setInterval(() => {
      setActiveIndex((currentIndex) => (currentIndex + 1) % HERO_SLIDE_ASSETS.length);
    }, SLIDE_INTERVAL_MILLISECONDS);
    return () => clearInterval(advanceTimer);
  }, [isPaused]);

  function showPreviousSlide() {
    setActiveIndex((currentIndex) => (currentIndex + HERO_SLIDE_ASSETS.length - 1) % HERO_SLIDE_ASSETS.length);
  }

  function showNextSlide() {
    setActiveIndex((currentIndex) => (currentIndex + 1) % HERO_SLIDE_ASSETS.length);
  }

  return (
    <section
      aria-label={dictionary.home.heroCarouselAriaLabel}
      onMouseEnter={() => setIsPaused(true)}
      onMouseLeave={() => setIsPaused(false)}
      className="relative h-[524px] overflow-hidden rounded-2xl bg-surface ring-1 ring-inset ring-hairline-soft"
    >
      {heroSlides.map((slide, slideIndex) => (
        <div
          key={slide.categorySlug}
          aria-hidden={slideIndex !== activeIndex}
          className={`absolute inset-0 transition-opacity duration-700 ${
            slideIndex === activeIndex ? "opacity-100" : "pointer-events-none opacity-0"
          }`}
        >
          <Image
            src={slide.imageUrl}
            alt=""
            fill
            priority={slideIndex === 0}
            sizes="100vw"
            className="object-cover"
          />
          <div className="absolute inset-0 bg-linear-to-r from-espresso/75 via-espresso/50 to-transparent" />
          <div className="absolute inset-0 bg-linear-to-t from-espresso/40 to-transparent to-[32%]" />
          <div className="absolute bottom-28 left-13 flex max-w-[420px] flex-col items-start gap-3.75">
            <span className="flex items-center gap-2.75 text-[10.5px] uppercase tracking-[0.24em] text-sand">
              <span aria-hidden className="h-px w-6.5 bg-sand/70" />
              {slide.eyebrow}
            </span>
            <h2 className="text-pretty text-[47px] font-medium leading-[1.04] tracking-[-0.015em] text-white">
              {slide.title}
            </h2>
            <p className="max-w-[340px] text-[13.5px] leading-[1.7] text-white/88">{slide.body}</p>
            <Link
              href={`/category/${slide.categorySlug}`}
              tabIndex={slideIndex === activeIndex ? undefined : -1}
              className="mt-2 flex h-12 items-center gap-3 rounded-pill bg-canvas px-7 text-[11.5px] font-semibold uppercase tracking-[0.14em] text-ink shadow-[0_14px_30px_-18px_var(--tw-shadow-color)] shadow-espresso/70 transition-colors duration-300 hover:bg-terracotta hover:text-white"
            >
              {slide.callToAction}
            </Link>
          </div>
        </div>
      ))}

      <div className="absolute bottom-11 right-7.5 z-10 flex items-center gap-2.25">
        <button
          type="button"
          aria-label={dictionary.home.previousSlide}
          onClick={showPreviousSlide}
          className={arrowButtonClassName}
        >
          <ArrowLeftIcon />
        </button>
        <button
          type="button"
          aria-label={dictionary.home.nextSlide}
          onClick={showNextSlide}
          className={arrowButtonClassName}
        >
          <ArrowRightIcon className="h-[11px] w-[11px] stroke-current" />
        </button>
      </div>

      <div className="absolute bottom-13 left-13 z-10 flex items-center gap-3.5">
        <div className="flex items-center gap-2">
          {heroSlides.map((slide, slideIndex) => (
            <button
              key={slide.categorySlug}
              type="button"
              aria-label={dictionary.home.showSlideAria(slide.eyebrow)}
              aria-current={slideIndex === activeIndex}
              onClick={() => setActiveIndex(slideIndex)}
              className={`h-[3px] cursor-pointer rounded-sm transition-all duration-500 ${
                slideIndex === activeIndex ? "w-11 bg-white" : "w-[18px] bg-white/45"
              }`}
            />
          ))}
        </div>
        <span className="text-[10.5px] font-semibold tracking-[0.16em] text-white">
          {slideCounterLabel(activeIndex, heroSlides.length)}
        </span>
      </div>
    </section>
  );
}
