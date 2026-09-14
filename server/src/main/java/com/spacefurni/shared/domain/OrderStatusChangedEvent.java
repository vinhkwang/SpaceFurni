package com.spacefurni.shared.domain;

import java.util.UUID;

public record OrderStatusChangedEvent(UUID orderId, String orderNumber, UUID userId, String previousStatus,
        String newStatus) {
}
