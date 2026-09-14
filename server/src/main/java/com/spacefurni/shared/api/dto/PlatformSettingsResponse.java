package com.spacefurni.shared.api.dto;

public record PlatformSettingsResponse(long freeDeliveryThresholdAmount, long standardDeliveryFeeAmount,
        long nextDayDeliveryFeeAmount, int lowStockThresholdUnits) {
}
