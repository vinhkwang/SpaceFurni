package com.spacefurni.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.api.mapper.ProductResponseMapper;
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
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class ProductRecommendationQueryServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private ProductRecommendationQueryService service() {
        return new ProductRecommendationQueryService(productRepository, new ProductResponseMapper());
    }

    private Product persistProduct(String label, ProductStatus status, Category category) {
        Product product = new Product("SKU-" + UUID.randomUUID(), label, "slug-" + UUID.randomUUID(), category,
                Money.ofVnd(1_000_000L), null, status, "short", "long", "dims", "material", "Beige",
                BigDecimal.ZERO, 0, false, false);
        return productRepository.save(product);
    }

    private void persistOrder(UUID userId, UUID... productIds) {
        DeliveryDetails deliveryDetails =
                new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1", "Ho Chi Minh City", null);
        Order order = new Order("SF-" + UUID.randomUUID().toString().substring(0, 8), userId, Money.ofVnd(1_000_000L),
                Money.ofVnd(300_000L), Money.zeroVnd(), Money.ofVnd(1_300_000L), null, deliveryDetails,
                DeliveryWindow.STANDARD, PaymentMethod.CARD);
        for (UUID productId : productIds) {
            order.addItem(new OrderItem(productId, "Snapshot", "SKU-1", 1_000_000L, 1, 1_000_000L));
        }
        orderRepository.save(order);
    }

    @Test
    void ranksCoPurchasedProductsByOccurrenceCountDescending() {
        Category category =
                categoryRepository.save(new Category(null, "Rank category", "cat-" + UUID.randomUUID(), null, 1));
        Product productA = persistProduct("Product A", ProductStatus.PUBLISHED, category);
        Product productB = persistProduct("Product B", ProductStatus.PUBLISHED, category);
        Product productC = persistProduct("Product C", ProductStatus.PUBLISHED, category);
        Product unrelated = persistProduct("Unrelated", ProductStatus.PUBLISHED, category);
        UUID userId = userRepository
                .save(new User("rank-" + UUID.randomUUID() + "@example.com", "hash", "Rank User", UserRole.CUSTOMER))
                .getId();

        persistOrder(userId, productA.getId(), productB.getId());
        persistOrder(userId, productA.getId(), productB.getId());
        persistOrder(userId, productA.getId(), productB.getId());
        persistOrder(userId, productA.getId(), productC.getId());
        persistOrder(userId, unrelated.getId());

        List<ProductSummaryResponse> results = service().recommendationsFor(productA.getId(), 8);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).id()).isEqualTo(productB.getId());
        assertThat(results.get(1).id()).isEqualTo(productC.getId());
    }

    @Test
    void excludesArchivedCoPurchasedProducts() {
        Category category = categoryRepository
                .save(new Category(null, "Archived category", "cat-" + UUID.randomUUID(), null, 1));
        Product viewed = persistProduct("Viewed", ProductStatus.PUBLISHED, category);
        Product archivedPartner = persistProduct("Archived Partner", ProductStatus.ARCHIVED, category);
        UUID userId = userRepository.save(
                new User("archived-" + UUID.randomUUID() + "@example.com", "hash", "Archived User",
                        UserRole.CUSTOMER))
                .getId();

        persistOrder(userId, viewed.getId(), archivedPartner.getId());

        List<ProductSummaryResponse> results = service().recommendationsFor(viewed.getId(), 8);

        assertThat(results).isEmpty();
    }

    @Test
    void returnsEmptyListWhenProductNeverCoPurchased() {
        Category category = categoryRepository
                .save(new Category(null, "Lonely category", "cat-" + UUID.randomUUID(), null, 1));
        Product neverCoPurchased = persistProduct("Never Co-purchased", ProductStatus.PUBLISHED, category);

        List<ProductSummaryResponse> results = service().recommendationsFor(neverCoPurchased.getId(), 8);

        assertThat(results).isEmpty();
    }
}
