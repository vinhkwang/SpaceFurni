"use client";

import { useState, type ReactNode } from "react";
import { Input } from "@/components/ui/Input";
import type { PaymentMethod } from "@/lib/api/types";

type CardDraftValues = {
  cardNumber: string;
  expiry: string;
  cvc: string;
};

const STORAGE_KEY = "spacefurni:checkout:paymentMethod";

const EMPTY_CARD_DRAFT_VALUES: CardDraftValues = {
  cardNumber: "",
  expiry: "",
  cvc: "",
};

function loadStoredPaymentMethod(): PaymentMethod {
  try {
    const storedValue = sessionStorage.getItem(STORAGE_KEY);
    if (storedValue === "CARD" || storedValue === "CASH_ON_DELIVERY" || storedValue === "BANK_TRANSFER") {
      return storedValue;
    }
  } catch {}
  return "CARD";
}

const cardIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 stroke-current" fill="none" strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
    <rect x="2.5" y="5.5" width="19" height="13" rx="2.2" />
    <path d="M2.5 9.5h19" />
  </svg>
);

const cashIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 stroke-current" fill="none" strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
    <rect x="2.5" y="6" width="19" height="12" rx="2" />
    <circle cx="12" cy="12" r="2.6" />
  </svg>
);

const bankIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-4 w-4 stroke-current" fill="none" strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
    <path d="M3 10.5 12 4l9 6.5" />
    <path d="M4.5 10.5v8M9.5 10.5v8M14.5 10.5v8M19.5 10.5v8" />
    <path d="M3 20.5h18" />
  </svg>
);

const infoIcon = (
  <svg viewBox="0 0 24 24" aria-hidden className="h-3.5 w-3.5 shrink-0 stroke-current" fill="none" strokeWidth={2} strokeLinecap="round">
    <circle cx="12" cy="12" r="9" />
    <path d="M12 11v5" />
    <path d="M12 8h.01" />
  </svg>
);

type PaymentMethodOption = {
  value: PaymentMethod;
  label: string;
  icon: ReactNode;
  hint?: string;
};

const PAYMENT_METHOD_OPTIONS: PaymentMethodOption[] = [
  { value: "CARD", label: "Card", icon: cardIcon, hint: "Visa · Mastercard · JCB" },
  { value: "CASH_ON_DELIVERY", label: "Cash on delivery", icon: cashIcon },
  { value: "BANK_TRANSFER", label: "Bank transfer", icon: bankIcon },
];

export function PaymentMethodSelector() {
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>(() => loadStoredPaymentMethod());
  const [cardDraftValues, setCardDraftValues] = useState<CardDraftValues>(EMPTY_CARD_DRAFT_VALUES);

  function selectPaymentMethod(nextPaymentMethod: PaymentMethod): void {
    setPaymentMethod(nextPaymentMethod);
    try {
      sessionStorage.setItem(STORAGE_KEY, nextPaymentMethod);
    } catch {}
  }

  function updateCardDraftField<FieldName extends keyof CardDraftValues>(
    fieldName: FieldName,
    fieldValue: CardDraftValues[FieldName],
  ): void {
    setCardDraftValues((previousCardDraftValues) => ({ ...previousCardDraftValues, [fieldName]: fieldValue }));
  }

  return (
    <div className="rounded-2xl border border-hairline bg-white px-8.5 py-8">
      <h2 className="mb-6 text-[19px] font-medium">Payment</h2>

      <div className="mb-6.5 flex flex-col gap-3">
        {PAYMENT_METHOD_OPTIONS.map((option) => {
          const isSelected = paymentMethod === option.value;
          return (
            <label
              key={option.value}
              className={`flex cursor-pointer items-center gap-3.5 rounded-xl border px-5 py-4.5 transition-colors duration-200 ${
                isSelected ? "border-deep bg-surface" : "border-hairline hover:border-deep/40"
              }`}
            >
              <input
                type="radio"
                name="paymentMethod"
                value={option.value}
                checked={isSelected}
                onChange={() => selectPaymentMethod(option.value)}
                className="sr-only"
              />
              <span
                className={`h-4 w-4 shrink-0 rounded-full border ${
                  isSelected ? "border-[5px] border-deep" : "border-hairline"
                }`}
              />
              <span className="text-ink-soft">{option.icon}</span>
              <span className="text-[13px] font-semibold">{option.label}</span>
              {option.hint ? (
                <span className="ml-auto text-[11.5px] text-ink-muted">{option.hint}</span>
              ) : null}
            </label>
          );
        })}
      </div>

      {paymentMethod === "CARD" ? (
        <div className="flex flex-col gap-4">
          <div className="flex items-center gap-2.5 rounded-xl bg-surface-warm px-4.5 py-3.5 text-[12px] text-ink-soft">
            {infoIcon}
            This is a simulated payment for this demo — no card network is contacted and no charge is made.
          </div>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-[1fr_130px_110px]">
            <Input
              label="Card number"
              placeholder="4242 4242 4242 4242"
              value={cardDraftValues.cardNumber}
              onChange={(event) => updateCardDraftField("cardNumber", event.target.value)}
              autoComplete="cc-number"
              inputMode="numeric"
            />
            <Input
              label="Expiry"
              placeholder="09 / 28"
              value={cardDraftValues.expiry}
              onChange={(event) => updateCardDraftField("expiry", event.target.value)}
              autoComplete="cc-exp"
              inputMode="numeric"
            />
            <Input
              label="CVC"
              placeholder="123"
              value={cardDraftValues.cvc}
              onChange={(event) => updateCardDraftField("cvc", event.target.value)}
              autoComplete="cc-csc"
              inputMode="numeric"
            />
          </div>
        </div>
      ) : null}
    </div>
  );
}
