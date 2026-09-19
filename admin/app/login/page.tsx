import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";
import { LoginForm } from "@/components/LoginForm";
import { LanguageToggle } from "@/components/layout/LanguageToggle";
import { LogoLockup } from "@/components/ui/LogoLockup";

export const metadata = {
  title: "Sign in",
};

function internalRedirectTarget(value: string | string[] | undefined): string {
  const requestedPath = Array.isArray(value) ? value[0] : value;
  if (!requestedPath || !requestedPath.startsWith("/") || requestedPath.startsWith("//")) {
    return "/dashboard";
  }
  return requestedPath;
}

export default async function LoginPage(props: PageProps<"/login">) {
  const [searchParams, dictionary] = await Promise.all([props.searchParams, getLocale().then(getDictionary)]);
  const redirectTo = internalRedirectTarget(searchParams.redirect);

  return (
    <main className="flex min-h-screen items-center justify-center bg-canvas px-6">
      <div className="w-full max-w-[380px]">
        <div className="mb-6 flex justify-end">
          <LanguageToggle />
        </div>
        <div className="mb-8 flex justify-center">
          <LogoLockup />
        </div>
        <h1 className="mb-8 text-center text-[26px] font-medium tracking-[-0.02em] text-ink">
          {dictionary.auth.adminConsoleTitle}
        </h1>

        <LoginForm redirectTo={redirectTo} />
      </div>
    </main>
  );
}
