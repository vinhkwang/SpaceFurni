import type { ComponentPropsWithoutRef } from "react";

type InputProps = ComponentPropsWithoutRef<"input"> & {
  label?: string;
  errorMessage?: string;
};

const fieldClassName =
  "h-[50px] rounded-xl border border-hairline bg-canvas px-4 text-[13px] text-ink outline-none transition-colors duration-200 placeholder:text-ink-muted focus:border-terracotta disabled:cursor-not-allowed disabled:opacity-50";

export function Input({
  label,
  errorMessage,
  className,
  ...nativeInputProps
}: InputProps) {
  return (
    <label className="flex flex-col gap-2">
      {label ? (
        <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
          {label}
        </span>
      ) : null}
      <input
        aria-invalid={errorMessage ? true : undefined}
        className={`${fieldClassName} ${className ?? ""}`}
        {...nativeInputProps}
      />
      {errorMessage ? (
        <span className="text-[11px] tracking-[0.02em] text-terracotta">
          {errorMessage}
        </span>
      ) : null}
    </label>
  );
}
