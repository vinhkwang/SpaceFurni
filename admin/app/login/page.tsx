import { LoginForm } from "@/components/LoginForm";
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
  const searchParams = await props.searchParams;
  const redirectTo = internalRedirectTarget(searchParams.redirect);

  return (
    <main className="flex min-h-screen items-center justify-center bg-canvas px-6">
      <div className="w-full max-w-[380px]">
        <div className="mb-8 flex justify-center">
          <LogoLockup />
        </div>
        <h1 className="mb-8 text-center text-[26px] font-medium tracking-[-0.02em] text-ink">
          Admin console
        </h1>

        <LoginForm redirectTo={redirectTo} />
      </div>
    </main>
  );
}
