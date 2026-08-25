package com.spacefurni.catalog.api.dto;

public record ProductFilterRequest(String categorySlug, String subCategorySlug, Long minPrice, Long maxPrice,
        String sort, Integer page, Integer size) {
}
