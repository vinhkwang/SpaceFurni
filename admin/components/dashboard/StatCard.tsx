import type { ReactNode } from "react";

type StatCardProps = {
  label: string;
  value: number;
  icon: ReactNode;
};

export function StatCard({ label, value, icon }: StatCardProps) {
  return (
    <div className="rounded-2xl border border-hairline-soft bg-white p-5.5">
      <div className="mb-4.5 flex items-center justify-between">
        <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">{label}</span>
        <span className="flex h-8.5 w-8.5 items-center justify-center rounded-xl bg-surface-raised text-ink">
          {icon}
        </span>
      </div>
      <div className="text-[26px] font-semibold tracking-tight text-ink">{value.toLocaleString("en-US")}</div>
    </div>
  );
}
