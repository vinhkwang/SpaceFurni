package com.spacefurni.checkout.api.dto;

import com.spacefurni.checkout.domain.OrderStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryResponse(UUID id, String orderNumber, OrderStatus status, long totalAmount,
        String currencyCode, int itemCount, Instant placedAt) {
}
