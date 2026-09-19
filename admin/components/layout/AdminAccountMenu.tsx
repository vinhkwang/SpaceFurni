"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import type { CurrentUserResponse } from "@/lib/api/types";
import { useDictionary } from "@/lib/i18n/LocaleProvider";

type AdminAccountMenuProps = {
  currentUser: CurrentUserResponse;
  userRole: string;
};

function initialsFor(fullName: string): string {
  return fullName
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((namePart) => namePart[0]?.toUpperCase())
    .join("");
}

export function AdminAccountMenu({ currentUser, userRole }: AdminAccountMenuProps) {
  const router = useRouter();
  const dictionary = useDictionary();
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function closeOnOutsidePointerDown(event: PointerEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    document.addEventListener("pointerdown", closeOnOutsidePointerDown);
    return () => document.removeEventListener("pointerdown", closeOnOutsidePointerDown);
  }, []);

  async function signOut() {
    setIsOpen(false);
    await fetch("/api/session/logout", { method: "POST" });
    router.replace("/login");
    router.refresh();
  }

  return (
    <div ref={containerRef} className="relative">
      <button
        type="button"
        onClick={() => setIsOpen((currentIsOpen) => !currentIsOpen)}
        aria-expanded={isOpen}
        aria-haspopup="menu"
        aria-label={dictionary.accountMenu.accountMenuAriaLabel}
        className="flex cursor-pointer items-center gap-3.5 border-l border-hairline pl-4.5 outline-none [-webkit-tap-highlight-color:transparent]"
      >
        <span className="flex h-9.5 w-9.5 items-center justify-center rounded-pill bg-deep text-[12.5px] font-semibold text-white">
          {initialsFor(currentUser.fullName)}
        </span>
        <div className="text-left">
          <div className="text-[12.5px] font-semibold text-ink">{currentUser.fullName}</div>
          <div className="text-[10.5px] text-ink-muted">{userRole}</div>
        </div>
      </button>

      {isOpen ? (
        <div
          role="menu"
          className="absolute right-0 top-[54px] z-60 w-[200px] rounded-2xl border border-hairline bg-white py-2 shadow-lg"
        >
          <Link
            href="/profile"
            role="menuitem"
            onClick={() => setIsOpen(false)}
            className="block px-5 py-2.5 text-[12.5px] text-ink transition-colors duration-200 hover:bg-surface"
          >
            {dictionary.accountMenu.profile}
          </Link>
          <button
            type="button"
            role="menuitem"
            onClick={signOut}
            className="block w-full cursor-pointer px-5 py-2.5 text-left text-[12.5px] text-terracotta transition-colors duration-200 hover:bg-surface"
          >
            {dictionary.accountMenu.signOut}
          </button>
        </div>
      ) : null}
    </div>
  );
}
