package com.spacefurni.cart.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartStatus;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
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

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class CartRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

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
        return entityManager.persistAndFlush(product).getId();
    }

    @Test
    void findByUserIdAndStatusFetchesItemsInOneStatement() {
        UUID userId = persistUser();
        Cart cart = new Cart(userId, null);
        cart.addOrIncrementLine(persistProduct(), 1, null);
        cart.addOrIncrementLine(persistProduct(), 2, null);
        entityManager.persistAndFlush(cart);
        entityManager.clear();

        Statistics statistics = statistics();
        statistics.clear();

        Optional<Cart> found = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE);

        assertThat(found).isPresent();
        assertThat(found.get().getItems()).hasSize(2);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void findByGuestTokenAndStatusFetchesItemsInOneStatement() {
        UUID guestToken = UUID.randomUUID();
        Cart cart = new Cart(null, guestToken);
        cart.addOrIncrementLine(persistProduct(), 1, null);
        entityManager.persistAndFlush(cart);
        entityManager.clear();

        Statistics statistics = statistics();
        statistics.clear();

        Optional<Cart> found = cartRepository.findByGuestTokenAndStatus(guestToken, CartStatus.ACTIVE);

        assertThat(found).isPresent();
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void findByUserIdAndStatusIgnoresCartsInOtherStatuses() {
        UUID userId = persistUser();
        Cart cart = new Cart(userId, null);
        entityManager.persistAndFlush(cart);
        entityManager.clear();

        Optional<Cart> found = cartRepository.findByUserIdAndStatus(userId, CartStatus.CONVERTED);

        assertThat(found).isEmpty();
    }

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }
}
