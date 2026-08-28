package com.spacefurni.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.shared.domain.Money;
import org.junit.jupiter.api.Test;

class FixedAmountDiscountStrategyTest {

    private final FixedAmountDiscountStrategy strategy = new FixedAmountDiscountStrategy();

    @Test
    void returnsTheFixedAmountWhenBelowSubtotal() {
        Promotion fixedOff = new Promotion("SAVE200K", PromotionType.FIXED_AMOUNT, 200_000L, null, true, null, null);

        Money discount = strategy.calculateDiscount(Money.ofVnd(1_000_000L), fixedOff);

        assertThat(discount.amount()).isEqualTo(200_000L);
        assertThat(discount.currencyCode()).isEqualTo("VND");
    }

    @Test
    void capsDiscountAtSubtotalWhenFixedAmountExceedsIt() {
        Promotion fixedOff = new Promotion("SAVE200K", PromotionType.FIXED_AMOUNT, 200_000L, null, true, null, null);

        Money discount = strategy.calculateDiscount(Money.ofVnd(100_000L), fixedOff);

        assertThat(discount.amount()).isEqualTo(100_000L);
    }
}
