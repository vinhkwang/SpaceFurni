package com.spacefurni.catalog.api.dto;

import com.spacefurni.catalog.domain.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record AdminProductRequest(
        @NotBlank String title,

        @NotBlank String departmentSlug,

        String subCategorySlug,

        @Positive long price,

        @PositiveOrZero int stock,

        String shortDescription,

        String longDescription,

        String dimensions,

        String material,

        String primaryColorName,

        String imageUrl,

        ProductStatus status,

        Long version) {
}
