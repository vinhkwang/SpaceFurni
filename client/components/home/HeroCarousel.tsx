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
  "absolute top-1/2 z-10 flex h-[42px] w-[42px] -translate-y-1/2 cursor-pointer items-center justify-center rounded-full border border-white/30 bg-canvas/15 text-white backdrop-blur-sm transition-colors duration-200 hover:bg-canvas hover:text-ink";

function slideCounterLabel(activeIndex: number, slideCount: number): string {
  return `0${activeIndex + 1} / 0${slideCount}`;
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
      className="relative h-[524px] overflow-hidden rounded-2xl bg-surface"
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
          <div className="absolute inset-0 bg-linear-to-r from-deep/55 via-deep/10 to-transparent" />
          <div className="absolute bottom-24 left-12 flex max-w-[400px] flex-col items-start gap-3.5">
            <span className="text-[10.5px] uppercase tracking-[0.22em] text-sand">
              {slide.eyebrow}
            </span>
            <h2 className="text-[44px] font-medium leading-[1.06] tracking-[-0.01em] text-white">
              {slide.title}
            </h2>
            <p className="max-w-[330px] text-[13.5px] leading-[1.6] text-white/80">{slide.body}</p>
            <Link
              href={`/category/${slide.categorySlug}`}
              tabIndex={slideIndex === activeIndex ? undefined : -1}
              className="mt-1.5 flex h-[46px] items-center gap-3 rounded-pill bg-canvas px-[26px] text-[11.5px] font-semibold uppercase tracking-[0.14em] text-ink transition-colors duration-300 hover:bg-terracotta hover:text-white"
            >
              {slide.callToAction}
            </Link>
          </div>
        </div>
      ))}

      <button
        type="button"
        aria-label={dictionary.home.previousSlide}
        onClick={showPreviousSlide}
        className={`${arrowButtonClassName} left-[22px]`}
      >
        <svg
          viewBox="0 0 24 24"
          aria-hidden
          className="h-3 w-3 stroke-current"
          fill="none"
          strokeWidth={2.5}
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="m15 18-6-6 6-6" />
        </svg>
      </button>
      <button
        type="button"
        aria-label={dictionary.home.nextSlide}
        onClick={showNextSlide}
        className={`${arrowButtonClassName} right-[22px]`}
      >
        <svg
          viewBox="0 0 24 24"
          aria-hidden
          className="h-3 w-3 stroke-current"
          fill="none"
          strokeWidth={2.5}
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="m9 18 6-6-6-6" />
        </svg>
      </button>

      <div className="absolute bottom-11 left-12 z-10 flex items-center gap-3">
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
        <span className="ml-2 text-[10.5px] tracking-[0.14em] text-white/75">
          {slideCounterLabel(activeIndex, heroSlides.length)}
        </span>
      </div>
    </section>
  );
}
