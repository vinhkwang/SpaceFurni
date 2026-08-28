package com.spacefurni.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.shared.domain.Money;
import org.junit.jupiter.api.Test;

class PercentageDiscountStrategyTest {

    private final PercentageDiscountStrategy strategy = new PercentageDiscountStrategy();

    @Test
    void calculatesExactPercentageOfSubtotal() {
        Promotion tenPercentOff = new Promotion("SPACE10", PromotionType.PERCENTAGE, 10L, null, true, null, null);

        Money discount = strategy.calculateDiscount(Money.ofVnd(19_500_000L), tenPercentOff);

        assertThat(discount.amount()).isEqualTo(1_950_000L);
        assertThat(discount.currencyCode()).isEqualTo("VND");
    }

    @Test
    void roundsFractionalDongToNearestWhole() {
        Promotion fifteenPercentOff = new Promotion("SAVE15", PromotionType.PERCENTAGE, 15L, null, true, null, null);

        Money discount = strategy.calculateDiscount(Money.ofVnd(333L), fifteenPercentOff);

        assertThat(discount.amount()).isEqualTo(Math.round(333L * 15 / 100.0));
    }

    @Test
    void capsDiscountAtSubtotalWhenPercentageWouldExceedIt() {
        Promotion overOneHundredPercent = new Promotion("HUGE", PromotionType.PERCENTAGE, 150L, null, true, null,
                null);

        Money discount = strategy.calculateDiscount(Money.ofVnd(1_000L), overOneHundredPercent);

        assertThat(discount.amount()).isEqualTo(1_000L);
    }
}
