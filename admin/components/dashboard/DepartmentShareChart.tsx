import type { DepartmentRevenueShareResponse } from "@/lib/api/types";
import type { Dictionary } from "@/lib/i18n/getDictionary";

type DepartmentShareChartProps = {
  shares: DepartmentRevenueShareResponse[];
  dictionary: Dictionary;
};

const DEPARTMENT_COLOR_CLASS_NAMES: Record<string, string> = {
  "Living room": "bg-deep",
  Kitchen: "bg-terracotta",
  Bedroom: "bg-brass",
  "Work & study": "bg-success",
  Others: "bg-sand",
};

const FALLBACK_DEPARTMENT_COLOR_CLASS_NAME = "bg-ink-muted";

function departmentColorClassName(departmentName: string): string {
  return DEPARTMENT_COLOR_CLASS_NAMES[departmentName] ?? FALLBACK_DEPARTMENT_COLOR_CLASS_NAME;
}

export function DepartmentShareChart({ shares, dictionary }: DepartmentShareChartProps) {
  return (
    <div className="flex flex-col rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div className="mb-6.5">
        <div className="text-[15px] font-semibold text-ink">{dictionary.dashboard.topDepartments}</div>
        <div className="mt-1 text-[11.5px] text-ink-muted">{dictionary.dashboard.shareOfRevenue}</div>
      </div>

      {shares.length === 0 ? (
        <div className="flex flex-1 items-center justify-center text-[12.5px] text-ink-muted">
          {dictionary.dashboard.noOrdersYet}
        </div>
      ) : (
        <>
          <div className="flex h-3 gap-0.5 overflow-hidden rounded-pill">
            {shares.map((share) => (
              <div
                key={share.departmentName}
                style={{ width: `${share.percentageShare}%` }}
                className={`h-full ${departmentColorClassName(share.departmentName)}`}
              />
            ))}
          </div>
          <ul className="mt-5.5 flex flex-col gap-3.5">
            {shares.map((share) => (
              <li key={share.departmentName} className="flex items-center justify-between gap-3 text-[12.5px]">
                <span className="flex items-center gap-2.5 text-ink">
                  <span className={`h-2.5 w-2.5 shrink-0 rounded-full ${departmentColorClassName(share.departmentName)}`} />
                  {share.departmentName}
                </span>
                <span className="font-semibold text-ink">{share.percentageShare.toFixed(1)}%</span>
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
}
