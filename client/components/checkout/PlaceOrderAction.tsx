"use client";

import { useState, useTransition } from "react";
import Link from "next/link";
import { OrderConfirmation } from "@/components/checkout/OrderConfirmation";
import {
  placeOrderAction,
  type PlaceOrderDeliveryDetails,
  type PlaceOrderInput,
} from "@/lib/checkout/checkoutActions";
import type { DeliveryWindow, OrderResponse, PaymentMethod } from "@/lib/api/types";

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

export function PlaceOrderAction() {
  const [idempotencyKey] = useState<string>(() => crypto.randomUUID());
  const [isSubmitting, startSubmission] = useTransition();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [placedOrder, setPlacedOrder] = useState<OrderResponse | null>(null);

  if (placedOrder !== null) {
    return <OrderConfirmation order={placedOrder} />;
  }

  const deliveryDraft = readStoredDeliveryDraft();

  function submitOrder(): void {
    if (deliveryDraft === null) {
      setErrorMessage("Add your delivery details before placing the order.");
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
        setErrorMessage(result.errorMessage);
        return;
      }
      clearCheckoutDrafts();
      setErrorMessage(null);
      setPlacedOrder(result.order);
    });
  }

  return (
    <div className="flex flex-col gap-3">
      {errorMessage === null ? null : (
        <p role="alert" className="text-[12.5px] text-terracotta">
          {errorMessage}
        </p>
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
