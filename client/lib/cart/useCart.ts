"use client";

import { useOptimistic, useState, useTransition } from "react";
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

export function useCart(cart: CartResponse) {
  const [optimisticCart, applyOptimisticUpdate] = useOptimistic(cart, applyOptimisticCartUpdate);
  const [isMutating, startMutation] = useTransition();
  const [mutationError, setMutationError] = useState<string | null>(null);

  function addLine(productId: string, quantity: number): void {
    setMutationError(null);
    startMutation(async () => {
      const result = await addCartLineAction(productId, quantity);
      if (!result.success) {
        setMutationError(result.errorMessage);
      }
    });
  }

  function updateQuantity(productId: string, quantity: number): void {
    setMutationError(null);
    startMutation(async () => {
      applyOptimisticUpdate({ type: "updateQuantity", productId, quantity });
      const result = await updateCartLineQuantityAction(productId, quantity);
      if (!result.success) {
        setMutationError(result.errorMessage);
      }
    });
  }

  function removeLine(productId: string): void {
    setMutationError(null);
    startMutation(async () => {
      applyOptimisticUpdate({ type: "removeLine", productId });
      const result = await removeCartLineAction(productId);
      if (!result.success) {
        setMutationError(result.errorMessage);
      }
    });
  }

  return { cart: optimisticCart, isMutating, mutationError, addLine, updateQuantity, removeLine };
}
