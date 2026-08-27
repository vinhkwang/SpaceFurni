package com.spacefurni.catalog.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductDetailResponse(UUID id, String sku, String slug, String name, String categoryName,
        long priceAmount, Long compareAtPriceAmount, String currencyCode, BigDecimal ratingAverage,
        Integer reviewCount, String shortDescription, String longDescription, String dimensions, String material,
        String primaryColorName, ProductBadgeResponse badge, List<String> imageUrls,
        List<SpecificationEntry> specifications, List<String> colorSwatchHexCodes, int availableQuantity,
        String stockLabel, List<ProductSummaryResponse> relatedProducts) {

    public record SpecificationEntry(String key, String value) {
    }
}
