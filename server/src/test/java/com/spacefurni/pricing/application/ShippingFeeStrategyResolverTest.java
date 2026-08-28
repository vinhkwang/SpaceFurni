package com.spacefurni.pricing.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.pricing.domain.NextDayShippingFeeStrategy;
import com.spacefurni.pricing.domain.StandardShippingFeeStrategy;
import org.junit.jupiter.api.Test;

class ShippingFeeStrategyResolverTest {

    private final ShippingFeeStrategyResolver resolver = new ShippingFeeStrategyResolver(
            new StandardShippingFeeStrategy(), new NextDayShippingFeeStrategy());

    @Test
    void resolvesStandardStrategyForStandardWindow() {
        assertThat(resolver.resolve(DeliveryWindow.STANDARD)).isInstanceOf(StandardShippingFeeStrategy.class);
    }

    @Test
    void resolvesNextDayStrategyForNextDayWindow() {
        assertThat(resolver.resolve(DeliveryWindow.NEXT_DAY)).isInstanceOf(NextDayShippingFeeStrategy.class);
    }
}
