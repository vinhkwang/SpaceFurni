package com.spacefurni.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.checkout.domain.DeliveryDetails;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderItem;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.exception.BusinessRuleViolationException;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

class AdminOrderServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AdminOrderService adminOrderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    private UUID persistUser() {
        User user = new User("user-" + UUID.randomUUID() + "@example.com", "hash", "Test User", UserRole.CUSTOMER);
        return userRepository.save(user).getId();
    }

    private UUID persistProductWithReservedStock(int quantityOnHand, int quantityReserved) {
        Category category =
                categoryRepository.save(new Category(null, "Zzq Sofa", "zzq-sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Zzq Test Sofa", "zzq-test-sofa-" + UUID.randomUUID(),
                category, Money.ofVnd(1_000_000L), null, ProductStatus.DRAFT, "short", "long", "1x1x1cm", "Fabric",
                "Grey", new BigDecimal("4.0"), 0, false, false);
        UUID productId = productRepository.save(product).getId();
        inventoryItemRepository.save(new InventoryItem(productId, quantityOnHand, quantityReserved));
        return productId;
    }

    private Order persistOrder(String orderNumber, UUID userId, UUID productId, int quantity, OrderStatus status) {
        DeliveryDetails deliveryDetails =
                new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1", "Ho Chi Minh City", null);
        Order order = new Order(orderNumber, userId, Money.ofVnd(1_000_000L), Money.ofVnd(300_000L), Money.zeroVnd(),
                Money.ofVnd(1_300_000L), null, deliveryDetails, DeliveryWindow.STANDARD, PaymentMethod.CARD);
        order.addItem(new OrderItem(productId, "Test Sofa", "SKU-1", 1_000_000L, quantity, 1_000_000L * quantity));
        if (status == OrderStatus.PAID) {
            order.transitionTo(OrderStatus.PAID);
        }
        return orderRepository.save(order);
    }

    @Test
    void transitionsToALegalNextStatus() {
        UUID userId = persistUser();
        UUID productId = persistProductWithReservedStock(10, 2);
        Order order = persistOrder("SF-5001", userId, productId, 2, OrderStatus.PAID);

        adminOrderService.transitionOrderStatus("SF-5001", OrderStatus.PACKING, order.getVersion());

        Order reloaded = orderRepository.findByOrderNumber("SF-5001").orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.PACKING);
    }

    @Test
    void rejectsAnIllegalTransitionFromDeliveredToPending() {
        UUID userId = persistUser();
        UUID productId = persistProductWithReservedStock(10, 2);
        Order order = persistOrder("SF-5002", userId, productId, 2, OrderStatus.PAID);
        order.transitionTo(OrderStatus.PACKING);
        order.transitionTo(OrderStatus.DELIVERED);
        Order deliveredOrder = orderRepository.save(order);

        assertThatThrownBy(() -> adminOrderService.transitionOrderStatus("SF-5002", OrderStatus.PENDING,
                deliveredOrder.getVersion())).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void rejectsAStaleVersionAsConcurrentModification() {
        UUID userId = persistUser();
        UUID productId = persistProductWithReservedStock(10, 2);
        persistOrder("SF-5003", userId, productId, 2, OrderStatus.PAID);

        assertThatThrownBy(
                () -> adminOrderService.transitionOrderStatus("SF-5003", OrderStatus.PACKING, 999L))
                        .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void cancellingReleasesReservedStockBackToQuantityOnHand() {
        UUID userId = persistUser();
        UUID productId = persistProductWithReservedStock(8, 2);
        Order order = persistOrder("SF-5004", userId, productId, 2, OrderStatus.PAID);

        adminOrderService.transitionOrderStatus("SF-5004", OrderStatus.CANCELLED, order.getVersion());

        Order reloaded = orderRepository.findByOrderNumber("SF-5004").orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        InventoryItem inventoryItem = inventoryItemRepository.findById(productId).orElseThrow();
        assertThat(inventoryItem.getQuantityOnHand()).isEqualTo(10);
        assertThat(inventoryItem.getQuantityReserved()).isEqualTo(0);
    }

    @Test
    void throwsForAnUnknownOrderNumber() {
        assertThatThrownBy(() -> adminOrderService.transitionOrderStatus("SF-DOES-NOT-EXIST", OrderStatus.PACKING,
                0L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
