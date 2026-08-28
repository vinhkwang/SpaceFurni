package com.spacefurni.pricing.domain;

import com.spacefurni.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class StandardShippingFeeStrategy implements ShippingFeeStrategy {

    private static final long FREE_SHIPPING_THRESHOLD_AMOUNT = 10_000_000L;
    private static final long STANDARD_SHIPPING_FEE_AMOUNT = 300_000L;

    @Override
    public Money calculateFee(Money subtotal) {
        if (subtotal.isZero() || subtotal.amount() > FREE_SHIPPING_THRESHOLD_AMOUNT) {
            return new Money(0L, subtotal.currencyCode());
        }
        return new Money(STANDARD_SHIPPING_FEE_AMOUNT, subtotal.currencyCode());
    }
}
