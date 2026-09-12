package com.spacefurni.checkout;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.cart.application.CartService;
import com.spacefurni.cart.domain.Cart;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.checkout.api.dto.DeliveryDetailsRequest;
import com.spacefurni.checkout.api.dto.PlaceOrderRequest;
import com.spacefurni.checkout.application.AdminOrderService;
import com.spacefurni.checkout.application.CheckoutService;
import com.spacefurni.checkout.application.OrderCancellationService;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderCancellationConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private OrderCancellationService orderCancellationService;

    @Autowired
    private AdminOrderService adminOrderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private UUID persistUser() {
        User user = new User("user-" + UUID.randomUUID() + "@example.com", "hash", "Test User", UserRole.CUSTOMER);
        return userRepository.saveAndFlush(user).getId();
    }

    private UUID seedProductWithStock(int quantityOnHand) {
        Category category = categoryRepository
                .save(new Category(null, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Concurrency Test Sofa",
                "concurrency-test-sofa-" + UUID.randomUUID(), category, Money.ofVnd(1_000_000L), null,
                ProductStatus.DRAFT, "short", "long", "1x1x1cm", "Fabric", "Grey", new BigDecimal("4.0"), 0, false,
                false);
        productRepository.saveAndFlush(product);
        inventoryItemRepository.saveAndFlush(new InventoryItem(product.getId(), quantityOnHand, 0));
        return product.getId();
    }

    private Cart cartWithLine(UUID userId, UUID productId, int quantity) {
        Cart cart = cartService.resolveOrCreateActiveCart(userId, null);
        return cartService.addLine(cart, productId, quantity, null);
    }

    private Order placePendingOrder(UUID userId, UUID productId, int quantity) {
        cartWithLine(userId, productId, quantity);
        DeliveryDetailsRequest deliveryDetails = new DeliveryDetailsRequest("Nguyen Van A", "0901234567", "1 Le Loi",
                "District 1", "Ho Chi Minh City", null);
        PlaceOrderRequest request = new PlaceOrderRequest(deliveryDetails, DeliveryWindow.STANDARD,
                PaymentMethod.CASH_ON_DELIVERY);
        return checkoutService.placeOrder(userId, UUID.randomUUID().toString(), request);
    }

    private void attempt(Runnable action, AtomicReference<Exception> failure) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            failure.set(exception);
        }
    }

    private void runConcurrently(List<Runnable> tasks) throws InterruptedException {
        int count = tasks.size();
        ExecutorService executor = Executors.newFixedThreadPool(count);
        CountDownLatch readyLatch = new CountDownLatch(count);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(count);
        for (Runnable task : tasks) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    task.run();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        readyLatch.await();
        startLatch.countDown();
        assertThat(doneLatch.await(30, TimeUnit.SECONDS)).as("all tasks finished within timeout").isTrue();
        executor.shutdown();
    }

    @Test
    void concurrentCustomerCancellationAndAdminPackingLeavesExactlyOneWinner() throws InterruptedException {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(5);
        Order order = placePendingOrder(userId, productId, 2);
        int quantityOnHandAfterOrdering = inventoryItemRepository.findById(productId).orElseThrow()
                .getQuantityOnHand();
        long versionAtRaceStart = order.getVersion();
        AtomicReference<Exception> cancelFailure = new AtomicReference<>();
        AtomicReference<Exception> packingFailure = new AtomicReference<>();

        runConcurrently(List.of(
                () -> attempt(() -> orderCancellationService.cancelOrder(userId, order.getId(), "Changed my mind"),
                        cancelFailure),
                () -> attempt(() -> adminOrderService.transitionOrderStatus(order.getOrderNumber(),
                        OrderStatus.PACKING, versionAtRaceStart), packingFailure)));

        boolean cancelWon = cancelFailure.get() == null;
        boolean packingWon = packingFailure.get() == null;
        assertThat(cancelWon ^ packingWon).as("exactly one of cancel or packing wins the race").isTrue();

        Order finalOrder = orderRepository.findById(order.getId()).orElseThrow();
        int finalQuantityOnHand = inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand();
        if (cancelWon) {
            assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(packingFailure.get()).isNotNull();
            assertThat(finalQuantityOnHand).isEqualTo(5);
        } else {
            assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.PACKING);
            assertThat(cancelFailure.get()).isNotNull();
            assertThat(finalQuantityOnHand).isEqualTo(quantityOnHandAfterOrdering);
        }
    }
}
