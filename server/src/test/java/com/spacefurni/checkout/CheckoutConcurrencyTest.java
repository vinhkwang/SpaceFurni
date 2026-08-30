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
import com.spacefurni.checkout.application.CheckoutService;
import com.spacefurni.checkout.application.IdempotencyKeyConflictException;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.inventory.domain.InsufficientStockException;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.datasource.hikari.maximum-pool-size=60")
class CheckoutConcurrencyTest extends AbstractIntegrationTest {

    private static final String DEFAULT_PHONE = "0901234567";

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

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

    private void addToCart(UUID userId, UUID productId, int quantity) {
        Cart cart = cartService.resolveOrCreateActiveCart(userId, null);
        cartService.addLine(cart, productId, quantity);
    }

    private PlaceOrderRequest placeOrderRequest() {
        DeliveryDetailsRequest deliveryDetails = new DeliveryDetailsRequest("Nguyen Van A", DEFAULT_PHONE,
                "1 Le Loi", "District 1", "Ho Chi Minh City", null);
        return new PlaceOrderRequest(deliveryDetails, DeliveryWindow.STANDARD, PaymentMethod.CASH_ON_DELIVERY);
    }

    private void attempt(Supplier<Order> action, AtomicReference<Order> result, AtomicReference<Exception> failure) {
        try {
            result.set(action.get());
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
    void concurrentSubmissionsOfTheSameIdempotencyKeyProduceExactlyOneOrder() throws InterruptedException {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(10);
        addToCart(userId, productId, 1);
        String idempotencyKey = UUID.randomUUID().toString();
        PlaceOrderRequest request = placeOrderRequest();
        AtomicReference<Order> firstResult = new AtomicReference<>();
        AtomicReference<Order> secondResult = new AtomicReference<>();
        AtomicReference<Exception> firstFailure = new AtomicReference<>();
        AtomicReference<Exception> secondFailure = new AtomicReference<>();

        runConcurrently(List.of(
                () -> attempt(() -> checkoutService.placeOrder(userId, idempotencyKey, request), firstResult,
                        firstFailure),
                () -> attempt(() -> checkoutService.placeOrder(userId, idempotencyKey, request), secondResult,
                        secondFailure)));

        assertThat(firstFailure.get()).isNull();
        assertThat(secondFailure.get()).isNull();
        assertThat(firstResult.get().getId()).isEqualTo(secondResult.get().getId());
        assertThat(orderRepository.findAllByUserIdOrderByPlacedAtDesc(userId, PageRequest.of(0, 10))
                .getTotalElements()).isEqualTo(1);
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(9);
    }

    @Test
    void theSameIdempotencyKeyWithADifferentPayloadIsRejected() {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(10);
        addToCart(userId, productId, 1);
        String idempotencyKey = UUID.randomUUID().toString();
        checkoutService.placeOrder(userId, idempotencyKey, placeOrderRequest());

        DeliveryDetailsRequest differentDeliveryDetails = new DeliveryDetailsRequest("Tran Thi B", DEFAULT_PHONE,
                "2 Nguyen Hue", "District 1", "Ho Chi Minh City", null);
        PlaceOrderRequest differentRequest = new PlaceOrderRequest(differentDeliveryDetails, DeliveryWindow.STANDARD,
                PaymentMethod.CASH_ON_DELIVERY);

        AtomicReference<Order> result = new AtomicReference<>();
        AtomicReference<Exception> failure = new AtomicReference<>();
        attempt(() -> checkoutService.placeOrder(userId, idempotencyKey, differentRequest), result, failure);

        assertThat(failure.get()).isInstanceOf(IdempotencyKeyConflictException.class);
    }

    @Test
    void twoUsersRacingForTheLastUnitLeavesExactlyOneWinnerAndZeroStock() throws InterruptedException {
        UUID productId = seedProductWithStock(1);
        UUID userA = persistUser();
        UUID userB = persistUser();
        addToCart(userA, productId, 1);
        addToCart(userB, productId, 1);
        AtomicReference<Order> resultA = new AtomicReference<>();
        AtomicReference<Order> resultB = new AtomicReference<>();
        AtomicReference<Exception> failureA = new AtomicReference<>();
        AtomicReference<Exception> failureB = new AtomicReference<>();

        runConcurrently(List.of(
                () -> attempt(
                        () -> checkoutService.placeOrder(userA, UUID.randomUUID().toString(), placeOrderRequest()),
                        resultA, failureA),
                () -> attempt(
                        () -> checkoutService.placeOrder(userB, UUID.randomUUID().toString(), placeOrderRequest()),
                        resultB, failureB)));

        boolean aWon = resultA.get() != null;
        boolean bWon = resultB.get() != null;
        assertThat(aWon ^ bWon).as("exactly one of the two racing users wins").isTrue();
        Exception loserFailure = aWon ? failureB.get() : failureA.get();
        assertThat(loserFailure).isInstanceOf(InsufficientStockException.class);
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(0);
        assertThat(orderRepository.findAllByUserIdOrderByPlacedAtDesc(userA, PageRequest.of(0, 10))
                .getTotalElements() + orderRepository
                        .findAllByUserIdOrderByPlacedAtDesc(userB, PageRequest.of(0, 10)).getTotalElements())
                .isEqualTo(1);
    }

    @Test
    void tenConcurrentCheckoutsOfDistinctProductsAllSucceedWithDistinctOrderNumbers() throws InterruptedException {
        int count = 10;
        List<UUID> userIds = IntStream.range(0, count).mapToObj(i -> persistUser()).collect(Collectors.toList());
        List<UUID> productIds = IntStream.range(0, count).mapToObj(i -> seedProductWithStock(5))
                .collect(Collectors.toList());
        for (int i = 0; i < count; i++) {
            addToCart(userIds.get(i), productIds.get(i), 1);
        }
        List<AtomicReference<Order>> results = IntStream.range(0, count)
                .mapToObj(i -> new AtomicReference<Order>()).collect(Collectors.toList());
        List<AtomicReference<Exception>> failures = IntStream.range(0, count)
                .mapToObj(i -> new AtomicReference<Exception>()).collect(Collectors.toList());
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = i;
            tasks.add(() -> attempt(
                    () -> checkoutService.placeOrder(userIds.get(index), UUID.randomUUID().toString(),
                            placeOrderRequest()),
                    results.get(index), failures.get(index)));
        }

        runConcurrently(tasks);

        assertThat(failures).allSatisfy(failure -> assertThat(failure.get()).isNull());
        Set<String> orderNumbers = results.stream().map(AtomicReference::get).map(Order::getOrderNumber)
                .collect(Collectors.toSet());
        assertThat(orderNumbers).hasSize(count);
    }
}
