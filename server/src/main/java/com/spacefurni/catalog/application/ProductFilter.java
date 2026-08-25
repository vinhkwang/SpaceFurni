package com.spacefurni.catalog.application;

public record ProductFilter(String departmentSlug, String subCategorySlug, Long minPriceAmount, Long maxPriceAmount,
        ProductSortOption sort) {
}
