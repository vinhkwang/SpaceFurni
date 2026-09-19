package com.spacefurni.checkout.api.dto;

import com.spacefurni.checkout.domain.OrderStatus;
import java.time.Instant;

public record AdminCustomerOrderSummaryResponse(String orderNumber, Instant placedAt, long totalAmount,
        String currencyCode, OrderStatus status) {
}
