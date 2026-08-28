package com.spacefurni.pricing.domain;

import com.spacefurni.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class NextDayShippingFeeStrategy implements ShippingFeeStrategy {

    private static final long NEXT_DAY_SHIPPING_FEE_AMOUNT = 300_000L;

    @Override
    public Money calculateFee(Money subtotal) {
        return new Money(NEXT_DAY_SHIPPING_FEE_AMOUNT, subtotal.currencyCode());
    }
}
