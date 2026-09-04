"use client";

import { useOptimistic, useState, useTransition } from "react";
import { ApiError } from "@/lib/api/ApiError";
import type { CartResponse } from "@/lib/api/types";
import { addCartLineAction, removeCartLineAction, updateCartLineQuantityAction } from "@/lib/cart/cartActions";

type CartOptimisticUpdate =
  | { type: "updateQuantity"; productId: string; quantity: number }
  | { type: "removeLine"; productId: string };

function applyOptimisticCartUpdate(cart: CartResponse, update: CartOptimisticUpdate): CartResponse {
  if (update.type === "removeLine") {
    return { ...cart, lines: cart.lines.filter((line) => line.productId !== update.productId) };
  }
  return {
    ...cart,
    lines: cart.lines.map((line) =>
      line.productId === update.productId ? { ...line, quantity: update.quantity } : line,
    ),
  };
}

function mutationFailureMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  return "We could not update your cart. Try again.";
}

export function useCart(cart: CartResponse) {
  const [optimisticCart, applyOptimisticUpdate] = useOptimistic(cart, applyOptimisticCartUpdate);
  const [isMutating, startMutation] = useTransition();
  const [mutationError, setMutationError] = useState<string | null>(null);

  function addLine(productId: string, quantity: number): void {
    setMutationError(null);
    startMutation(async () => {
      try {
        await addCartLineAction(productId, quantity);
      } catch (error) {
        setMutationError(mutationFailureMessage(error));
      }
    });
  }

  function updateQuantity(productId: string, quantity: number): void {
    setMutationError(null);
    startMutation(async () => {
      applyOptimisticUpdate({ type: "updateQuantity", productId, quantity });
      try {
        await updateCartLineQuantityAction(productId, quantity);
      } catch (error) {
        setMutationError(mutationFailureMessage(error));
      }
    });
  }

  function removeLine(productId: string): void {
    setMutationError(null);
    startMutation(async () => {
      applyOptimisticUpdate({ type: "removeLine", productId });
      try {
        await removeCartLineAction(productId);
      } catch (error) {
        setMutationError(mutationFailureMessage(error));
      }
    });
  }

  return { cart: optimisticCart, isMutating, mutationError, addLine, updateQuantity, removeLine };
}
