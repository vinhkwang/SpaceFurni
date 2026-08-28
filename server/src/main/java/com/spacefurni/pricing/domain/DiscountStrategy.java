package com.spacefurni.pricing.domain;

import com.spacefurni.shared.domain.Money;

public interface DiscountStrategy {

    Money calculateDiscount(Money subtotal, Promotion promotion);
}
