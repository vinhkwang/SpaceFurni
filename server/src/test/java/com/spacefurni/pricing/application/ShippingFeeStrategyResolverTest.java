package com.spacefurni.pricing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.pricing.domain.NextDayShippingFeeStrategy;
import com.spacefurni.pricing.domain.StandardShippingFeeStrategy;
import com.spacefurni.shared.application.PlatformSettingsService;
import org.junit.jupiter.api.Test;

class ShippingFeeStrategyResolverTest {

    private final PlatformSettingsService platformSettingsService = mock(PlatformSettingsService.class);
    private final ShippingFeeStrategyResolver resolver = new ShippingFeeStrategyResolver(
            new StandardShippingFeeStrategy(platformSettingsService),
            new NextDayShippingFeeStrategy(platformSettingsService));

    @Test
    void resolvesStandardStrategyForStandardWindow() {
        assertThat(resolver.resolve(DeliveryWindow.STANDARD)).isInstanceOf(StandardShippingFeeStrategy.class);
    }

    @Test
    void resolvesNextDayStrategyForNextDayWindow() {
        assertThat(resolver.resolve(DeliveryWindow.NEXT_DAY)).isInstanceOf(NextDayShippingFeeStrategy.class);
    }
}
