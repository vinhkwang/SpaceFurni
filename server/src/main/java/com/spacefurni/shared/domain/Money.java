package com.spacefurni.shared.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record Money(long amount, String currencyCode) {

    public static Money ofVnd(long amount) {
        return new Money(amount, "VND");
    }

    public static Money zeroVnd() {
        return new Money(0L, "VND");
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount + other.amount, this.currencyCode);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount - other.amount, this.currencyCode);
    }

    public Money multipliedBy(int factor) {
        return new Money(this.amount * factor, this.currencyCode);
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return this.amount > other.amount;
    }

    public boolean isZero() {
        return this.amount == 0L;
    }

    private void requireSameCurrency(Money other) {
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new IllegalArgumentException(
                    "Cannot combine amounts in %s and %s".formatted(this.currencyCode, other.currencyCode));
        }
    }
}
