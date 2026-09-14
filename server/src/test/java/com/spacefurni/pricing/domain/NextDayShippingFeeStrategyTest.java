package com.spacefurni.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.spacefurni.shared.application.PlatformSettingsService;
import com.spacefurni.shared.domain.Money;
import org.junit.jupiter.api.Test;

class NextDayShippingFeeStrategyTest {

    private final PlatformSettingsService platformSettingsService = mock(PlatformSettingsService.class);
    private final NextDayShippingFeeStrategy strategy = new NextDayShippingFeeStrategy(platformSettingsService);

    @Test
    void isAlwaysTheFlatFeeRegardlessOfSubtotal() {
        when(platformSettingsService.getSettings()).thenReturn(new PlatformSettingsService.PlatformSettingsSnapshot(
                Money.ofVnd(10_000_000L), Money.ofVnd(300_000L), Money.ofVnd(300_000L), 6));

        assertThat(strategy.calculateFee(Money.ofVnd(1_000_000L)).amount()).isEqualTo(300_000L);
        assertThat(strategy.calculateFee(Money.ofVnd(50_000_000L)).amount()).isEqualTo(300_000L);
    }
}
