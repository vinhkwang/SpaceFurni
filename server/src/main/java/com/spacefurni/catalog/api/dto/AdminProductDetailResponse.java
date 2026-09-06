package com.spacefurni.catalog.api.dto;

import com.spacefurni.catalog.domain.ProductStatus;
import java.util.UUID;

public record AdminProductDetailResponse(UUID id, String title, String departmentSlug, String subCategorySlug,
        long price, int stock, String shortDescription, String longDescription, String dimensions, String material,
        String primaryColorName, String imageUrl, ProductStatus status, Long version) {
}
