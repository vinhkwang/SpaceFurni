package com.spacefurni.shared.domain;

import java.util.UUID;

public record OrderPlacedEvent(UUID orderId, String orderNumber, UUID userId) {
}
