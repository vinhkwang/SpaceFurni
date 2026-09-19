"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import type { CurrentUserResponse } from "@/lib/api/types";
import { useDictionary } from "@/lib/i18n/LocaleProvider";

type AccountMenuProps = {
  currentUser: CurrentUserResponse;
};

function userInitials(fullName: string): string {
  const initials = fullName
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .map((namePart) => namePart.charAt(0).toUpperCase());
  if (initials.length === 0) {
    return "?";
  }
  return initials.length === 1 ? initials[0] : `${initials[0]}${initials[initials.length - 1]}`;
}

export function AccountMenu({ currentUser }: AccountMenuProps) {
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
    router.push("/");
    router.refresh();
  }

  return (
    <div ref={containerRef} className="relative">
      <button
        type="button"
        onClick={() => setIsOpen((currentIsOpen) => !currentIsOpen)}
        title={currentUser.fullName}
        aria-expanded={isOpen}
        aria-haspopup="menu"
        className="flex h-[46px] cursor-pointer items-center gap-[9px] rounded-pill bg-surface px-2 transition-colors duration-200 hover:bg-deep hover:text-white"
      >
        <span className="flex h-[30px] w-[30px] items-center justify-center rounded-full bg-white text-[11px] font-semibold text-deep">
          {userInitials(currentUser.fullName)}
        </span>
      </button>

      {isOpen ? (
        <div
          role="menu"
          className="absolute right-0 top-[54px] z-60 w-[200px] rounded-2xl border border-hairline bg-white py-2 shadow-lg"
        >
          <Link
            href="/account/profile"
            role="menuitem"
            onClick={() => setIsOpen(false)}
            className="block px-5 py-2.5 text-[12.5px] text-ink transition-colors duration-200 hover:bg-surface"
          >
            {dictionary.accountMenu.profile}
          </Link>
          <Link
            href="/account/orders"
            role="menuitem"
            onClick={() => setIsOpen(false)}
            className="block px-5 py-2.5 text-[12.5px] text-ink transition-colors duration-200 hover:bg-surface"
          >
            {dictionary.accountMenu.orders}
          </Link>
          <button
            type="button"
            role="menuitem"
            onClick={signOut}
            className="block w-full cursor-pointer px-5 py-2.5 text-left text-[12.5px] text-terracotta transition-colors duration-200 hover:bg-surface"
          >
            {dictionary.accountMenu.logout}
          </button>
        </div>
      ) : null}
    </div>
  );
}
