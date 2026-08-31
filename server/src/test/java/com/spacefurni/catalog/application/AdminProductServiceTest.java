package com.spacefurni.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.catalog.api.dto.AdminProductRequest;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import com.spacefurni.support.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminProductServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AdminProductService adminProductService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    private AdminProductRequest validRequest(String title) {
        return new AdminProductRequest(title, "living-room", "sofa", 7_200_000L, 12, "Short description",
                "Long description", "80x75x70cm", "Oak, linen", "Terracotta", "https://example.com/chair.jpg",
                ProductStatus.PUBLISHED, null);
    }

    @Test
    void createsProductWithInventoryRowInTheSameTransaction() {
        UUID productId = adminProductService.createProduct(validRequest("Zzq Admin Test Sofa"));

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getName()).isEqualTo("Zzq Admin Test Sofa");
        assertThat(product.getSlug()).isEqualTo("zzq-admin-test-sofa");
        assertThat(product.getSku()).startsWith("LIV-");

        Category sofaCategory = categoryRepository.findBySlug("sofa").orElseThrow();
        assertThat(product.getCategory().getId()).isEqualTo(sofaCategory.getId());

        InventoryItem inventoryItem = inventoryItemRepository.findById(productId).orElseThrow();
        assertThat(inventoryItem.getQuantityOnHand()).isEqualTo(12);
        assertThat(inventoryItem.getQuantityReserved()).isZero();
    }

    @Test
    void duplicateNameYieldsADistinctSlug() {
        adminProductService.createProduct(validRequest("Zzq Duplicate Test Sofa"));
        UUID secondProductId = adminProductService.createProduct(validRequest("Zzq Duplicate Test Sofa"));

        Product secondProduct = productRepository.findById(secondProductId).orElseThrow();
        assertThat(secondProduct.getSlug()).isEqualTo("zzq-duplicate-test-sofa-2");
    }

    @Test
    void missingSubCategoryDefaultsToTheDepartmentsFirstSubCategory() {
        AdminProductRequest requestWithoutSubCategory = new AdminProductRequest("Zzq Bedroom Default Test Item",
                "bedroom", null, 5_000_000L, 4, null, null, null, null, null, null, ProductStatus.DRAFT, null);

        UUID productId = adminProductService.createProduct(requestWithoutSubCategory);

        Product product = productRepository.findById(productId).orElseThrow();
        Category bedCategory = categoryRepository.findBySlug("bed").orElseThrow();
        assertThat(product.getCategory().getId()).isEqualTo(bedCategory.getId());
    }

    @Test
    void rejectsAnUnknownDepartment() {
        AdminProductRequest unknownDepartment = new AdminProductRequest("Zzq Ghost Test Product",
                "not-a-department", null, 5_000_000L, 4, null, null, null, null, null, null, ProductStatus.DRAFT,
                null);

        assertThatThrownBy(() -> adminProductService.createProduct(unknownDepartment))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsAnUnknownSubCategory() {
        AdminProductRequest unknownSubCategory = new AdminProductRequest("Zzq Ghost Test Product", "living-room",
                "not-a-sub-category", 5_000_000L, 4, null, null, null, null, null, null, ProductStatus.DRAFT, null);

        assertThatThrownBy(() -> adminProductService.createProduct(unknownSubCategory))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
