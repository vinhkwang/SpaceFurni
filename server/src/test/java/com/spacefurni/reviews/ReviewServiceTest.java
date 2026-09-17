package com.spacefurni.reviews;

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
import com.spacefurni.reviews.application.ReviewService;
import com.spacefurni.reviews.domain.InvalidReviewException;
import com.spacefurni.reviews.domain.Review;
import com.spacefurni.reviews.infrastructure.ReviewRepository;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.support.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ReviewServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

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

    private UUID persistOrderItem(UUID userId, UUID productId, boolean delivered) {
        DeliveryDetails deliveryDetails =
                new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1", "Ho Chi Minh City", null);
        Order order = new Order("SF-" + System.nanoTime(), userId, Money.ofVnd(500_000L), Money.ofVnd(300_000L),
                Money.zeroVnd(), Money.ofVnd(800_000L), null, deliveryDetails, DeliveryWindow.STANDARD,
                PaymentMethod.CASH_ON_DELIVERY);
        order.addItem(new OrderItem(productId, "Scratch Sofa", "SKU-1", 500_000L, 1, 500_000L));
        orderRepository.saveAndFlush(order);
        if (delivered) {
            order.transitionTo(OrderStatus.PACKING);
            order.transitionTo(OrderStatus.DELIVERED);
            orderRepository.saveAndFlush(order);
        }
        return order.getItems().iterator().next().getId();
    }

    @Test
    void submitReviewSucceedsForEligibleDeliveredOrderItem() {
        UUID userId = persistUser();
        Product product = persistProduct();
        UUID orderItemId = persistOrderItem(userId, product.getId(), true);

        Review review = reviewService.submitReview(userId, orderItemId, (short) 5, "Great");

        assertThat(review.getId()).isNotNull();
        assertThat(reviewRepository.existsByOrderItemId(orderItemId)).isTrue();
    }

    @Test
    void submitReviewRejectsWrongUser() {
        UUID ownerId = persistUser();
        UUID otherUserId = persistUser();
        Product product = persistProduct();
        UUID orderItemId = persistOrderItem(ownerId, product.getId(), true);

        assertThatThrownBy(() -> reviewService.submitReview(otherUserId, orderItemId, (short) 4, "Not mine"))
                .isInstanceOf(InvalidReviewException.class);
        assertThat(reviewRepository.existsByOrderItemId(orderItemId)).isFalse();
    }

    @Test
    void submitReviewRejectsNonDeliveredOrder() {
        UUID userId = persistUser();
        Product product = persistProduct();
        UUID orderItemId = persistOrderItem(userId, product.getId(), false);

        assertThatThrownBy(() -> reviewService.submitReview(userId, orderItemId, (short) 3, "Too soon"))
                .isInstanceOf(InvalidReviewException.class);
        assertThat(reviewRepository.existsByOrderItemId(orderItemId)).isFalse();
    }

    @Test
    void submitReviewRejectsDuplicateReviewForSameOrderItem() {
        UUID userId = persistUser();
        Product product = persistProduct();
        UUID orderItemId = persistOrderItem(userId, product.getId(), true);
        reviewService.submitReview(userId, orderItemId, (short) 5, "First");

        assertThatThrownBy(() -> reviewService.submitReview(userId, orderItemId, (short) 4, "Second"))
                .isInstanceOf(InvalidReviewException.class);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getReviewCount()).isEqualTo(1);
    }
}
