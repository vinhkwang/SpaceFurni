package com.spacefurni.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.spacefurni.shared.application.PlatformSettingsService;
import com.spacefurni.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StandardShippingFeeStrategyTest {

    private final PlatformSettingsService platformSettingsService = mock(PlatformSettingsService.class);
    private final StandardShippingFeeStrategy strategy = new StandardShippingFeeStrategy(platformSettingsService);

    @BeforeEach
    void seedSettings() {
        when(platformSettingsService.getSettings()).thenReturn(new PlatformSettingsService.PlatformSettingsSnapshot(
                Money.ofVnd(10_000_000L), Money.ofVnd(300_000L), Money.ofVnd(300_000L), 6));
    }

    @Test
    void chargesStandardFeeAtExactlyTheFreeShippingThreshold() {
        Money fee = strategy.calculateFee(Money.ofVnd(10_000_000L));

        assertThat(fee.amount()).isEqualTo(300_000L);
    }

    @Test
    void isFreeJustAboveTheThreshold() {
        Money fee = strategy.calculateFee(Money.ofVnd(10_000_001L));

        assertThat(fee.amount()).isZero();
    }

    @Test
    void chargesStandardFeeBelowTheThreshold() {
        Money fee = strategy.calculateFee(Money.ofVnd(1_000_000L));

        assertThat(fee.amount()).isEqualTo(300_000L);
    }

    @Test
    void isZeroForAnEmptyCart() {
        Money fee = strategy.calculateFee(Money.zeroVnd());

        assertThat(fee.amount()).isZero();
    }
}
