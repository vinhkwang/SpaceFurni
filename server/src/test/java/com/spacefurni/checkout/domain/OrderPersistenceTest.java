package com.spacefurni.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.exception.BusinessRuleViolationException;
import java.math.BigDecimal;
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
class OrderPersistenceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID persistUser() {
        User user = new User("user-" + UUID.randomUUID() + "@example.com", "hash", "Test User", UserRole.CUSTOMER);
        return entityManager.persistAndFlush(user).getId();
    }

    private UUID persistProduct() {
        Category category = categoryRepository.save(new Category(null, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Test Sofa", "test-sofa-" + UUID.randomUUID(),
                category, Money.ofVnd(1_000_000L), null, ProductStatus.PUBLISHED, "short", "long", "1x1x1cm",
                "Fabric", "Grey", new BigDecimal("4.0"), 0, false, false);
        return productRepository.saveAndFlush(product).getId();
    }

    private Order newOrder(UUID userId) {
        DeliveryDetails deliveryDetails = new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1",
                "Ho Chi Minh City", null);
        return new Order("SF-2419", userId, Money.ofVnd(1_000_000L), Money.ofVnd(300_000L), Money.zeroVnd(),
                Money.ofVnd(1_300_000L), null, deliveryDetails, DeliveryWindow.STANDARD, PaymentMethod.CARD);
    }

    @Test
    void persistsAndReloadsFourEmbeddedMoneyValuesSharingOneCurrencyColumn() {
        UUID userId = persistUser();
        Order order = newOrder(userId);

        UUID id = entityManager.persistAndFlush(order).getId();
        entityManager.clear();

        Order reloaded = entityManager.find(Order.class, id);

        assertThat(reloaded.getSubtotal().amount()).isEqualTo(1_000_000L);
        assertThat(reloaded.getShipping().amount()).isEqualTo(300_000L);
        assertThat(reloaded.getDiscount().amount()).isZero();
        assertThat(reloaded.getTotal().amount()).isEqualTo(1_300_000L);
        assertThat(reloaded.getSubtotal().currencyCode()).isEqualTo("VND");
        assertThat(reloaded.getShipping().currencyCode()).isEqualTo("VND");
        assertThat(reloaded.getDiscount().currencyCode()).isEqualTo("VND");
        assertThat(reloaded.getTotal().currencyCode()).isEqualTo("VND");
    }

    @Test
    void persistsAndReloadsDeliveryDetails() {
        UUID userId = persistUser();
        Order order = newOrder(userId);

        UUID id = entityManager.persistAndFlush(order).getId();
        entityManager.clear();

        Order reloaded = entityManager.find(Order.class, id);

        assertThat(reloaded.getDeliveryDetails().getFullName()).isEqualTo("Nguyen Van A");
        assertThat(reloaded.getDeliveryDetails().getDistrict()).isEqualTo("District 1");
        assertThat(reloaded.getDeliveryDetails().getNote()).isNull();
    }

    @Test
    void addItemMaintainsBothSidesOfTheAssociationAndCascadesOnPersist() {
        UUID userId = persistUser();
        UUID productId = persistProduct();
        Order order = newOrder(userId);
        OrderItem item = new OrderItem(productId, "Test Sofa", "SKU-1", 1_000_000L, 1, 1_000_000L);

        order.addItem(item);

        assertThat(item.getOrder()).isSameAs(order);
        assertThat(order.getItems()).contains(item);

        UUID id = entityManager.persistAndFlush(order).getId();
        entityManager.clear();

        Order reloaded = entityManager.find(Order.class, id);
        assertThat(reloaded.getItems()).hasSize(1);
        assertThat(reloaded.getItems().iterator().next().getProductNameSnapshot()).isEqualTo("Test Sofa");
    }

    @Test
    void transitionToMovesToAnAllowedStatus() {
        UUID userId = persistUser();
        Order order = newOrder(userId);

        order.transitionTo(OrderStatus.PAID);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void transitionToThrowsOnAnIllegalTransition() {
        UUID userId = persistUser();
        Order order = newOrder(userId);

        assertThatThrownBy(() -> order.transitionTo(OrderStatus.DELIVERED))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
}
