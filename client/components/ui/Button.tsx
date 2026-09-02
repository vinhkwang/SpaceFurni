import type { ComponentPropsWithoutRef } from "react";

type ButtonVariant = "primary" | "secondary" | "ghost";

type ButtonSize = "small" | "medium" | "large";

type ButtonProps = ComponentPropsWithoutRef<"button"> & {
  variant?: ButtonVariant;
  size?: ButtonSize;
};

const sharedClassName =
  "inline-flex cursor-pointer items-center justify-center gap-3 rounded-pill font-semibold uppercase tracking-[0.14em] transition-colors duration-300 disabled:cursor-not-allowed disabled:opacity-50";

const variantClassNames: Record<ButtonVariant, string> = {
  primary: "bg-terracotta text-white hover:bg-deep",
  secondary:
    "border border-hairline text-ink hover:border-deep hover:bg-deep hover:text-white",
  ghost: "text-ink hover:bg-surface",
};

const sizeClassNames: Record<ButtonSize, string> = {
  small: "h-[38px] px-[18px] text-[10.5px]",
  medium: "h-12 px-7 text-[11.5px]",
  large: "h-[54px] px-8 text-[11.5px]",
};

export function Button({
  variant = "primary",
  size = "medium",
  type = "button",
  className,
  ...nativeButtonProps
}: ButtonProps) {
  return (
    <button
      type={type}
      className={`${sharedClassName} ${variantClassNames[variant]} ${sizeClassNames[size]} ${className ?? ""}`}
      {...nativeButtonProps}
    />
  );
}
