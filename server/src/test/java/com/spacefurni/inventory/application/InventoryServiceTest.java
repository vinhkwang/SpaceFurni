package com.spacefurni.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.inventory.api.dto.StockReservationLine;
import com.spacefurni.inventory.domain.InsufficientStockException;
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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class InventoryServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private InventoryService service() {
        return new InventoryService(inventoryItemRepository);
    }

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
    void reserveStockForOrderLinesDecrementsEachLineAtomically() {
        UUID productId = seedProductWithStock(10);

        service().reserveStockForOrderLines(List.of(new StockReservationLine(productId, 4)));

        InventoryItem item = inventoryItemRepository.findById(productId).orElseThrow();
        assertThat(item.getQuantityOnHand()).isEqualTo(6);
        assertThat(item.getQuantityReserved()).isEqualTo(4);
    }

    @Test
    void reserveStockForOrderLinesThrowsInsufficientStockExceptionOnShortfall() {
        UUID productId = seedProductWithStock(2);

        assertThatThrownBy(() -> service().reserveStockForOrderLines(List.of(new StockReservationLine(productId, 5))))
                .isInstanceOf(InsufficientStockException.class)
                .satisfies(exception -> {
                    InsufficientStockException insufficientStockException = (InsufficientStockException) exception;
                    assertThat(insufficientStockException.getProductId()).isEqualTo(productId);
                    assertThat(insufficientStockException.getRequestedQuantity()).isEqualTo(5);
                    assertThat(insufficientStockException.getAvailableQuantity()).isEqualTo(2);
                });
    }

    @Test
    void reserveStockForOrderLinesProcessesLinesInAscendingProductIdOrder() {
        UUID higherProductId = null;
        UUID lowerProductId = null;
        for (int attempt = 0; attempt < 20 && (higherProductId == null || lowerProductId == null); attempt++) {
            UUID candidateA = seedProductWithStock(10);
            UUID candidateB = seedProductWithStock(1);
            if (candidateA.compareTo(candidateB) > 0) {
                higherProductId = candidateA;
                lowerProductId = candidateB;
            }
        }
        assertThat(higherProductId).isNotNull();
        UUID higher = higherProductId;
        UUID lower = lowerProductId;

        List<StockReservationLine> unsortedLines = List.of(new StockReservationLine(higher, 1),
                new StockReservationLine(lower, 5));

        assertThatThrownBy(() -> service().reserveStockForOrderLines(unsortedLines))
                .isInstanceOf(InsufficientStockException.class)
                .satisfies(exception -> assertThat(((InsufficientStockException) exception).getProductId())
                        .isEqualTo(lower));
        InventoryItem higherItem = inventoryItemRepository.findById(higher).orElseThrow();
        assertThat(higherItem.getQuantityOnHand()).isEqualTo(10);
    }

    @Test
    void releaseStockForOrderLinesRestoresQuantityOnHandAndReducesReserved() {
        UUID productId = seedProductWithStock(10);
        service().reserveStockForOrderLines(List.of(new StockReservationLine(productId, 4)));

        service().releaseStockForOrderLines(List.of(new StockReservationLine(productId, 4)));

        InventoryItem item = inventoryItemRepository.findById(productId).orElseThrow();
        assertThat(item.getQuantityOnHand()).isEqualTo(10);
        assertThat(item.getQuantityReserved()).isEqualTo(0);
    }

    @Test
    void adjustQuantityOnHandAppliesPositiveDelta() {
        UUID productId = seedProductWithStock(5);

        service().adjustQuantityOnHand(productId, 3);

        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(8);
    }

    @Test
    void adjustQuantityOnHandRejectsNegativeDeltaTakingStockBelowZero() {
        UUID productId = seedProductWithStock(2);

        assertThatThrownBy(() -> service().adjustQuantityOnHand(productId, -5))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(2);
    }

    @Test
    void adjustQuantityOnHandThrowsResourceNotFoundExceptionForUnknownProduct() {
        assertThatThrownBy(() -> service().adjustQuantityOnHand(UUID.randomUUID(), 1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAvailableQuantitiesReturnsQuantityOnHandKeyedByProductId() {
        UUID first = seedProductWithStock(5);
        UUID second = seedProductWithStock(8);

        Map<UUID, Integer> quantities = service().findAvailableQuantities(List.of(first, second));

        assertThat(quantities).containsEntry(first, 5).containsEntry(second, 8);
    }
}
