import Image from "next/image";
import Link from "next/link";

export default function AuthLayout({ children }: { children: React.ReactNode }) {
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

      <div className="flex items-center justify-center px-6 py-15">{children}</div>
    </main>
  );
}
