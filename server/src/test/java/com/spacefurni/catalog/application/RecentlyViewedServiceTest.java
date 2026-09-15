package com.spacefurni.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.domain.RecentlyViewedProduct;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.catalog.infrastructure.RecentlyViewedProductRepository;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class RecentlyViewedServiceTest {

    @Autowired
    private RecentlyViewedProductRepository recentlyViewedProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private RecentlyViewedService service() {
        return new RecentlyViewedService(recentlyViewedProductRepository);
    }

    private Product persistProduct(Category category) {
        Product product = new Product("SKU-" + UUID.randomUUID(), "Product", "slug-" + UUID.randomUUID(), category,
                Money.ofVnd(1_000_000L), null, ProductStatus.PUBLISHED, "short", "long", "dims", "material",
                "Beige", BigDecimal.ZERO, 0, false, false);
        return productRepository.save(product);
    }

    private Category persistCategory() {
        return categoryRepository.save(new Category(null, "Category", "cat-" + UUID.randomUUID(), null, 1));
    }

    @Test
    void repeatedViewOfSameProductUpdatesTimestampWithoutDuplicatingRow() throws InterruptedException {
        Category category = persistCategory();
        Product product = persistProduct(category);
        UUID guestToken = UUID.randomUUID();
        RecentlyViewedService service = service();

        service.recordView(null, guestToken, product.getId());
        List<RecentlyViewedProduct> firstView = recentlyViewedProductRepository
                .findByGuestTokenOrderByViewedAtDesc(guestToken, PageRequest.of(0, 20));
        assertThat(firstView).hasSize(1);
        Instant firstViewedAt = firstView.get(0).getViewedAt();

        Thread.sleep(5);
        service.recordView(null, guestToken, product.getId());
        List<RecentlyViewedProduct> secondView = recentlyViewedProductRepository
                .findByGuestTokenOrderByViewedAtDesc(guestToken, PageRequest.of(0, 20));
        assertThat(secondView).hasSize(1);
        assertThat(secondView.get(0).getId()).isEqualTo(firstView.get(0).getId());
        assertThat(secondView.get(0).getViewedAt()).isAfter(firstViewedAt);
    }

    @Test
    void viewingAThirteenthProductEvictsTheOldest() {
        Category category = persistCategory();
        List<UUID> productIds = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            productIds.add(persistProduct(category).getId());
        }
        UUID guestToken = UUID.randomUUID();
        RecentlyViewedService service = service();

        for (UUID productId : productIds) {
            service.recordView(null, guestToken, productId);
        }

        List<RecentlyViewedProduct> remainingViews = recentlyViewedProductRepository
                .findByGuestTokenOrderByViewedAtDesc(guestToken, PageRequest.of(0, 20));
        assertThat(remainingViews).hasSize(12);
        List<UUID> remainingProductIds = remainingViews.stream().map(RecentlyViewedProduct::getProductId).toList();
        assertThat(remainingProductIds).doesNotContain(productIds.get(0));
        assertThat(remainingProductIds).containsExactlyInAnyOrderElementsOf(productIds.subList(1, 13));
    }

    @Test
    void viewsForDifferentUsersAndGuestsAreTrackedIndependently() {
        Category category = persistCategory();
        Product product = persistProduct(category);
        UUID userId = userRepository
                .save(new User("independent-" + UUID.randomUUID() + "@example.com", "hash", "Independent User",
                        UserRole.CUSTOMER))
                .getId();
        UUID guestToken = UUID.randomUUID();
        RecentlyViewedService service = service();

        service.recordView(userId, null, product.getId());
        service.recordView(null, guestToken, product.getId());

        assertThat(recentlyViewedProductRepository.findByUserIdOrderByViewedAtDesc(userId, PageRequest.of(0, 20)))
                .hasSize(1);
        assertThat(recentlyViewedProductRepository.findByGuestTokenOrderByViewedAtDesc(guestToken,
                PageRequest.of(0, 20))).hasSize(1);
    }
}
