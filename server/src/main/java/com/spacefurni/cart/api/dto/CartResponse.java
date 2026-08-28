package com.spacefurni.cart.api.dto;

import java.util.List;
import java.util.UUID;

public record CartResponse(UUID id, UUID guestToken, List<CartLineResponse> lines,
        PriceBreakdownResponse priceBreakdown) {
}
