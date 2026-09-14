package com.spacefurni.shared.domain;

import java.util.UUID;

public record LowStockEvent(UUID productId, int quantityOnHand, int threshold) {
}
