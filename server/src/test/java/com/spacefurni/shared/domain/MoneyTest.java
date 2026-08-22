package com.spacefurni.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void ofVndCreatesAmountInVnd() {
        Money money = Money.ofVnd(150_000L);

        assertThat(money.amount()).isEqualTo(150_000L);
        assertThat(money.currencyCode()).isEqualTo("VND");
    }

    @Test
    void zeroVndIsZeroAndInVnd() {
        Money money = Money.zeroVnd();

        assertThat(money.isZero()).isTrue();
        assertThat(money.currencyCode()).isEqualTo("VND");
    }

    @Test
    void plusReturnsNewInstanceWithSummedAmount() {
        Money first = Money.ofVnd(100_000L);
        Money second = Money.ofVnd(50_000L);

        Money sum = first.plus(second);

        assertThat(sum.amount()).isEqualTo(150_000L);
        assertThat(first.amount()).isEqualTo(100_000L);
        assertThat(second.amount()).isEqualTo(50_000L);
    }

    @Test
    void minusReturnsNewInstanceWithSubtractedAmount() {
        Money first = Money.ofVnd(100_000L);
        Money second = Money.ofVnd(30_000L);

        Money difference = first.minus(second);

        assertThat(difference.amount()).isEqualTo(70_000L);
        assertThat(first.amount()).isEqualTo(100_000L);
    }

    @Test
    void multipliedByReturnsNewInstanceScaledByFactor() {
        Money unitPrice = Money.ofVnd(25_000L);

        Money total = unitPrice.multipliedBy(3);

        assertThat(total.amount()).isEqualTo(75_000L);
        assertThat(unitPrice.amount()).isEqualTo(25_000L);
    }

    @Test
    void isGreaterThanComparesAmounts() {
        Money larger = Money.ofVnd(200_000L);
        Money smaller = Money.ofVnd(100_000L);

        assertThat(larger.isGreaterThan(smaller)).isTrue();
        assertThat(smaller.isGreaterThan(larger)).isFalse();
    }

    @Test
    void arithmeticAcrossDifferentCurrenciesThrows() {
        Money vnd = Money.ofVnd(100_000L);
        Money other = new Money(100_000L, "USD");

        assertThatThrownBy(() -> vnd.plus(other)).isInstanceOf(IllegalArgumentException.class);
    }
}
