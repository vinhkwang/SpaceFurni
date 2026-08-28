package com.spacefurni.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.shared.domain.Money;
import org.junit.jupiter.api.Test;

class NextDayShippingFeeStrategyTest {

    private final NextDayShippingFeeStrategy strategy = new NextDayShippingFeeStrategy();

    @Test
    void isAlwaysTheFlatFeeRegardlessOfSubtotal() {
        assertThat(strategy.calculateFee(Money.ofVnd(1_000_000L)).amount()).isEqualTo(300_000L);
        assertThat(strategy.calculateFee(Money.ofVnd(50_000_000L)).amount()).isEqualTo(300_000L);
    }
}
