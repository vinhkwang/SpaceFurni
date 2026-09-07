import Link from "next/link";
import { LoginForm } from "@/components/auth/LoginForm";

export const metadata = {
  title: "Sign in",
};

function internalRedirectTarget(value: string | string[] | undefined): string {
  const requestedPath = Array.isArray(value) ? value[0] : value;
  if (!requestedPath || !requestedPath.startsWith("/") || requestedPath.startsWith("//")) {
    return "/";
  }
  return requestedPath;
}

export default async function LoginPage(props: PageProps<"/login">) {
  const searchParams = await props.searchParams;
  const redirectTo = internalRedirectTarget(searchParams.redirect);

  return (
    <div className="w-full max-w-[400px] lg:min-h-[705px]">
      <div className="mb-9 flex gap-1 rounded-pill bg-surface p-1">
        <span className="flex h-11 flex-1 items-center justify-center rounded-pill bg-white text-[11px] font-semibold uppercase tracking-[0.13em] text-ink shadow-sm">
          Sign in
        </span>
        <Link
          href="/signup"
          className="flex h-11 flex-1 items-center justify-center rounded-pill text-[11px] font-semibold uppercase tracking-[0.13em] text-ink-muted transition-colors duration-200 hover:text-ink"
        >
          Create account
        </Link>
      </div>

      <h1 className="mb-2.5 text-[32px] font-medium tracking-[-0.02em]">Welcome back</h1>
      <p className="mb-8 text-[13px] leading-[1.65] text-ink-muted">
        Sign in to see your orders, saved rooms and wishlist.
      </p>

      <LoginForm redirectTo={redirectTo} />

      <Link
        href="/"
        className="mt-3 block text-center text-[11.5px] uppercase tracking-[0.1em] text-ink-muted transition-colors duration-200 hover:text-ink"
      >
        ← Back to the store
      </Link>
    </div>
  );
}
