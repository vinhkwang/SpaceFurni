package com.spacefurni.reviews;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.spacefurni.reviews.application.ReviewService;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.datasource.hikari.maximum-pool-size=60")
class ReviewConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private UUID persistUser() {
        return userRepository
                .saveAndFlush(new User("scratch-" + UUID.randomUUID() + "@example.com", "hash", "Scratch User",
                        UserRole.CUSTOMER))
                .getId();
    }

    private Product persistProduct() {
        Category category = categoryRepository
                .save(new Category(null, "Scratch Sofa", "scratch-sofa-" + UUID.randomUUID(), null, 1));
        return productRepository.saveAndFlush(new Product("SKU-" + UUID.randomUUID(), "Scratch Sofa",
                "scratch-test-sofa-" + UUID.randomUUID(), category, Money.ofVnd(500_000L), null,
                ProductStatus.PUBLISHED, "short", "long", "1x1x1cm", "Fabric", "Grey", null, 0, false, false));
    }

    private UUID persistDeliveredOrderItem(UUID userId, UUID productId) {
        DeliveryDetails deliveryDetails =
                new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1", "Ho Chi Minh City", null);
        Order order = new Order("SF-" + System.nanoTime(), userId, Money.ofVnd(500_000L), Money.ofVnd(300_000L),
                Money.zeroVnd(), Money.ofVnd(800_000L), null, deliveryDetails, DeliveryWindow.STANDARD,
                PaymentMethod.CASH_ON_DELIVERY);
        order.addItem(new OrderItem(productId, "Scratch Sofa", "SKU-1", 500_000L, 1, 500_000L));
        orderRepository.saveAndFlush(order);
        order.transitionTo(OrderStatus.PACKING);
        order.transitionTo(OrderStatus.DELIVERED);
        orderRepository.saveAndFlush(order);
        return order.getItems().iterator().next().getId();
    }

    @Test
    void concurrentReviewSubmissionsRecomputeTheRatingAggregateWithNoLostUpdates() throws InterruptedException {
        UUID userId = persistUser();
        Product product = persistProduct();
        reviewService.submitReview(userId, persistDeliveredOrderItem(userId, product.getId()), (short) 3,
                "Existing one");
        reviewService.submitReview(userId, persistDeliveredOrderItem(userId, product.getId()), (short) 3,
                "Existing two");

        int concurrentSubmissionCount = 20;
        List<UUID> orderItemIds = new ArrayList<>();
        for (int i = 0; i < concurrentSubmissionCount; i++) {
            orderItemIds.add(persistDeliveredOrderItem(userId, product.getId()));
        }

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(concurrentSubmissionCount);
        ExecutorService executor = Executors.newFixedThreadPool(concurrentSubmissionCount);
        for (int i = 0; i < concurrentSubmissionCount; i++) {
            UUID orderItemId = orderItemIds.get(i);
            short rating = (short) (i % 2 == 0 ? 5 : 1);
            executor.submit(() -> {
                try {
                    startLatch.await();
                    reviewService.submitReview(userId, orderItemId, rating, "Concurrent");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        assertThat(doneLatch.await(15, TimeUnit.SECONDS)).as("all submissions finished within timeout").isTrue();
        executor.shutdown();

        Product afterConcurrentSubmissions = productRepository.findById(product.getId()).orElseThrow();
        assertThat(afterConcurrentSubmissions.getReviewCount()).isEqualTo(2 + concurrentSubmissionCount);
        assertThat(afterConcurrentSubmissions.getRatingAverage()).isEqualByComparingTo(new BigDecimal("3.0"));
    }
}
