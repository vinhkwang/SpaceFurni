"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { useDictionary } from "@/lib/i18n/LocaleProvider";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";

type LoginFormProps = {
  redirectTo: string;
};

type SessionEnvelope = {
  success: boolean;
  error: { message: string } | null;
};

const socialButtonClassName =
  "flex h-[52px] cursor-not-allowed items-center justify-center gap-3 rounded-xl border border-hairline bg-white text-[12.5px] font-medium opacity-50";

export function LoginForm({ redirectTo }: LoginFormProps) {
  const router = useRouter();
  const dictionary = useDictionary();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isStayingSignedIn, setIsStayingSignedIn] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function submitCredentials(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      const response = await fetch("/api/session/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      const envelope = (await response.json()) as SessionEnvelope;

      if (!envelope.success) {
        setErrorMessage(envelope.error?.message ?? dictionary.auth.couldNotSignIn);
        setIsSubmitting(false);
        return;
      }

      router.replace(redirectTo);
      router.refresh();
    } catch {
      setErrorMessage(dictionary.auth.couldNotReachServer);
      setIsSubmitting(false);
    }
  }

  return (
    <form onSubmit={submitCredentials} className="flex flex-col gap-[18px]">
      <Input
        label={dictionary.auth.emailAddress}
        type="email"
        value={email}
        onChange={(event) => setEmail(event.target.value)}
        placeholder={dictionary.auth.emailPlaceholder}
        autoComplete="email"
        required
      />

      <div className="flex flex-col gap-2">
        <div className="flex items-center justify-between">
          <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
            {dictionary.auth.password}
          </span>
          <button
            type="button"
            onClick={() => setIsPasswordVisible(!isPasswordVisible)}
            className="cursor-pointer text-[10.5px] uppercase tracking-[0.1em] text-terracotta"
          >
            {isPasswordVisible ? dictionary.auth.hide : dictionary.auth.show}
          </button>
        </div>
        <Input
          type={isPasswordVisible ? "text" : "password"}
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder={dictionary.auth.passwordPlaceholder}
          aria-label={dictionary.auth.password}
          autoComplete="current-password"
          required
        />
      </div>

      {errorMessage ? (
        <p
          role="alert"
          className="flex items-center gap-2.5 rounded-xl bg-terracotta/10 px-4 py-3 text-[12.5px] text-terracotta"
        >
          <svg
            viewBox="0 0 24 24"
            aria-hidden
            className="h-3.5 w-3.5 shrink-0 stroke-current"
            fill="none"
            strokeWidth={2}
            strokeLinecap="round"
          >
            <path d="M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18z" />
            <path d="M12 8v5" />
            <path d="M12 16h.01" />
          </svg>
          {errorMessage}
        </p>
      ) : null}

      <div className="flex items-center justify-between">
        <label className="flex cursor-pointer items-center gap-2.5">
          <input
            type="checkbox"
            checked={isStayingSignedIn}
            onChange={(event) => setIsStayingSignedIn(event.target.checked)}
            className="peer sr-only"
          />
          <span className="flex h-[18px] w-[18px] items-center justify-center rounded-[5px] border border-hairline text-transparent peer-checked:border-deep peer-checked:bg-deep peer-checked:text-white">
            <svg
              viewBox="0 0 24 24"
              aria-hidden
              className="h-2.5 w-2.5 stroke-current"
              fill="none"
              strokeWidth={3.5}
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="m5 13 4 4L19 7" />
            </svg>
          </span>
          <span className="text-[12.5px] text-ink-soft">{dictionary.auth.keepMeSignedIn}</span>
        </label>
        <span className="text-[12.5px] text-terracotta">{dictionary.auth.forgotPassword}</span>
      </div>

      <Button type="submit" size="large" disabled={isSubmitting} className="mt-1.5 w-full">
        {isSubmitting ? dictionary.auth.signingIn : dictionary.auth.signIn}
      </Button>

      <div className="my-1.5 flex items-center gap-4">
        <span className="h-px flex-1 bg-hairline" />
        <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">{dictionary.auth.or}</span>
        <span className="h-px flex-1 bg-hairline" />
      </div>

      <div className="grid grid-cols-2 gap-3">
        <button type="button" disabled className={socialButtonClassName}>
          {dictionary.common.google}
        </button>
        <button type="button" disabled className={socialButtonClassName}>
          {dictionary.common.facebook}
        </button>
      </div>
    </form>
  );
}
