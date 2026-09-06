"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

type LoginFormProps = {
  redirectTo: string;
};

type SessionEnvelope = {
  success: boolean;
  error: { message: string } | null;
};

const inputClassName =
  "h-12 w-full rounded-card border border-hairline bg-white px-4 text-[13.5px] text-ink placeholder:text-ink-muted focus:border-deep focus:outline-none";

export function LoginForm({ redirectTo }: LoginFormProps) {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
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
        setErrorMessage(envelope.error?.message ?? "We could not sign you in. Try again.");
        setIsSubmitting(false);
        return;
      }

      router.replace(redirectTo);
      router.refresh();
    } catch {
      setErrorMessage("We could not reach the server. Try again.");
      setIsSubmitting(false);
    }
  }

  return (
    <form onSubmit={submitCredentials} className="flex flex-col gap-4 rounded-card border border-hairline bg-white p-8 shadow-sm">
      <label className="flex flex-col gap-2">
        <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
          Email address
        </span>
        <input
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="you@spacefurni.dev"
          autoComplete="email"
          required
          className={inputClassName}
        />
      </label>

      <label className="flex flex-col gap-2">
        <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
          Password
        </span>
        <input
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="••••••••"
          autoComplete="current-password"
          required
          className={inputClassName}
        />
      </label>

      {errorMessage ? (
        <p role="alert" className="rounded-card bg-terracotta/10 px-4 py-3 text-[12.5px] text-terracotta">
          {errorMessage}
        </p>
      ) : null}

      <button
        type="submit"
        disabled={isSubmitting}
        className="mt-1.5 flex h-12 w-full items-center justify-center rounded-pill bg-deep text-[12.5px] font-semibold uppercase tracking-[0.1em] text-white transition-opacity disabled:opacity-60"
      >
        {isSubmitting ? "Signing in" : "Sign in"}
      </button>
    </form>
  );
}
