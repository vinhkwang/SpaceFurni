package com.spacefurni.inventory.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class InventoryItemRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID seedProductWithStock(int quantityOnHand) {
        Category department = categoryRepository
                .save(new Category(null, "Living room", "living-room-" + UUID.randomUUID(), null, 1));
        Category subCategory = categoryRepository
                .save(new Category(department, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Oslo Sofa", "oslo-sofa-" + UUID.randomUUID(),
                subCategory, Money.ofVnd(5_000_000L), null, ProductStatus.PUBLISHED, "short", "long", "200x90x80cm",
                "Fabric", "Beige", new BigDecimal("4.5"), 12, true, false);
        productRepository.saveAndFlush(product);
        entityManager.persistAndFlush(new InventoryItem(product.getId(), quantityOnHand, 0));
        return product.getId();
    }

    @Test
    void decrementSucceedsWhenStockIsSufficient() {
        UUID productId = seedProductWithStock(10);

        int rowsAffected = inventoryItemRepository.decrementQuantityOnHandIfSufficient(productId, 4);

        assertThat(rowsAffected).isEqualTo(1);
        InventoryItem updated = inventoryItemRepository.findById(productId).orElseThrow();
        assertThat(updated.getQuantityOnHand()).isEqualTo(6);
        assertThat(updated.getQuantityReserved()).isEqualTo(4);
    }

    @Test
    void decrementReturnsZeroWhenRequestedQuantityExceedsStock() {
        UUID productId = seedProductWithStock(3);

        int rowsAffected = inventoryItemRepository.decrementQuantityOnHandIfSufficient(productId, 4);

        assertThat(rowsAffected).isEqualTo(0);
        InventoryItem unchanged = inventoryItemRepository.findById(productId).orElseThrow();
        assertThat(unchanged.getQuantityOnHand()).isEqualTo(3);
        assertThat(unchanged.getQuantityReserved()).isEqualTo(0);
    }

    @Test
    void incrementRestoresQuantityOnHandAndReducesReserved() {
        UUID productId = seedProductWithStock(10);
        inventoryItemRepository.decrementQuantityOnHandIfSufficient(productId, 4);

        int rowsAffected = inventoryItemRepository.incrementQuantityOnHand(productId, 4);

        assertThat(rowsAffected).isEqualTo(1);
        InventoryItem restored = inventoryItemRepository.findById(productId).orElseThrow();
        assertThat(restored.getQuantityOnHand()).isEqualTo(10);
        assertThat(restored.getQuantityReserved()).isEqualTo(0);
    }

    @Test
    void findsAllByProductIdInOrderedByProductIdAscending() {
        UUID first = seedProductWithStock(5);
        UUID second = seedProductWithStock(8);

        List<InventoryItem> items = inventoryItemRepository
                .findAllByProductIdInOrderByProductIdAsc(List.of(second, first));

        assertThat(items).hasSize(2)
                .isSortedAccordingTo(Comparator.comparing(item -> item.getProductId().toString()));
    }
}
