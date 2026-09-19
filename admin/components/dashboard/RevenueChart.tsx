import { formatMoney } from "@/lib/formatting/formatMoney";
import type { Dictionary } from "@/lib/i18n/getDictionary";
import type { Locale } from "@/lib/i18n/localeConstants";
import type { MonthlyRevenuePointResponse } from "@/lib/api/types";

type RevenueChartProps = {
  points: MonthlyRevenuePointResponse[];
  dictionary: Dictionary;
  locale: Locale;
};

const CHART_WIDTH = 640;
const CHART_HEIGHT = 200;
const CHART_PADDING_X = 20;
const CHART_PADDING_TOP = 16;
const CHART_PADDING_BOTTOM = 26;
const BASELINE_Y = CHART_HEIGHT - CHART_PADDING_BOTTOM;

function monthLabelFormatter(locale: Locale): Intl.DateTimeFormat {
  return new Intl.DateTimeFormat(locale === "vi" ? "vi-VN" : "en-US", { month: "short", timeZone: "UTC" });
}

type PlottedPoint = {
  x: number;
  y: number;
  point: MonthlyRevenuePointResponse;
};

function plotPoints(points: MonthlyRevenuePointResponse[]): PlottedPoint[] {
  const maxRevenueAmount = Math.max(...points.map((point) => point.revenueAmount), 1);
  const plotWidth = CHART_WIDTH - CHART_PADDING_X * 2;
  const plotHeight = BASELINE_Y - CHART_PADDING_TOP;
  const stepX = points.length > 1 ? plotWidth / (points.length - 1) : 0;

  return points.map((point, index) => ({
    x: CHART_PADDING_X + stepX * index,
    y: CHART_PADDING_TOP + plotHeight * (1 - point.revenueAmount / maxRevenueAmount),
    point,
  }));
}

function EmptyPanel({ dictionary }: { dictionary: Dictionary }) {
  return (
    <div className="flex flex-1 items-center justify-center text-[12.5px] text-ink-muted">
      {dictionary.dashboard.noOrdersYet}
    </div>
  );
}

export function RevenueChart({ points, dictionary, locale }: RevenueChartProps) {
  const plotted = plotPoints(points);
  const monthFormatter = monthLabelFormatter(locale);
  const linePath = plotted.map(({ x, y }, index) => `${index === 0 ? "M" : "L"}${x},${y}`).join(" ");
  const areaPath =
    plotted.length > 0
      ? `${linePath} L${plotted[plotted.length - 1].x},${BASELINE_Y} L${plotted[0].x},${BASELINE_Y} Z`
      : "";
  const latestPoint = plotted[plotted.length - 1];

  return (
    <div className="flex flex-col rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div className="mb-2 flex items-start justify-between">
        <div>
          <div className="text-[15px] font-semibold text-ink">{dictionary.dashboard.revenue}</div>
          <div className="mt-1 text-[11.5px] text-ink-muted">{dictionary.dashboard.last12Months}</div>
        </div>
        {latestPoint ? (
          <div className="text-right">
            <div className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">{dictionary.dashboard.thisMonth}</div>
            <div className="text-[18px] font-semibold text-ink">{formatMoney(latestPoint.point.revenueAmount)}</div>
          </div>
        ) : null}
      </div>

      {plotted.length === 0 ? (
        <EmptyPanel dictionary={dictionary} />
      ) : (
        <svg
          viewBox={`0 0 ${CHART_WIDTH} ${CHART_HEIGHT}`}
          className="w-full"
          role="img"
          aria-label={dictionary.dashboard.revenueChartAriaLabel}
        >
          <line x1={CHART_PADDING_X} y1={BASELINE_Y} x2={CHART_WIDTH - CHART_PADDING_X} y2={BASELINE_Y} className="stroke-hairline" strokeWidth={1} />
          <path d={areaPath} className="fill-terracotta/10" />
          <path d={linePath} className="fill-none stroke-terracotta" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" />
          {plotted.map(({ x, y, point }) => (
            <circle key={point.month} cx={x} cy={y} r={3} className="fill-terracotta" />
          ))}
          {plotted.map(({ x, point }) => (
            <text key={point.month} x={x} y={CHART_HEIGHT - 8} textAnchor="middle" className="fill-ink-muted text-[9px]">
              {monthFormatter.format(new Date(`${point.month}T00:00:00Z`))}
            </text>
          ))}
        </svg>
      )}
    </div>
  );
}
