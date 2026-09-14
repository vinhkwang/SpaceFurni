package com.spacefurni.shared.notification;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.inventory.api.dto.StockReservationLine;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class InventoryNotificationListenerTest extends AbstractIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @MockitoBean
    private NotificationService notificationService;

    private record SeededProduct(UUID productId, String name) {
    }

    private SeededProduct seedProductWithStock(int quantityOnHand) {
        Category category = categoryRepository
                .save(new Category(null, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        String name = "Test Sofa " + UUID.randomUUID();
        Product product = new Product("SKU-" + UUID.randomUUID(), name, "test-sofa-" + UUID.randomUUID(), category,
                Money.ofVnd(1_000_000L), null, ProductStatus.PUBLISHED, "short", "long", "1x1x1cm", "Fabric",
                "Grey", new BigDecimal("4.0"), 0, false, false);
        productRepository.saveAndFlush(product);
        inventoryItemRepository.saveAndFlush(new InventoryItem(product.getId(), quantityOnHand, 0));
        return new SeededProduct(product.getId(), name);
    }

    @Test
    void crossingTheLowStockThresholdSendsExactlyOneAlertWithCorrectContent() {
        SeededProduct product = seedProductWithStock(7);

        inventoryService.reserveStockForOrderLines(List.of(new StockReservationLine(product.productId(), 2)));

        verify(notificationService, times(1)).sendLowStockAlert(product.name(), 5, 6);
    }

    @Test
    void stayingAtOrAboveTheThresholdSendsNoAlert() {
        SeededProduct product = seedProductWithStock(7);

        inventoryService.reserveStockForOrderLines(List.of(new StockReservationLine(product.productId(), 1)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void furtherDecrementsAfterCrossingDoNotSendAnotherAlert() {
        SeededProduct product = seedProductWithStock(7);
        inventoryService.reserveStockForOrderLines(List.of(new StockReservationLine(product.productId(), 2)));

        inventoryService.reserveStockForOrderLines(List.of(new StockReservationLine(product.productId(), 1)));

        verify(notificationService, times(1)).sendLowStockAlert(product.name(), 5, 6);
    }
}
