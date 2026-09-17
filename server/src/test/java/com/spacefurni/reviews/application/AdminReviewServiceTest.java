package com.spacefurni.reviews.application;

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
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.reviews.domain.Review;
import com.spacefurni.reviews.domain.ReviewStatus;
import com.spacefurni.reviews.infrastructure.ReviewRepository;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class AdminReviewServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AdminReviewService adminReviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

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

    private UUID persistOrderItem(UUID userId, UUID productId) {
        DeliveryDetails deliveryDetails =
                new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1", "Ho Chi Minh City", null);
        Order order = new Order("SF-" + System.nanoTime(), userId, Money.ofVnd(500_000L), Money.ofVnd(300_000L),
                Money.zeroVnd(), Money.ofVnd(800_000L), null, deliveryDetails, DeliveryWindow.STANDARD,
                PaymentMethod.CASH_ON_DELIVERY);
        order.addItem(new OrderItem(productId, "Scratch Sofa", "SKU-1", 500_000L, 1, 500_000L));
        return orderRepository.saveAndFlush(order).getItems().iterator().next().getId();
    }

    @Test
    void listReviewsFiltersByStatusAndProduct() {
        UUID userId = persistUser();
        Product productOne = persistProduct();
        Product productTwo = persistProduct();
        reviewRepository.save(
                new Review(productOne.getId(), userId, persistOrderItem(userId, productOne.getId()), (short) 5,
                        "Great"));
        Review hiddenOnProductOne = reviewRepository.save(
                new Review(productOne.getId(), userId, persistOrderItem(userId, productOne.getId()), (short) 1,
                        "Bad"));
        hiddenOnProductOne.hide();
        reviewRepository.save(hiddenOnProductOne);
        reviewRepository.save(
                new Review(productTwo.getId(), userId, persistOrderItem(userId, productTwo.getId()), (short) 3,
                        "OK"));

        var publishedOnProductOne = adminReviewService.listReviews(ReviewStatus.PUBLISHED, productOne.getId(),
                PageRequest.of(0, 10));
        assertThat(publishedOnProductOne.getTotalElements()).isEqualTo(1);

        var allOnProductOne = adminReviewService.listReviews(null, productOne.getId(), PageRequest.of(0, 10));
        assertThat(allOnProductOne.getTotalElements()).isEqualTo(2);

        var allHidden = adminReviewService.listReviews(ReviewStatus.HIDDEN, null, PageRequest.of(0, 10));
        assertThat(allHidden.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateStatusHidingAndRestoringRecomputesTheProductAggregateAtomically() {
        UUID userId = persistUser();
        Product product = persistProduct();
        Review reviewOne = reviewRepository.save(
                new Review(product.getId(), userId, persistOrderItem(userId, product.getId()), (short) 5, "Great"));
        reviewRepository.save(
                new Review(product.getId(), userId, persistOrderItem(userId, product.getId()), (short) 1, "Bad"));

        adminReviewService.updateStatus(reviewOne.getId(), ReviewStatus.HIDDEN);
        Product afterHidingOneOfTwo = productRepository.findById(product.getId()).orElseThrow();
        assertThat(afterHidingOneOfTwo.getReviewCount()).isEqualTo(1);
        assertThat(afterHidingOneOfTwo.getRatingAverage()).isEqualByComparingTo(new BigDecimal("1.0"));
        assertThat(reviewRepository.findById(reviewOne.getId()).orElseThrow().getStatus())
                .isEqualTo(ReviewStatus.HIDDEN);

        adminReviewService.updateStatus(reviewOne.getId(), ReviewStatus.PUBLISHED);
        Product afterRestoring = productRepository.findById(product.getId()).orElseThrow();
        assertThat(afterRestoring.getReviewCount()).isEqualTo(2);
        assertThat(afterRestoring.getRatingAverage()).isEqualByComparingTo(new BigDecimal("3.0"));
    }
}
