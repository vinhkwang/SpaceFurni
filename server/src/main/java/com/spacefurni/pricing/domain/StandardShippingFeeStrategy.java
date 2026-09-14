package com.spacefurni.pricing.domain;

import com.spacefurni.shared.application.PlatformSettingsService;
import com.spacefurni.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class StandardShippingFeeStrategy implements ShippingFeeStrategy {

    private final PlatformSettingsService platformSettingsService;

    public StandardShippingFeeStrategy(PlatformSettingsService platformSettingsService) {
        this.platformSettingsService = platformSettingsService;
    }

    @Override
    public Money calculateFee(Money subtotal) {
        PlatformSettingsService.PlatformSettingsSnapshot settings = platformSettingsService.getSettings();
        if (subtotal.isZero() || subtotal.isGreaterThan(settings.freeDeliveryThreshold())) {
            return new Money(0L, subtotal.currencyCode());
        }
        return new Money(settings.standardDeliveryFee().amount(), subtotal.currencyCode());
    }
}
