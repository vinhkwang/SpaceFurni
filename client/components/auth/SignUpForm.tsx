"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";

type SignUpFormProps = {
  redirectTo: string;
};

type SessionEnvelope = {
  success: boolean;
  error: { message: string } | null;
};

const MINIMUM_PASSWORD_LENGTH = 8;

const socialButtonClassName =
  "flex h-[52px] cursor-not-allowed items-center justify-center gap-3 rounded-xl border border-hairline bg-white text-[12.5px] font-medium opacity-50";

export function SignUpForm({ redirectTo }: SignUpFormProps) {
  const router = useRouter();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function submitRegistration(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      const response = await fetch("/api/session/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fullName, email, password }),
      });
      const envelope = (await response.json()) as SessionEnvelope;

      if (!envelope.success) {
        setErrorMessage(envelope.error?.message ?? "We could not create your account. Try again.");
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
    <form onSubmit={submitRegistration} className="flex flex-col gap-[18px]">
      <Input
        label="Full name"
        value={fullName}
        onChange={(event) => setFullName(event.target.value)}
        placeholder="Nguyen Minh"
        autoComplete="name"
        required
      />

      <Input
        label="Email address"
        type="email"
        value={email}
        onChange={(event) => setEmail(event.target.value)}
        placeholder="you@email.com"
        autoComplete="email"
        required
      />

      <div className="flex flex-col gap-2">
        <div className="flex items-center justify-between">
          <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
            Password
          </span>
          <button
            type="button"
            onClick={() => setIsPasswordVisible(!isPasswordVisible)}
            className="cursor-pointer text-[10.5px] uppercase tracking-[0.1em] text-terracotta"
          >
            {isPasswordVisible ? "Hide" : "Show"}
          </button>
        </div>
        <Input
          type={isPasswordVisible ? "text" : "password"}
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="At least 8 characters"
          aria-label="Password"
          autoComplete="new-password"
          minLength={MINIMUM_PASSWORD_LENGTH}
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

      <Button type="submit" size="large" disabled={isSubmitting} className="mt-1.5 w-full">
        {isSubmitting ? "Creating account" : "Create account"}
      </Button>

      <div className="my-1.5 flex items-center gap-4">
        <span className="h-px flex-1 bg-hairline" />
        <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">or</span>
        <span className="h-px flex-1 bg-hairline" />
      </div>

      <div className="grid grid-cols-2 gap-3">
        <button type="button" disabled className={socialButtonClassName}>
          Google
        </button>
        <button type="button" disabled className={socialButtonClassName}>
          Facebook
        </button>
      </div>
    </form>
  );
}
