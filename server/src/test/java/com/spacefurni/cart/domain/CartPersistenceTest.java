package com.spacefurni.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
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
class CartPersistenceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID persistProduct() {
        Category category = categoryRepository.save(new Category(null, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Test Sofa", "test-sofa-" + UUID.randomUUID(),
                category, Money.ofVnd(1_000_000L), null, ProductStatus.PUBLISHED, "short", "long", "1x1x1cm",
                "Fabric", "Grey", new BigDecimal("4.0"), 0, false, false);
        return entityManager.persistAndFlush(product).getId();
    }

    @Test
    void persistsAndReloadsGuestCart() {
        Cart cart = new Cart(null, UUID.randomUUID());

        UUID id = entityManager.persistAndFlush(cart).getId();
        entityManager.clear();

        Cart reloaded = entityManager.find(Cart.class, id);

        assertThat(reloaded.getUserId()).isNull();
        assertThat(reloaded.getGuestToken()).isNotNull();
        assertThat(reloaded.getStatus()).isEqualTo(CartStatus.ACTIVE);
    }

    @Test
    void addOrIncrementLineCreatesNewLineForUnseenProduct() {
        UUID productId = persistProduct();
        Cart cart = new Cart(null, UUID.randomUUID());
        cart.addOrIncrementLine(productId, 2, null);

        UUID id = entityManager.persistAndFlush(cart).getId();
        entityManager.clear();

        Cart reloaded = entityManager.find(Cart.class, id);

        assertThat(reloaded.getItems()).hasSize(1);
        assertThat(reloaded.findLineByProductId(productId)).isPresent()
                .get().extracting(CartItem::getQuantity).isEqualTo(2);
    }

    @Test
    void addOrIncrementLineUpsertsRatherThanDuplicatingExistingLine() {
        UUID productId = persistProduct();
        Cart cart = new Cart(null, UUID.randomUUID());
        cart.addOrIncrementLine(productId, 2, null);
        entityManager.persistAndFlush(cart);

        cart.addOrIncrementLine(productId, 3, null);
        UUID id = entityManager.persistAndFlush(cart).getId();
        entityManager.clear();

        Cart reloaded = entityManager.find(Cart.class, id);

        assertThat(reloaded.getItems()).hasSize(1);
        assertThat(reloaded.findLineByProductId(productId)).isPresent()
                .get().extracting(CartItem::getQuantity).isEqualTo(5);
    }
}
