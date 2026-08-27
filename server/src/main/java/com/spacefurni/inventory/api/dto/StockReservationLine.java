package com.spacefurni.inventory.api.dto;

import java.util.UUID;

public record StockReservationLine(UUID productId, int quantity) {
}
