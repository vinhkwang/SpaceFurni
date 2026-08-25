package com.spacefurni.catalog.api.mapper;

import com.spacefurni.catalog.api.dto.CategoryTreeResponse;
import com.spacefurni.catalog.domain.Category;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CategoryResponseMapper {

    public CategoryTreeResponse toTree(Category category, Map<UUID, Long> productCountsBySubCategoryId) {
        List<CategoryTreeResponse> subCategories = category.getChildren() == null ? List.of()
                : category.getChildren().stream().map(child -> toTree(child, productCountsBySubCategoryId)).toList();
        long productCount = subCategories.isEmpty()
                ? productCountsBySubCategoryId.getOrDefault(category.getId(), 0L)
                : subCategories.stream().mapToLong(CategoryTreeResponse::productCount).sum();
        return new CategoryTreeResponse(category.getId(), category.getName(), category.getSlug(),
                category.getImageUrl(), productCount, subCategories);
    }
}
