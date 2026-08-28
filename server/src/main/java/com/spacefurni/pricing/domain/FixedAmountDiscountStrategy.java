package com.spacefurni.pricing.domain;

import com.spacefurni.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    @Override
    public Money calculateDiscount(Money subtotal, Promotion promotion) {
        return new Money(Math.min(promotion.getValue(), subtotal.amount()), subtotal.currencyCode());
    }
}
