package com.spacefurni.catalog.api.dto;

import java.util.List;
import java.util.UUID;

public record CategoryTreeResponse(UUID id, String name, String slug, String imageUrl, long productCount,
        List<CategoryTreeResponse> subCategories) {
}
