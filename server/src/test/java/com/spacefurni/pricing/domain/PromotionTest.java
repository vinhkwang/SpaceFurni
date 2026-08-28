package com.spacefurni.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.shared.domain.Money;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class PromotionTest {

    private static final Instant NOW = Instant.parse("2026-06-15T00:00:00Z");

    @Test
    void isRedeemableAtIsTrueWhenActiveWithNoWindowOrMinimum() {
        Promotion promotion = new Promotion("SPACE10", PromotionType.PERCENTAGE, 10L, null, true, null, null);

        assertThat(promotion.isRedeemableAt(NOW, Money.ofVnd(1L))).isTrue();
    }

    @Test
    void isRedeemableAtIsFalseWhenInactive() {
        Promotion promotion = new Promotion("SPACE10", PromotionType.PERCENTAGE, 10L, null, false, null, null);

        assertThat(promotion.isRedeemableAt(NOW, Money.ofVnd(1_000_000L))).isFalse();
    }

    @Test
    void isRedeemableAtIsFalseBeforeStartWindow() {
        Promotion promotion = new Promotion("SPACE10", PromotionType.PERCENTAGE, 10L, null, true,
                NOW.plus(1, ChronoUnit.DAYS), null);

        assertThat(promotion.isRedeemableAt(NOW, Money.ofVnd(1_000_000L))).isFalse();
    }

    @Test
    void isRedeemableAtIsFalseAfterEndWindow() {
        Promotion promotion = new Promotion("SPACE10", PromotionType.PERCENTAGE, 10L, null, true, null,
                NOW.minus(1, ChronoUnit.DAYS));

        assertThat(promotion.isRedeemableAt(NOW, Money.ofVnd(1_000_000L))).isFalse();
    }

    @Test
    void isRedeemableAtIsTrueExactlyAtWindowBoundaries() {
        Promotion promotion = new Promotion("SPACE10", PromotionType.PERCENTAGE, 10L, null, true, NOW, NOW);

        assertThat(promotion.isRedeemableAt(NOW, Money.ofVnd(1_000_000L))).isTrue();
    }

    @Test
    void isRedeemableAtIsFalseBelowMinimumSubtotal() {
        Promotion promotion = new Promotion("SPACE10", PromotionType.PERCENTAGE, 10L, 500_000L, true, null, null);

        assertThat(promotion.isRedeemableAt(NOW, Money.ofVnd(499_999L))).isFalse();
    }

    @Test
    void isRedeemableAtIsTrueAtExactlyMinimumSubtotal() {
        Promotion promotion = new Promotion("SPACE10", PromotionType.PERCENTAGE, 10L, 500_000L, true, null, null);

        assertThat(promotion.isRedeemableAt(NOW, Money.ofVnd(500_000L))).isTrue();
    }
}
