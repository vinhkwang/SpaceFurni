package com.spacefurni.pricing.application;

import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.pricing.domain.PriceBreakdown;
import com.spacefurni.pricing.domain.Promotion;
import com.spacefurni.pricing.infrastructure.PromotionRepository;
import com.spacefurni.shared.domain.Money;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingService {

    private static final long FREE_SHIPPING_THRESHOLD_AMOUNT = 10_000_000L;

    private final PromotionRepository promotionRepository;
    private final DiscountStrategyFactory discountStrategyFactory;
    private final ShippingFeeStrategyResolver shippingFeeStrategyResolver;

    public PricingService(PromotionRepository promotionRepository, DiscountStrategyFactory discountStrategyFactory,
            ShippingFeeStrategyResolver shippingFeeStrategyResolver) {
        this.promotionRepository = promotionRepository;
        this.discountStrategyFactory = discountStrategyFactory;
        this.shippingFeeStrategyResolver = shippingFeeStrategyResolver;
    }

    @Transactional(readOnly = true)
    public PriceBreakdown calculate(List<PricingLine> lines, String promotionCode, DeliveryWindow deliveryWindow) {
        Money subtotal = computeSubtotal(lines);
        Promotion promotion = resolveRedeemablePromotion(promotionCode, subtotal);
        Money discount = discountStrategyFactory.resolve(promotion).calculateDiscount(subtotal, promotion);
        Money shipping = shippingFeeStrategyResolver.resolve(deliveryWindow).calculateFee(subtotal);
        Money total = subtotal.minus(discount).plus(shipping);
        String appliedPromotionCode = promotion == null ? null : promotion.getCode();
        Money amountToFreeShipping = computeAmountToFreeShipping(subtotal);
        return new PriceBreakdown(subtotal, shipping, discount, total, appliedPromotionCode, amountToFreeShipping);
    }

    private Money computeSubtotal(List<PricingLine> lines) {
        return lines.stream().map(line -> line.unitPrice().multipliedBy(line.quantity()))
                .reduce(Money.zeroVnd(), Money::plus);
    }

    private Promotion resolveRedeemablePromotion(String promotionCode, Money subtotal) {
        if (promotionCode == null || promotionCode.isBlank()) {
            return null;
        }
        return promotionRepository.findById(promotionCode.toUpperCase())
                .filter(promotion -> promotion.isRedeemableAt(Instant.now(), subtotal)).orElse(null);
    }

    private Money computeAmountToFreeShipping(Money subtotal) {
        long amount = Math.max(FREE_SHIPPING_THRESHOLD_AMOUNT + 1 - subtotal.amount(), 0L);
        return new Money(amount, subtotal.currencyCode());
    }
}
