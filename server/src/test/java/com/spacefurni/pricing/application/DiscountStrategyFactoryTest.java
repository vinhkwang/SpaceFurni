package com.spacefurni.pricing.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.pricing.domain.FixedAmountDiscountStrategy;
import com.spacefurni.pricing.domain.NoDiscountStrategy;
import com.spacefurni.pricing.domain.PercentageDiscountStrategy;
import com.spacefurni.pricing.domain.Promotion;
import com.spacefurni.pricing.domain.PromotionType;
import org.junit.jupiter.api.Test;

class DiscountStrategyFactoryTest {

    private final DiscountStrategyFactory factory = new DiscountStrategyFactory(new PercentageDiscountStrategy(),
            new FixedAmountDiscountStrategy(), new NoDiscountStrategy());

    @Test
    void resolvesPercentageStrategyForPercentagePromotion() {
        Promotion promotion = new Promotion("SPACE10", PromotionType.PERCENTAGE, 10L, null, true, null, null);

        assertThat(factory.resolve(promotion)).isInstanceOf(PercentageDiscountStrategy.class);
    }

    @Test
    void resolvesFixedAmountStrategyForFixedAmountPromotion() {
        Promotion promotion = new Promotion("SAVE200K", PromotionType.FIXED_AMOUNT, 200_000L, null, true, null,
                null);

        assertThat(factory.resolve(promotion)).isInstanceOf(FixedAmountDiscountStrategy.class);
    }

    @Test
    void resolvesNoDiscountStrategyWhenPromotionIsAbsent() {
        assertThat(factory.resolve(null)).isInstanceOf(NoDiscountStrategy.class);
    }
}
