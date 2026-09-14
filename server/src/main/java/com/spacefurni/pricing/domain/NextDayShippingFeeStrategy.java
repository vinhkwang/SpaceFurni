package com.spacefurni.pricing.domain;

import com.spacefurni.shared.application.PlatformSettingsService;
import com.spacefurni.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class NextDayShippingFeeStrategy implements ShippingFeeStrategy {

    private final PlatformSettingsService platformSettingsService;

    public NextDayShippingFeeStrategy(PlatformSettingsService platformSettingsService) {
        this.platformSettingsService = platformSettingsService;
    }

    @Override
    public Money calculateFee(Money subtotal) {
        Money nextDayDeliveryFee = platformSettingsService.getSettings().nextDayDeliveryFee();
        return new Money(nextDayDeliveryFee.amount(), subtotal.currencyCode());
    }
}
