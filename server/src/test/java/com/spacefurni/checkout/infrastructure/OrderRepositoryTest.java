package com.spacefurni.checkout.infrastructure;

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
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

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

    private Order newOrder(String orderNumber, UUID userId) {
        DeliveryDetails deliveryDetails = new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1",
                "Ho Chi Minh City", null);
        return new Order(orderNumber, userId, Money.ofVnd(1_000_000L), Money.ofVnd(300_000L), Money.zeroVnd(),
                Money.ofVnd(1_300_000L), null, deliveryDetails, DeliveryWindow.STANDARD, PaymentMethod.CARD);
    }

    @Test
    void findByOrderNumberFetchesItemsInOneStatement() {
        UUID userId = persistUser();
        UUID productId = persistProduct();
        Order order = newOrder("SF-3001", userId);
        order.addItem(new OrderItem(productId, "Test Sofa", "SKU-1", 1_000_000L, 1, 1_000_000L));
        entityManager.persistAndFlush(order);
        entityManager.clear();

        Statistics statistics = statistics();
        statistics.clear();

        Optional<Order> found = orderRepository.findByOrderNumber("SF-3001");

        assertThat(found).isPresent();
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void findByOrderNumberReturnsEmptyForUnknownNumber() {
        assertThat(orderRepository.findByOrderNumber("SF-DOES-NOT-EXIST")).isEmpty();
    }

    @Test
    void findAllByUserIdOrderByPlacedAtDescReturnsOnlyThatUsersOrdersMostRecentFirst() {
        UUID userA = persistUser();
        UUID userB = persistUser();
        Order first = entityManager.persistAndFlush(newOrder("SF-3002", userA));
        Order second = entityManager.persistAndFlush(newOrder("SF-3003", userA));
        entityManager.persistAndFlush(newOrder("SF-3004", userB));
        entityManager.clear();

        Page<Order> page = orderRepository.findAllByUserIdOrderByPlacedAtDesc(userA, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Order::getOrderNumber).containsExactly(second.getOrderNumber(),
                first.getOrderNumber());
    }

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }
}
