"use client";

import Image from "next/image";
import { useDictionary } from "@/lib/i18n/LocaleProvider";

export function LogoLockup() {
  const dictionary = useDictionary();

  return (
    <span className="flex items-center gap-3.5">
      <span className="flex h-[34px] w-[88px] items-start overflow-hidden">
        <Image
          src="/images/logo.png"
          alt="SpaceFurni"
          width={88}
          height={55}
          priority
          className="w-[88px]"
        />
      </span>
      <span className="flex flex-col gap-[3px] border-l border-hairline pl-3.5">
        <span className="flex gap-[0.34em] text-[19px] leading-none tracking-[0.14em]">
          <span className="font-bold">SPACE</span>
          <span className="font-light text-ink-soft">FURNI</span>
        </span>
        <span className="text-[9.5px] uppercase tracking-[0.14em] text-ink-muted">
          {dictionary.header.tagline}
        </span>
      </span>
    </span>
  );
}
