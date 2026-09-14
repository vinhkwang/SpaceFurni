"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import type { PlatformSettingsResponse } from "@/lib/api/types";
import { updateSettingsAction } from "@/lib/settings/settingsActions";
import { useToast } from "@/components/ui/Toast";

type SettingsFormProps = {
  settings: PlatformSettingsResponse;
};

const FIELD_CLASS_NAME =
  "h-12.5 rounded-xl border border-hairline bg-canvas px-4 text-[13.5px] text-ink outline-none transition-colors duration-200 focus:border-deep";

function InfoIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="mt-0.5 h-3.5 w-3.5 shrink-0 stroke-current" fill="none" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 11v5.5M12 8v.1" />
    </svg>
  );
}

export function SettingsForm({ settings }: SettingsFormProps) {
  const router = useRouter();
  const { showToast } = useToast();

  const [freeDeliveryThresholdAmount, setFreeDeliveryThresholdAmount] = useState(
    String(settings.freeDeliveryThresholdAmount),
  );
  const [standardDeliveryFeeAmount, setStandardDeliveryFeeAmount] = useState(
    String(settings.standardDeliveryFeeAmount),
  );
  const [nextDayDeliveryFeeAmount, setNextDayDeliveryFeeAmount] = useState(
    String(settings.nextDayDeliveryFeeAmount),
  );
  const [lowStockThresholdUnits, setLowStockThresholdUnits] = useState(String(settings.lowStockThresholdUnits));
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  async function submitSettings(submitEvent: FormEvent<HTMLFormElement>) {
    submitEvent.preventDefault();
    setIsSubmitting(true);
    setErrorMessage("");

    const result = await updateSettingsAction({
      freeDeliveryThresholdAmount: Number(freeDeliveryThresholdAmount),
      standardDeliveryFeeAmount: Number(standardDeliveryFeeAmount),
      nextDayDeliveryFeeAmount: Number(nextDayDeliveryFeeAmount),
      lowStockThresholdUnits: Number(lowStockThresholdUnits),
    });

    setIsSubmitting(false);
    if (result.success) {
      showToast("Settings saved.");
      router.refresh();
      return;
    }
    setErrorMessage(result.errorMessage);
  }

  return (
    <form onSubmit={submitSettings} className="rounded-2xl border border-hairline-soft bg-white p-7.5">
      <div className="mb-6.5 flex items-center justify-between">
        <div className="text-[15px] font-semibold text-ink">Delivery & pricing rules</div>
        <button
          type="submit"
          disabled={isSubmitting}
          className="flex h-11 items-center rounded-pill bg-deep px-6 text-[11px] font-semibold uppercase tracking-[0.13em] text-white transition-colors duration-200 hover:bg-terracotta disabled:cursor-not-allowed disabled:opacity-60"
        >
          Save changes
        </button>
      </div>

      {errorMessage ? (
        <p role="alert" className="mb-6 rounded-xl bg-terracotta/10 px-4.5 py-3.5 text-[12.5px] text-terracotta">
          {errorMessage}
        </p>
      ) : null}

      <div className="grid grid-cols-2 gap-4.5">
        <label className="flex flex-col gap-2">
          <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Free delivery over</span>
          <input
            required
            type="number"
            min={0}
            value={freeDeliveryThresholdAmount}
            onChange={(changeEvent) => setFreeDeliveryThresholdAmount(changeEvent.target.value)}
            className={FIELD_CLASS_NAME}
          />
        </label>
        <label className="flex flex-col gap-2">
          <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Standard delivery fee</span>
          <input
            required
            type="number"
            min={0}
            value={standardDeliveryFeeAmount}
            onChange={(changeEvent) => setStandardDeliveryFeeAmount(changeEvent.target.value)}
            className={FIELD_CLASS_NAME}
          />
        </label>
        <label className="flex flex-col gap-2">
          <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Next-day delivery fee</span>
          <input
            required
            type="number"
            min={0}
            value={nextDayDeliveryFeeAmount}
            onChange={(changeEvent) => setNextDayDeliveryFeeAmount(changeEvent.target.value)}
            className={FIELD_CLASS_NAME}
          />
        </label>
        <label className="flex flex-col gap-2">
          <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">Low-stock threshold</span>
          <input
            required
            type="number"
            min={1}
            value={lowStockThresholdUnits}
            onChange={(changeEvent) => setLowStockThresholdUnits(changeEvent.target.value)}
            className={FIELD_CLASS_NAME}
          />
        </label>
      </div>

      <div className="mt-6.5 flex gap-3 rounded-xl bg-surface-raised px-5 py-4 text-[12px] leading-relaxed text-ink-soft">
        <InfoIcon />
        <span>
          Amounts are in VND đồng. Changes apply immediately across pricing, delivery and inventory — no deploy
          required.
        </span>
      </div>
    </form>
  );
}
