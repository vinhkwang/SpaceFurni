package com.spacefurni.catalog.api.dto;

import com.spacefurni.catalog.domain.ProductStatus;
import java.util.UUID;

public record AdminProductRowResponse(UUID id, String imageUrl, String title, String sku, String categoryLabel,
        long priceAmount, String currencyCode, int stockOnHand, ProductStatus status) {
}
