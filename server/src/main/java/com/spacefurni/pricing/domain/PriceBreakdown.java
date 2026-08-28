package com.spacefurni.pricing.domain;

import com.spacefurni.shared.domain.Money;

public record PriceBreakdown(Money subtotal, Money shipping, Money discount, Money total, String appliedPromotionCode,
        Money amountToFreeShipping) {
}
