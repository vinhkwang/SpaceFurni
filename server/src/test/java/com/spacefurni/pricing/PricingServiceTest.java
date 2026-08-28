package com.spacefurni.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.pricing.application.DiscountStrategyFactory;
import com.spacefurni.pricing.application.PricingLine;
import com.spacefurni.pricing.application.PricingService;
import com.spacefurni.pricing.application.ShippingFeeStrategyResolver;
import com.spacefurni.pricing.domain.FixedAmountDiscountStrategy;
import com.spacefurni.pricing.domain.NextDayShippingFeeStrategy;
import com.spacefurni.pricing.domain.NoDiscountStrategy;
import com.spacefurni.pricing.domain.PercentageDiscountStrategy;
import com.spacefurni.pricing.domain.PriceBreakdown;
import com.spacefurni.pricing.domain.Promotion;
import com.spacefurni.pricing.domain.PromotionType;
import com.spacefurni.pricing.domain.StandardShippingFeeStrategy;
import com.spacefurni.pricing.infrastructure.PromotionRepository;
import com.spacefurni.shared.domain.Money;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PricingServiceTest {

    @Autowired
    private PromotionRepository promotionRepository;

    private PricingService service() {
        DiscountStrategyFactory discountStrategyFactory = new DiscountStrategyFactory(
                new PercentageDiscountStrategy(), new FixedAmountDiscountStrategy(), new NoDiscountStrategy());
        ShippingFeeStrategyResolver shippingFeeStrategyResolver = new ShippingFeeStrategyResolver(
                new StandardShippingFeeStrategy(), new NextDayShippingFeeStrategy());
        return new PricingService(promotionRepository, discountStrategyFactory, shippingFeeStrategyResolver);
    }

    private List<PricingLine> lineOf(long unitPriceAmount, int quantity) {
        return List.of(new PricingLine(Money.ofVnd(unitPriceAmount), quantity));
    }

    @Test
    void standardShippingChargesFeeAtExactlyTheFreeShippingThreshold() {
        PriceBreakdown breakdown = service().calculate(lineOf(10_000_000L, 1), null, DeliveryWindow.STANDARD);

        assertThat(breakdown.shipping().amount()).isEqualTo(300_000L);
    }

    @Test
    void standardShippingIsFreeJustAboveTheThreshold() {
        PriceBreakdown breakdown = service().calculate(lineOf(10_000_001L, 1), null, DeliveryWindow.STANDARD);

        assertThat(breakdown.shipping().amount()).isZero();
    }

    @Test
    void emptyCartTotalsZero() {
        PriceBreakdown breakdown = service().calculate(List.of(), null, DeliveryWindow.STANDARD);

        assertThat(breakdown.subtotal().amount()).isZero();
        assertThat(breakdown.shipping().amount()).isZero();
        assertThat(breakdown.discount().amount()).isZero();
        assertThat(breakdown.total().amount()).isZero();
    }

    @Test
    void space10AppliesTenPercentDiscountOnAKnownSubtotal() {
        PriceBreakdown breakdown = service().calculate(lineOf(2_000_000L, 1), "SPACE10", DeliveryWindow.STANDARD);

        assertThat(breakdown.appliedPromotionCode()).isEqualTo("SPACE10");
        assertThat(breakdown.discount().amount()).isEqualTo(200_000L);
        assertThat(breakdown.total().amount()).isEqualTo(2_000_000L - 200_000L + 300_000L);
    }

    @Test
    void discountIsAppliedToSubtotalNotToSubtotalPlusShipping() {
        PriceBreakdown breakdown = service().calculate(lineOf(2_000_000L, 1), "SPACE10", DeliveryWindow.STANDARD);

        long tenPercentOfSubtotal = 200_000L;
        long tenPercentOfSubtotalPlusShipping = 230_000L;
        assertThat(breakdown.discount().amount()).isEqualTo(tenPercentOfSubtotal)
                .isNotEqualTo(tenPercentOfSubtotalPlusShipping);
    }

    @Test
    void nextDayShippingIsAlwaysFlatFeeRegardlessOfSubtotal() {
        PriceBreakdown breakdown = service().calculate(lineOf(50_000_000L, 1), null, DeliveryWindow.NEXT_DAY);

        assertThat(breakdown.shipping().amount()).isEqualTo(300_000L);
    }

    @Test
    void invalidPromotionCodeYieldsNoDiscount() {
        PriceBreakdown breakdown = service().calculate(lineOf(1_000_000L, 1), "DOES-NOT-EXIST",
                DeliveryWindow.STANDARD);

        assertThat(breakdown.discount().amount()).isZero();
        assertThat(breakdown.appliedPromotionCode()).isNull();
    }

    @Test
    void discountNeverExceedsSubtotal() {
        String code = "BIGFIXED" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        promotionRepository
                .saveAndFlush(new Promotion(code, PromotionType.FIXED_AMOUNT, 5_000_000L, null, true, null, null));

        PriceBreakdown breakdown = service().calculate(lineOf(1_000_000L, 1), code, DeliveryWindow.STANDARD);

        assertThat(breakdown.discount().amount()).isEqualTo(1_000_000L);
        assertThat(breakdown.total().amount()).isEqualTo(300_000L);
    }
}
