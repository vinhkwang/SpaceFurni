package com.spacefurni.cart.api.dto;

public record PriceBreakdownResponse(long subtotalAmount, long shippingAmount, long discountAmount, long totalAmount,
        String currencyCode, String appliedPromotionCode, long amountToFreeShippingAmount) {
}
