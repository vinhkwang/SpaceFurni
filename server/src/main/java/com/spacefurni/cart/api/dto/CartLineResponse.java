package com.spacefurni.cart.api.dto;

import java.util.UUID;

public record CartLineResponse(UUID productId, String productSlug, String productName, String imageUrl,
        long unitPriceAmount, String currencyCode, int quantity, long lineTotalAmount) {
}
