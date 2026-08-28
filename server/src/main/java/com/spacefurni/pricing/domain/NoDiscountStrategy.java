package com.spacefurni.pricing.domain;

import com.spacefurni.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class NoDiscountStrategy implements DiscountStrategy {

    @Override
    public Money calculateDiscount(Money subtotal, Promotion promotion) {
        return new Money(0L, subtotal.currencyCode());
    }
}
