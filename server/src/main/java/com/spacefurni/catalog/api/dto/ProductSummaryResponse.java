package com.spacefurni.catalog.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResponse(UUID id, String sku, String slug, String name, String categoryName,
        long priceAmount, Long compareAtPriceAmount, String currencyCode, BigDecimal ratingAverage,
        Integer reviewCount, String primaryImageUrl, ProductBadgeResponse badge) {
}
