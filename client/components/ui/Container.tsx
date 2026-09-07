import type { ReactNode } from "react";

type ContainerProps = {
  children: ReactNode;
  className?: string;
  alignToNavLabel?: boolean;
};

export function Container({ children, className, alignToNavLabel = false }: ContainerProps) {
  return (
    <div
      className={`mx-auto w-full max-w-content pr-6 ${alignToNavLabel ? "pl-12" : "pl-6"} ${className ?? ""}`}
    >
      {children}
    </div>
  );
}
