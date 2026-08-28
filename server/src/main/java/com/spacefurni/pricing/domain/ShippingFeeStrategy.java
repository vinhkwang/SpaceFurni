package com.spacefurni.pricing.domain;

import com.spacefurni.shared.domain.Money;

public interface ShippingFeeStrategy {

    Money calculateFee(Money subtotal);
}
