import { LoginForm } from "@/components/LoginForm";

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
        <p className="mb-2.5 text-center text-[11px] font-semibold uppercase tracking-[0.14em] text-ink-muted">
          SpaceFurni
        </p>
        <h1 className="mb-8 text-center text-[26px] font-medium tracking-[-0.02em] text-ink">
          Admin console
        </h1>

        <LoginForm redirectTo={redirectTo} />
      </div>
    </main>
  );
}
