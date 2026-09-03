"use client";

import Image from "next/image";
import { useState, type MouseEvent } from "react";
import type { ProductBadgeResponse } from "@/lib/api/types";
import { Badge } from "@/components/ui/Badge";

type ProductGalleryProps = {
  images: string[];
  productName: string;
  badge: ProductBadgeResponse | null;
};

type ZoomOrigin = {
  x: number;
  y: number;
};

const expandIcon = (
  <svg
    viewBox="0 0 24 24"
    aria-hidden
    className="h-[11px] w-[11px] stroke-current"
    fill="none"
    strokeWidth={2}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M9 3H4v5M15 3h5v5M9 21H4v-5M15 21h5v-5" />
  </svg>
);

export function ProductGallery({ images, productName, badge }: ProductGalleryProps) {
  const [activeImageIndex, setActiveImageIndex] = useState(0);
  const [zoomOrigin, setZoomOrigin] = useState<ZoomOrigin>({ x: 50, y: 50 });
  const [isZoomed, setIsZoomed] = useState(false);

  const activeImageUrl = images[activeImageIndex];

  function trackZoomOrigin(pointerEvent: MouseEvent<HTMLDivElement>) {
    const bounds = pointerEvent.currentTarget.getBoundingClientRect();
    setZoomOrigin({
      x: ((pointerEvent.clientX - bounds.left) / bounds.width) * 100,
      y: ((pointerEvent.clientY - bounds.top) / bounds.height) * 100,
    });
  }

  return (
    <div>
      <div
        className="relative flex aspect-[4/3.1] items-center justify-center overflow-hidden rounded-[18px] bg-surface p-16"
        onMouseMove={trackZoomOrigin}
        onMouseEnter={() => setIsZoomed(true)}
        onMouseLeave={() => setIsZoomed(false)}
      >
        {activeImageUrl ? (
          <Image
            src={activeImageUrl}
            alt={productName}
            fill
            sizes="(min-width: 1024px) 50vw, 100vw"
            className="object-contain mix-blend-multiply transition-transform duration-300 ease-out"
            style={{
              transformOrigin: `${zoomOrigin.x}% ${zoomOrigin.y}%`,
              transform: isZoomed ? "scale(1.8)" : "scale(1)",
            }}
          />
        ) : null}
        {badge ? (
          <span className="absolute left-5.5 top-5.5">
            <Badge variant={badge.variant}>{badge.label}</Badge>
          </span>
        ) : null}
        <span className="absolute bottom-5 right-5.5 flex items-center gap-2 text-[10.5px] uppercase tracking-[0.12em] text-ink-muted">
          {expandIcon}
          Hover to zoom
        </span>
      </div>

      {images.length > 1 ? (
        <div className="mt-3 grid grid-cols-4 gap-3">
          {images.map((imageUrl, imageIndex) => {
            const isActiveThumbnail = imageIndex === activeImageIndex;

            return (
              <button
                key={imageIndex}
                type="button"
                onClick={() => setActiveImageIndex(imageIndex)}
                aria-label={`Show image ${imageIndex + 1} of ${images.length}`}
                aria-current={isActiveThumbnail ? "true" : undefined}
                className={`flex aspect-[1/0.82] items-center justify-center rounded-[11px] border bg-surface-warm p-3.5 transition-colors duration-250 ${
                  isActiveThumbnail ? "border-ink" : "border-hairline-soft hover:border-ink-muted"
                }`}
              >
                <div className="relative h-full w-full">
                  <Image
                    src={imageUrl}
                    alt=""
                    fill
                    sizes="120px"
                    className="object-contain mix-blend-multiply"
                  />
                </div>
              </button>
            );
          })}
        </div>
      ) : null}
    </div>
  );
}
