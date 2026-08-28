package com.spacefurni.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.shared.domain.Money;
import org.junit.jupiter.api.Test;

class NoDiscountStrategyTest {

    private final NoDiscountStrategy strategy = new NoDiscountStrategy();

    @Test
    void returnsZeroWhenNoPromotionIsGiven() {
        Money discount = strategy.calculateDiscount(Money.ofVnd(1_000_000L), null);

        assertThat(discount.amount()).isZero();
        assertThat(discount.currencyCode()).isEqualTo("VND");
    }

    @Test
    void returnsZeroEvenWhenAPromotionIsGiven() {
        Promotion promotion = new Promotion("SPACE10", PromotionType.PERCENTAGE, 10L, null, true, null, null);

        Money discount = strategy.calculateDiscount(Money.ofVnd(1_000_000L), promotion);

        assertThat(discount.amount()).isZero();
    }
}
