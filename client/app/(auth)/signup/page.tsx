import Image from "next/image";
import Link from "next/link";
import { SignUpForm } from "@/components/auth/SignUpForm";

export const metadata = {
  title: "Create account",
};

function internalRedirectTarget(value: string | string[] | undefined): string {
  const requestedPath = Array.isArray(value) ? value[0] : value;
  if (!requestedPath || !requestedPath.startsWith("/") || requestedPath.startsWith("//")) {
    return "/";
  }
  return requestedPath;
}

export default async function SignUpPage(props: PageProps<"/signup">) {
  const searchParams = await props.searchParams;
  const redirectTo = internalRedirectTarget(searchParams.redirect);

  return (
    <main className="grid min-h-screen grid-cols-1 lg:grid-cols-2">
      <div className="relative hidden overflow-hidden lg:block">
        <Image
          src="/images/login-bg.jpg"
          alt=""
          fill
          priority
          sizes="50vw"
          className="object-cover"
        />
        <div className="absolute inset-0 bg-linear-to-b from-deep/60 via-deep/20 to-deep/80" />
        <Link href="/" className="absolute left-13 top-11 flex items-center gap-3.5">
          <span className="flex h-[58px] w-[58px] shrink-0 items-center justify-center rounded-2xl bg-canvas">
            <span className="flex h-[17px] w-[42px] items-start justify-center overflow-hidden">
              <Image src="/images/logo.png" alt="" width={42} height={26} className="w-[42px]" />
            </span>
          </span>
          <span>
            <span className="flex gap-[0.34em] text-[19px] leading-none tracking-[0.14em] text-white">
              <span className="font-bold">SPACE</span>
              <span className="font-light text-white/75">FURNI</span>
            </span>
            <span className="mt-1.5 block text-[9.5px] uppercase tracking-[0.16em] text-white/60">
              Furniture for real homes
            </span>
          </span>
        </Link>
        <p className="absolute inset-x-13 bottom-14 max-w-[460px] text-[31px] leading-[1.28] tracking-[-0.01em] text-white">
          “They measured the room, drew the layout, and carried the sofa up four floors. It has
          been our favourite seat for three years.”
        </p>
      </div>

      <div className="flex items-center justify-center px-6 py-15">
        <div className="w-full max-w-[400px]">
          <div className="mb-9 flex gap-1 rounded-pill bg-surface p-1">
            <Link
              href="/login"
              className="flex h-11 flex-1 items-center justify-center rounded-pill text-[11px] font-semibold uppercase tracking-[0.13em] text-ink-muted transition-colors duration-200 hover:text-ink"
            >
              Sign in
            </Link>
            <span className="flex h-11 flex-1 items-center justify-center rounded-pill bg-white text-[11px] font-semibold uppercase tracking-[0.13em] text-ink shadow-sm">
              Create account
            </span>
          </div>

          <h1 className="mb-2.5 text-[32px] font-medium tracking-[-0.02em]">
            Create your account
          </h1>
          <p className="mb-8 text-[13px] leading-[1.65] text-ink-muted">
            Save your wishlist, track deliveries and get first access to each Thursday drop.
          </p>

          <SignUpForm redirectTo={redirectTo} />

          <Link
            href="/"
            className="mt-3 block text-center text-[11.5px] uppercase tracking-[0.1em] text-ink-muted transition-colors duration-200 hover:text-ink"
          >
            ← Back to the store
          </Link>
        </div>
      </div>
    </main>
  );
}
