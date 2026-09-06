import { formatMoney } from "@/lib/formatting/formatMoney";

const DISABLED_FIELD_CLASS_NAME =
  "h-12.5 rounded-xl border border-hairline-soft bg-surface-raised px-4 text-[13.5px] text-ink-soft cursor-not-allowed";

const FREE_DELIVERY_THRESHOLD_AMOUNT = 10_000_000;
const STANDARD_DELIVERY_FEE_AMOUNT = 300_000;
const NEXT_DAY_DELIVERY_FEE_AMOUNT = 300_000;
const LOW_STOCK_THRESHOLD_UNITS = 6;

function InfoIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="mt-0.5 h-3.5 w-3.5 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 11v5.5M12 8v.1" />
    </svg>
  );
}

export default function SettingsPage() {
  return (
    <div className="rounded-2xl border border-hairline-soft bg-white p-7.5">
      <div className="mb-6.5 text-[15px] font-semibold text-ink">Delivery & pricing rules</div>
      <div className="grid grid-cols-2 gap-4.5">
        <label className="flex flex-col gap-2">
          <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Free delivery over</span>
          <input
            disabled
            readOnly
            value={formatMoney(FREE_DELIVERY_THRESHOLD_AMOUNT)}
            className={DISABLED_FIELD_CLASS_NAME}
          />
        </label>
        <label className="flex flex-col gap-2">
          <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Standard delivery fee</span>
          <input
            disabled
            readOnly
            value={formatMoney(STANDARD_DELIVERY_FEE_AMOUNT)}
            className={DISABLED_FIELD_CLASS_NAME}
          />
        </label>
        <label className="flex flex-col gap-2">
          <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Next-day delivery fee</span>
          <input
            disabled
            readOnly
            value={formatMoney(NEXT_DAY_DELIVERY_FEE_AMOUNT)}
            className={DISABLED_FIELD_CLASS_NAME}
          />
        </label>
        <label className="flex flex-col gap-2">
          <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Low-stock threshold</span>
          <input
            disabled
            readOnly
            value={`${LOW_STOCK_THRESHOLD_UNITS} units on hand`}
            className={DISABLED_FIELD_CLASS_NAME}
          />
        </label>
      </div>
      <div className="mt-6.5 flex gap-3 rounded-xl bg-surface-raised px-5 py-4 text-[12px] leading-relaxed text-ink-soft">
        <InfoIcon />
        <span>
          Delivery fees and the free-delivery threshold are configured in PricingService; the low-stock threshold is
          configured in InventoryService. These are read-only here — changing them means changing code, not this
          form.
        </span>
      </div>
    </div>
  );
}
