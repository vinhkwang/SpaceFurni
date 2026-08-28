package com.spacefurni.pricing.domain;

import com.spacefurni.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public Money calculateDiscount(Money subtotal, Promotion promotion) {
        long discountAmount = Math.round(subtotal.amount() * promotion.getValue() / 100.0);
        return new Money(Math.min(discountAmount, subtotal.amount()), subtotal.currencyCode());
    }
}
