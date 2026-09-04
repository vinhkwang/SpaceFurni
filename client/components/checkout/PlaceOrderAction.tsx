"use client";

import { useState, useTransition } from "react";
import Link from "next/link";
import { CheckoutErrorBanner } from "@/components/checkout/CheckoutErrorBanner";
import { OrderConfirmation } from "@/components/checkout/OrderConfirmation";
import {
  placeOrderAction,
  type PlaceOrderDeliveryDetails,
  type PlaceOrderInput,
} from "@/lib/checkout/checkoutActions";
import type { CartResponse, DeliveryWindow, OrderResponse, PaymentMethod } from "@/lib/api/types";

type PlaceOrderActionProps = {
  cart?: CartResponse;
};

type CheckoutErrorState = {
  code: string;
  message: string;
  details: Record<string, string> | null;
};

const DELIVERY_DETAILS_STORAGE_KEY = "spacefurni:checkout:deliveryDetails";
const PAYMENT_METHOD_STORAGE_KEY = "spacefurni:checkout:paymentMethod";

type StoredDeliveryDraft = PlaceOrderDeliveryDetails & { deliveryWindow: DeliveryWindow };

function readStoredDeliveryDraft(): StoredDeliveryDraft | null {
  try {
    const storedJson = sessionStorage.getItem(DELIVERY_DETAILS_STORAGE_KEY);
    if (storedJson === null) {
      return null;
    }
    const parsedDraft = JSON.parse(storedJson) as Partial<StoredDeliveryDraft>;
    if (
      typeof parsedDraft.fullName !== "string" ||
      typeof parsedDraft.phone !== "string" ||
      typeof parsedDraft.street !== "string" ||
      typeof parsedDraft.district !== "string" ||
      typeof parsedDraft.city !== "string" ||
      (parsedDraft.deliveryWindow !== "STANDARD" && parsedDraft.deliveryWindow !== "NEXT_DAY")
    ) {
      return null;
    }
    return {
      fullName: parsedDraft.fullName,
      phone: parsedDraft.phone,
      street: parsedDraft.street,
      district: parsedDraft.district,
      city: parsedDraft.city,
      note: typeof parsedDraft.note === "string" && parsedDraft.note.trim() !== "" ? parsedDraft.note : null,
      deliveryWindow: parsedDraft.deliveryWindow,
    };
  } catch {
    return null;
  }
}

function readStoredPaymentMethod(): PaymentMethod {
  try {
    const storedValue = sessionStorage.getItem(PAYMENT_METHOD_STORAGE_KEY);
    if (storedValue === "CARD" || storedValue === "CASH_ON_DELIVERY" || storedValue === "BANK_TRANSFER") {
      return storedValue;
    }
  } catch {}
  return "CARD";
}

function clearCheckoutDrafts(): void {
  try {
    sessionStorage.removeItem(DELIVERY_DETAILS_STORAGE_KEY);
    sessionStorage.removeItem(PAYMENT_METHOD_STORAGE_KEY);
  } catch {}
}

export function PlaceOrderAction({ cart }: PlaceOrderActionProps) {
  const [idempotencyKey] = useState<string>(() => crypto.randomUUID());
  const [isSubmitting, startSubmission] = useTransition();
  const [checkoutError, setCheckoutError] = useState<CheckoutErrorState | null>(null);
  const [placedOrder, setPlacedOrder] = useState<OrderResponse | null>(null);

  if (placedOrder !== null) {
    return <OrderConfirmation order={placedOrder} />;
  }

  const deliveryDraft = readStoredDeliveryDraft();

  function submitOrder(): void {
    if (deliveryDraft === null) {
      setCheckoutError({
        code: "MISSING_DELIVERY_DETAILS",
        message: "Add your delivery details before placing the order.",
        details: null,
      });
      return;
    }

    const deliveryDetails: PlaceOrderDeliveryDetails = {
      fullName: deliveryDraft.fullName,
      phone: deliveryDraft.phone,
      street: deliveryDraft.street,
      district: deliveryDraft.district,
      city: deliveryDraft.city,
      note: deliveryDraft.note,
    };
    const input: PlaceOrderInput = {
      deliveryDetails,
      deliveryWindow: deliveryDraft.deliveryWindow,
      paymentMethod: readStoredPaymentMethod(),
    };

    startSubmission(async () => {
      const result = await placeOrderAction(idempotencyKey, input);
      if (!result.success) {
        setCheckoutError({ code: result.errorCode, message: result.errorMessage, details: result.errorDetails });
        return;
      }
      clearCheckoutDrafts();
      setCheckoutError(null);
      setPlacedOrder(result.order);
    });
  }

  return (
    <div className="flex flex-col gap-3">
      {checkoutError === null ? null : (
        <CheckoutErrorBanner
          code={checkoutError.code}
          message={checkoutError.message}
          details={checkoutError.details}
          cart={cart}
        />
      )}
      {deliveryDraft === null ? (
        <Link
          href="/checkout?step=delivery"
          className="text-[11px] font-semibold uppercase tracking-[0.13em] text-terracotta underline-offset-2 hover:underline"
        >
          Add delivery details first
        </Link>
      ) : null}
      <button
        type="button"
        onClick={submitOrder}
        disabled={isSubmitting || deliveryDraft === null}
        className="flex h-[54px] w-full cursor-pointer items-center justify-center gap-3 rounded-pill bg-deep text-[11.5px] font-semibold uppercase tracking-[0.14em] text-white transition-colors duration-300 hover:bg-terracotta disabled:cursor-not-allowed disabled:opacity-50"
      >
        {isSubmitting ? "Placing order…" : "Place order"}
      </button>
    </div>
  );
}
