package com.spacefurni.pricing.application;

import com.spacefurni.pricing.domain.DiscountStrategy;
import com.spacefurni.pricing.domain.FixedAmountDiscountStrategy;
import com.spacefurni.pricing.domain.NoDiscountStrategy;
import com.spacefurni.pricing.domain.PercentageDiscountStrategy;
import com.spacefurni.pricing.domain.Promotion;
import com.spacefurni.pricing.domain.PromotionType;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DiscountStrategyFactory {

    private final Map<PromotionType, DiscountStrategy> strategiesByPromotionType;
    private final NoDiscountStrategy noDiscountStrategy;

    public DiscountStrategyFactory(PercentageDiscountStrategy percentageDiscountStrategy,
            FixedAmountDiscountStrategy fixedAmountDiscountStrategy, NoDiscountStrategy noDiscountStrategy) {
        this.strategiesByPromotionType = Map.of(PromotionType.PERCENTAGE, percentageDiscountStrategy,
                PromotionType.FIXED_AMOUNT, fixedAmountDiscountStrategy);
        this.noDiscountStrategy = noDiscountStrategy;
    }

    public DiscountStrategy resolve(Promotion promotion) {
        if (promotion == null) {
            return noDiscountStrategy;
        }
        return strategiesByPromotionType.get(promotion.getType());
    }
}
