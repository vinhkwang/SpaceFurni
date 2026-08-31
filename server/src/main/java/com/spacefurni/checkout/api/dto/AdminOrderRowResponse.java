package com.spacefurni.checkout.api.dto;

import com.spacefurni.checkout.domain.OrderStatus;
import java.time.Instant;

public record AdminOrderRowResponse(String orderNumber, String customerName, String district, String itemSummary,
        int lineCount, String paymentLabel, Instant placedAt, long totalAmount, String currencyCode,
        OrderStatus status) {
}
