package com.spacefurni.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.catalog.api.dto.CategoryTreeResponse;
import com.spacefurni.catalog.api.dto.ProductDetailResponse;
import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.api.mapper.CategoryResponseMapper;
import com.spacefurni.catalog.api.mapper.ProductResponseMapper;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class CatalogQueryServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private CatalogQueryService service() {
        return new CatalogQueryService(productRepository, categoryRepository, new ProductResponseMapper(),
                new CategoryResponseMapper(), new InventoryService(inventoryItemRepository));
    }

    @Test
    void findPublishedProductsFiltersBySubCategoryAndSortsByPriceAscending() {
        Category department = categoryRepository
                .save(new Category(null, "Living room", "living-room-" + UUID.randomUUID(), null, 1));
        Category sofa = categoryRepository
                .save(new Category(department, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Category chair = categoryRepository
                .save(new Category(department, "Chair", "chair-" + UUID.randomUUID(), null, 2));
        Product expensiveSofa = publishedProduct("SKU-1", "Cloud Sofa", "cloud-sofa-" + UUID.randomUUID(), sofa,
                24_900_000L);
        Product cheapSofa = publishedProduct("SKU-2", "Claire Sofa", "claire-sofa-" + UUID.randomUUID(), sofa,
                19_500_000L);
        Product chairProduct = publishedProduct("SKU-3", "Halden Chair", "halden-chair-" + UUID.randomUUID(), chair,
                7_200_000L);
        entityManager.persistAndFlush(expensiveSofa);
        entityManager.persistAndFlush(cheapSofa);
        entityManager.persistAndFlush(chairProduct);
        entityManager.clear();

        ProductFilter filter = new ProductFilter(null, sofa.getSlug(), null, null, ProductSortOption.PRICE_ASC);
        Page<ProductSummaryResponse> page = service().findPublishedProducts(filter, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(ProductSummaryResponse::name).containsExactly("Claire Sofa",
                "Cloud Sofa");
    }

    @Test
    void findPublishedProductsExcludesUnpublishedProducts() {
        Category department = categoryRepository
                .save(new Category(null, "Kitchen", "kitchen-" + UUID.randomUUID(), null, 1));
        Category table = categoryRepository
                .save(new Category(department, "Table", "table-" + UUID.randomUUID(), null, 1));
        Product draftProduct = new Product("SKU-4", "Draft Table", "draft-table-" + UUID.randomUUID(), table,
                Money.ofVnd(2_000_000L), null, ProductStatus.DRAFT, "short", "long", "120x60cm", "Pine", "Natural",
                new BigDecimal("4.0"), 0, false, false);
        entityManager.persistAndFlush(draftProduct);
        entityManager.clear();

        ProductFilter filter = new ProductFilter(null, table.getSlug(), null, null, ProductSortOption.NEWEST);
        Page<ProductSummaryResponse> page = service().findPublishedProducts(filter, PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findProductDetailBySlugReturnsDetailWithRelatedProductsFromSameSubCategory() {
        Category department = categoryRepository
                .save(new Category(null, "Bedroom", "bedroom-" + UUID.randomUUID(), null, 1));
        Category bedsideTable = categoryRepository
                .save(new Category(department, "Bedside table", "bedside-table-" + UUID.randomUUID(), null, 1));
        Product anchor = publishedProduct("SKU-5", "Spindle Bedside Table", "spindle-" + UUID.randomUUID(),
                bedsideTable, 4_300_000L);
        Product sibling = publishedProduct("SKU-6", "Oak Bedside Table", "oak-bedside-" + UUID.randomUUID(),
                bedsideTable, 3_900_000L);
        entityManager.persistAndFlush(anchor);
        entityManager.persistAndFlush(sibling);
        entityManager.persistAndFlush(new InventoryItem(anchor.getId(), 3, 0));
        entityManager.clear();

        ProductDetailResponse detail = service().findProductDetailBySlug(anchor.getSlug());

        assertThat(detail.name()).isEqualTo("Spindle Bedside Table");
        assertThat(detail.relatedProducts()).extracting(ProductSummaryResponse::name)
                .containsExactly("Oak Bedside Table");
        assertThat(detail.availableQuantity()).isEqualTo(3);
        assertThat(detail.stockLabel()).isEqualTo("Only 3 left");
    }

    @Test
    void findProductDetailBySlugReturnsOutOfStockWhenNoInventoryItemExists() {
        Category department = categoryRepository
                .save(new Category(null, "Bedroom", "bedroom-" + UUID.randomUUID(), null, 1));
        Category bedsideTable = categoryRepository
                .save(new Category(department, "Bedside table", "bedside-table-" + UUID.randomUUID(), null, 1));
        Product anchor = publishedProduct("SKU-12", "Spindle Bedside Table", "spindle-" + UUID.randomUUID(),
                bedsideTable, 4_300_000L);
        entityManager.persistAndFlush(anchor);
        entityManager.clear();

        ProductDetailResponse detail = service().findProductDetailBySlug(anchor.getSlug());

        assertThat(detail.availableQuantity()).isEqualTo(0);
        assertThat(detail.stockLabel()).isEqualTo("Out of stock");
    }

    @Test
    void findProductDetailBySlugThrowsForUnknownSlug() {
        assertThatThrownBy(() -> service().findProductDetailBySlug("does-not-exist"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findProductDetailBySlugThrowsForUnpublishedProduct() {
        Category department = categoryRepository
                .save(new Category(null, "Work & study", "work-study-" + UUID.randomUUID(), null, 1));
        Category desk = categoryRepository
                .save(new Category(department, "Desk", "desk-" + UUID.randomUUID(), null, 1));
        Product draftProduct = new Product("SKU-7", "Draft Desk", "draft-desk-" + UUID.randomUUID(), desk,
                Money.ofVnd(5_000_000L), null, ProductStatus.DRAFT, "short", "long", "120x60cm", "Oak", "Natural",
                new BigDecimal("4.0"), 0, false, false);
        entityManager.persistAndFlush(draftProduct);
        entityManager.clear();

        assertThatThrownBy(() -> service().findProductDetailBySlug(draftProduct.getSlug()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findCategoryTreeRollsUpPublishedProductCountsFromSubCategoriesToDepartment() {
        Category department = categoryRepository
                .save(new Category(null, "Living room", "living-room-" + UUID.randomUUID(), null, 1));
        Category sofa = categoryRepository
                .save(new Category(department, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Category shelf = categoryRepository
                .save(new Category(department, "Shelf", "shelf-" + UUID.randomUUID(), null, 2));
        entityManager.persistAndFlush(publishedProduct("SKU-8", "Cloud Sofa", "cloud-" + UUID.randomUUID(), sofa,
                24_900_000L));
        entityManager.persistAndFlush(publishedProduct("SKU-9", "Claire Sofa", "claire-" + UUID.randomUUID(), sofa,
                19_500_000L));
        entityManager.persistAndFlush(publishedProduct("SKU-10", "Anita Shelf", "anita-" + UUID.randomUUID(), shelf,
                3_850_000L));
        entityManager.clear();

        List<CategoryTreeResponse> tree = service().findCategoryTree();

        CategoryTreeResponse departmentNode = tree.stream()
                .filter(node -> node.id().equals(department.getId())).findFirst().orElseThrow();
        assertThat(departmentNode.productCount()).isEqualTo(3L);
        assertThat(departmentNode.subCategories()).extracting(CategoryTreeResponse::productCount)
                .containsExactly(2L, 1L);
    }

    @Test
    void suggestProductsMatchesByNameCaseInsensitive() {
        Category department = categoryRepository
                .save(new Category(null, "Living room", "living-room-" + UUID.randomUUID(), null, 1));
        Category sofa = categoryRepository
                .save(new Category(department, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        String uniqueToken = UUID.randomUUID().toString().substring(0, 8);
        String productName = "ZEPHYR-" + uniqueToken + " Sofa";
        entityManager.persistAndFlush(publishedProduct("SKU-11", productName, "zephyr-" + UUID.randomUUID(), sofa,
                24_900_000L));
        entityManager.clear();

        List<ProductSummaryResponse> suggestions = service().suggestProducts("zephyr-" + uniqueToken, 5);

        assertThat(suggestions).extracting(ProductSummaryResponse::name).containsExactly(productName);
    }

    @Test
    void findProductSummariesByIdsReturnsSummariesKeyedByProductIdIncludingUnpublished() {
        Category department = categoryRepository
                .save(new Category(null, "Living room", "living-room-" + UUID.randomUUID(), null, 1));
        Category sofa = categoryRepository
                .save(new Category(department, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product published = publishedProduct("SKU-13", "Cloud Sofa", "cloud-" + UUID.randomUUID(), sofa,
                24_900_000L);
        Product archived = new Product("SKU-14", "Retired Sofa", "retired-" + UUID.randomUUID(), sofa,
                Money.ofVnd(9_900_000L), null, ProductStatus.ARCHIVED, "short", "long", "dims", "material", "color",
                new BigDecimal("4.0"), 0, false, false);
        entityManager.persistAndFlush(published);
        entityManager.persistAndFlush(archived);
        entityManager.clear();

        Map<UUID, ProductSummaryResponse> summaries = service()
                .findProductSummariesByIds(List.of(published.getId(), archived.getId()));

        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(published.getId()).name()).isEqualTo("Cloud Sofa");
        assertThat(summaries.get(archived.getId()).name()).isEqualTo("Retired Sofa");
    }

    private Product publishedProduct(String sku, String name, String slug, Category category, long priceAmount) {
        return new Product(sku, name, slug, category, Money.ofVnd(priceAmount), null, ProductStatus.PUBLISHED,
                "short", "long", "dims", "material", "color", new BigDecimal("4.5"), 10, false, false);
    }
}
