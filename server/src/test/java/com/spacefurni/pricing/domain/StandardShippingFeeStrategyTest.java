package com.spacefurni.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.shared.domain.Money;
import org.junit.jupiter.api.Test;

class StandardShippingFeeStrategyTest {

    private final StandardShippingFeeStrategy strategy = new StandardShippingFeeStrategy();

    @Test
    void chargesStandardFeeAtExactlyTheFreeShippingThreshold() {
        Money fee = strategy.calculateFee(Money.ofVnd(10_000_000L));

        assertThat(fee.amount()).isEqualTo(300_000L);
    }

    @Test
    void isFreeJustAboveTheThreshold() {
        Money fee = strategy.calculateFee(Money.ofVnd(10_000_001L));

        assertThat(fee.amount()).isZero();
    }

    @Test
    void chargesStandardFeeBelowTheThreshold() {
        Money fee = strategy.calculateFee(Money.ofVnd(1_000_000L));

        assertThat(fee.amount()).isEqualTo(300_000L);
    }

    @Test
    void isZeroForAnEmptyCart() {
        Money fee = strategy.calculateFee(Money.zeroVnd());

        assertThat(fee.amount()).isZero();
    }
}
