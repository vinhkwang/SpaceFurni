package com.spacefurni.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.catalog.api.dto.AdminProductRequest;
import com.spacefurni.catalog.api.dto.AdminProductRowResponse;
import com.spacefurni.catalog.api.dto.StockAdjustmentRequest;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.inventory.domain.InsufficientStockException;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import com.spacefurni.support.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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

    @Test
    void updatesEditableFieldsAndLeavesStockAlone() {
        UUID productId = adminProductService.createProduct(validRequest("Zzq Update Test Sofa"));
        Product createdProduct = productRepository.findById(productId).orElseThrow();

        AdminProductRequest updateRequest = new AdminProductRequest("Zzq Updated Sofa Name", "kitchen", "cupboard",
                8_500_000L, 999, "New short description", "New long description", "90x80x75cm", "Walnut", "Charcoal",
                "https://example.com/updated.jpg", ProductStatus.DRAFT, createdProduct.getVersion());

        adminProductService.updateProduct(productId, updateRequest);

        Product updatedProduct = productRepository.findById(productId).orElseThrow();
        assertThat(updatedProduct.getName()).isEqualTo("Zzq Updated Sofa Name");
        assertThat(updatedProduct.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(updatedProduct.getPrice().amount()).isEqualTo(8_500_000L);
        assertThat(updatedProduct.getSlug()).isEqualTo("zzq-update-test-sofa");
        assertThat(updatedProduct.getSku()).isEqualTo(createdProduct.getSku());
        Category cupboardCategory = categoryRepository.findBySlug("cupboard").orElseThrow();
        assertThat(updatedProduct.getCategory().getId()).isEqualTo(cupboardCategory.getId());

        InventoryItem inventoryItem = inventoryItemRepository.findById(productId).orElseThrow();
        assertThat(inventoryItem.getQuantityOnHand()).isEqualTo(12);
    }

    @Test
    void rejectsAStaleVersionOnUpdate() {
        UUID productId = adminProductService.createProduct(validRequest("Zzq Stale Version Test Sofa"));
        Product createdProduct = productRepository.findById(productId).orElseThrow();
        long staleVersion = createdProduct.getVersion() + 1;

        AdminProductRequest staleUpdateRequest = new AdminProductRequest("Zzq Stale Version Test Sofa", "living-room",
                "sofa", 7_200_000L, 12, null, null, null, null, null, null, ProductStatus.PUBLISHED, staleVersion);

        assertThatThrownBy(() -> adminProductService.updateProduct(productId, staleUpdateRequest))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void archivingSoftDeletesAProductWithoutRemovingItsRow() {
        UUID productId = adminProductService.createProduct(validRequest("Zzq Archive Test Sofa"));

        adminProductService.archiveProduct(productId);

        Product archivedProduct = productRepository.findById(productId).orElseThrow();
        assertThat(archivedProduct.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
    }

    @Test
    void archivingAnUnknownProductThrows() {
        UUID unknownProductId = UUID.randomUUID();

        assertThatThrownBy(() -> adminProductService.archiveProduct(unknownProductId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void adjustsStockUpAndDown() {
        UUID productId = adminProductService.createProduct(validRequest("Zzq Stock Adjustment Test Sofa"));

        adminProductService.adjustStock(productId, new StockAdjustmentRequest(5));
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(17);

        adminProductService.adjustStock(productId, new StockAdjustmentRequest(-10));
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(7);
    }

    @Test
    void rejectsAStockAdjustmentThatWouldGoBelowZero() {
        UUID productId = adminProductService.createProduct(validRequest("Zzq Over Decrement Test Sofa"));

        assertThatThrownBy(() -> adminProductService.adjustStock(productId, new StockAdjustmentRequest(-100)))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(12);
    }

    @Test
    void adjustingStockForAnUnknownProductThrows() {
        UUID unknownProductId = UUID.randomUUID();

        assertThatThrownBy(() -> adminProductService.adjustStock(unknownProductId, new StockAdjustmentRequest(5)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listsProductsWithStockFromInventoryAndCategoryLabel() {
        UUID productId = adminProductService.createProduct(validRequest("Zzq Listing Stock Test Sofa"));

        Page<AdminProductRowResponse> page =
                adminProductService.listProducts("Zzq Listing Stock Test Sofa", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        AdminProductRowResponse row = page.getContent().get(0);
        assertThat(row.id()).isEqualTo(productId);
        assertThat(row.title()).isEqualTo("Zzq Listing Stock Test Sofa");
        assertThat(row.stockOnHand()).isEqualTo(12);
        assertThat(row.categoryLabel()).isEqualTo("Living room · Sofa");
        assertThat(row.status()).isEqualTo(ProductStatus.PUBLISHED);
    }

    @Test
    void searchMatchesBySku() {
        UUID productId = adminProductService.createProduct(validRequest("Zzq Sku Search Match Sofa"));
        String sku = productRepository.findById(productId).orElseThrow().getSku();

        Page<AdminProductRowResponse> page = adminProductService.listProducts(sku, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(AdminProductRowResponse::id).containsExactly(productId);
    }

    @Test
    void searchMatchesByDepartmentOrSubCategoryName() {
        AdminProductRequest deskRequest = new AdminProductRequest("Zzq Department Search Match Item", "work-study",
                "desk", 3_000_000L, 2, null, null, null, null, null, null, ProductStatus.PUBLISHED, null);
        UUID productId = adminProductService.createProduct(deskRequest);

        Page<AdminProductRowResponse> byDepartment =
                adminProductService.listProducts("work & study", PageRequest.of(0, 200));
        assertThat(byDepartment.getContent()).extracting(AdminProductRowResponse::id).contains(productId);

        Page<AdminProductRowResponse> bySubCategory = adminProductService.listProducts("desk", PageRequest.of(0, 200));
        assertThat(bySubCategory.getContent()).extracting(AdminProductRowResponse::id).contains(productId);
    }

    @Test
    void includesDraftAndArchivedProductsUnlikeThePublicList() {
        AdminProductRequest draftRequest = new AdminProductRequest("Zzq Draft Listing Test Sofa", "living-room",
                "sofa", 4_000_000L, 3, null, null, null, null, null, null, ProductStatus.DRAFT, null);
        adminProductService.createProduct(draftRequest);

        UUID archivedProductId = adminProductService.createProduct(validRequest("Zzq Archived Listing Test Sofa"));
        adminProductService.archiveProduct(archivedProductId);

        Page<AdminProductRowResponse> draftPage =
                adminProductService.listProducts("Zzq Draft Listing Test Sofa", PageRequest.of(0, 10));
        assertThat(draftPage.getContent()).extracting(AdminProductRowResponse::status)
                .containsExactly(ProductStatus.DRAFT);

        Page<AdminProductRowResponse> archivedPage =
                adminProductService.listProducts("Zzq Archived Listing Test Sofa", PageRequest.of(0, 10));
        assertThat(archivedPage.getContent()).extracting(AdminProductRowResponse::status)
                .containsExactly(ProductStatus.ARCHIVED);
    }

    @Test
    void pagesResultsAccordingToPageable() {
        adminProductService.createProduct(validRequest("Zzq Paging Test Sofa Alpha"));
        adminProductService.createProduct(validRequest("Zzq Paging Test Sofa Beta"));
        adminProductService.createProduct(validRequest("Zzq Paging Test Sofa Gamma"));

        Page<AdminProductRowResponse> firstPage =
                adminProductService.listProducts("Zzq Paging Test Sofa", PageRequest.of(0, 2));

        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }
}
