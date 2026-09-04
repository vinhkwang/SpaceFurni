"use client";

import { useEffect, useState, useTransition } from "react";
import type { CartResponse } from "@/lib/api/types";
import { applyPromotionCodeAction, clearPromotionCodeAction } from "@/lib/cart/cartActions";

type PromotionCodeInputProps = {
  cart: CartResponse;
};

type FeedbackMessage = {
  tone: "success" | "error";
  text: string;
};

const FEEDBACK_DISPLAY_DURATION_MS = 3200;

export function PromotionCodeInput({ cart }: PromotionCodeInputProps) {
  const [code, setCode] = useState("");
  const [isSubmitting, startSubmission] = useTransition();
  const [feedback, setFeedback] = useState<FeedbackMessage | null>(null);
  const appliedPromotionCode = cart.priceBreakdown.appliedPromotionCode;

  useEffect(() => {
    if (feedback === null) {
      return;
    }
    const dismissTimer = setTimeout(() => setFeedback(null), FEEDBACK_DISPLAY_DURATION_MS);
    return () => clearTimeout(dismissTimer);
  }, [feedback]);

  function applyCode(): void {
    const trimmedCode = code.trim();
    if (trimmedCode === "") {
      return;
    }
    startSubmission(async () => {
      const result = await applyPromotionCodeAction(trimmedCode);
      if (!result.success) {
        setFeedback({ tone: "error", text: result.errorMessage });
        return;
      }
      if (result.cart.priceBreakdown.appliedPromotionCode === null) {
        setFeedback({ tone: "error", text: "That code isn't valid" });
        return;
      }
      setCode("");
      setFeedback({
        tone: "success",
        text: `Promo applied — ${result.cart.priceBreakdown.appliedPromotionCode}`,
      });
    });
  }

  function clearCode(): void {
    startSubmission(async () => {
      const result = await clearPromotionCodeAction();
      if (!result.success) {
        setFeedback({ tone: "error", text: result.errorMessage });
        return;
      }
      setFeedback(null);
    });
  }

  return (
    <div className="flex flex-col gap-2.5">
      {appliedPromotionCode === null ? (
        <div className="flex gap-2">
          <input
            value={code}
            onChange={(event) => setCode(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === "Enter") {
                event.preventDefault();
                applyCode();
              }
            }}
            placeholder="Promo code"
            disabled={isSubmitting}
            className="h-11.5 flex-1 rounded-pill border border-hairline bg-surface-warm px-4 text-[12.5px] outline-none transition-colors duration-200 placeholder:text-ink-muted focus:border-terracotta disabled:cursor-not-allowed disabled:opacity-60"
          />
          <button
            type="button"
            onClick={applyCode}
            disabled={isSubmitting || code.trim() === ""}
            className="h-11.5 cursor-pointer rounded-pill border border-hairline px-5 text-[10.5px] font-semibold uppercase tracking-[0.12em] transition-colors duration-250 hover:border-deep hover:bg-deep hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
          >
            Apply
          </button>
        </div>
      ) : (
        <div className="flex items-center justify-between rounded-pill border border-hairline bg-surface-warm px-4 py-2.5">
          <span className="text-[12.5px] font-semibold uppercase tracking-[0.1em] text-terracotta">
            {appliedPromotionCode}
          </span>
          <button
            type="button"
            onClick={clearCode}
            disabled={isSubmitting}
            className="cursor-pointer text-[11px] uppercase tracking-[0.1em] text-ink-muted underline-offset-2 transition-colors duration-200 hover:text-ink hover:underline disabled:cursor-not-allowed disabled:opacity-50"
          >
            Remove
          </button>
        </div>
      )}

      {feedback === null ? null : (
        <p
          role="status"
          className={`text-[11.5px] ${feedback.tone === "success" ? "text-success" : "text-terracotta"}`}
        >
          {feedback.text}
        </p>
      )}
    </div>
  );
}
