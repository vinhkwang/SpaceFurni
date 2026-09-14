package com.spacefurni.shared.api.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdatePlatformSettingsRequest(@PositiveOrZero long freeDeliveryThresholdAmount,
        @PositiveOrZero long standardDeliveryFeeAmount, @PositiveOrZero long nextDayDeliveryFeeAmount,
        @Positive int lowStockThresholdUnits) {
}
