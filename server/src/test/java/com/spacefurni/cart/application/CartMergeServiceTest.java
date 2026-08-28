package com.spacefurni.cart.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartStatus;
import com.spacefurni.cart.infrastructure.CartRepository;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
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
class CartMergeServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TestEntityManager entityManager;

    private CartMergeService service() {
        return new CartMergeService(cartRepository, new InventoryService(inventoryItemRepository));
    }

    private UUID persistUser() {
        User user = new User("user-" + UUID.randomUUID() + "@example.com", "hash", "Test User", UserRole.CUSTOMER);
        return entityManager.persistAndFlush(user).getId();
    }

    private UUID seedProductWithStock(int quantityOnHand) {
        Category category = categoryRepository.save(new Category(null, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Test Sofa", "test-sofa-" + UUID.randomUUID(),
                category, Money.ofVnd(1_000_000L), null, ProductStatus.PUBLISHED, "short", "long", "1x1x1cm",
                "Fabric", "Grey", new BigDecimal("4.0"), 0, false, false);
        productRepository.saveAndFlush(product);
        entityManager.persistAndFlush(new InventoryItem(product.getId(), quantityOnHand, 0));
        return product.getId();
    }

    @Test
    void mergeSumsQuantitiesForSharedProductWhenWithinStock() {
        UUID productId = seedProductWithStock(10);
        UUID userId = persistUser();
        Cart guestCart = new Cart(null, UUID.randomUUID());
        guestCart.addOrIncrementLine(productId, 2);
        entityManager.persistAndFlush(guestCart);
        Cart userCart = new Cart(userId, null);
        userCart.addOrIncrementLine(productId, 1);
        entityManager.persistAndFlush(userCart);
        entityManager.clear();

        service().mergeGuestCartIntoUserCart(guestCart.getGuestToken(), userId);

        Cart reloadedUserCart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE).orElseThrow();
        assertThat(reloadedUserCart.findLineByProductId(productId)).isPresent().get()
                .extracting(line -> line.getQuantity()).isEqualTo(3);
    }

    @Test
    void mergeCapsMergedQuantityAtAvailableStock() {
        UUID productId = seedProductWithStock(2);
        UUID userId = persistUser();
        Cart guestCart = new Cart(null, UUID.randomUUID());
        guestCart.addOrIncrementLine(productId, 2);
        entityManager.persistAndFlush(guestCart);
        Cart userCart = new Cart(userId, null);
        userCart.addOrIncrementLine(productId, 1);
        entityManager.persistAndFlush(userCart);
        entityManager.clear();

        service().mergeGuestCartIntoUserCart(guestCart.getGuestToken(), userId);

        Cart reloadedUserCart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE).orElseThrow();
        assertThat(reloadedUserCart.findLineByProductId(productId)).isPresent().get()
                .extracting(line -> line.getQuantity()).isEqualTo(2);
    }

    @Test
    void mergeAddsProductOnlyPresentInGuestCart() {
        UUID productId = seedProductWithStock(10);
        UUID userId = persistUser();
        Cart guestCart = new Cart(null, UUID.randomUUID());
        guestCart.addOrIncrementLine(productId, 4);
        entityManager.persistAndFlush(guestCart);
        Cart userCart = new Cart(userId, null);
        entityManager.persistAndFlush(userCart);
        entityManager.clear();

        service().mergeGuestCartIntoUserCart(guestCart.getGuestToken(), userId);

        Cart reloadedUserCart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE).orElseThrow();
        assertThat(reloadedUserCart.findLineByProductId(productId)).isPresent().get()
                .extracting(line -> line.getQuantity()).isEqualTo(4);
    }

    @Test
    void mergeMarksGuestCartConverted() {
        UUID productId = seedProductWithStock(10);
        UUID userId = persistUser();
        Cart guestCart = new Cart(null, UUID.randomUUID());
        guestCart.addOrIncrementLine(productId, 1);
        UUID guestToken = guestCart.getGuestToken();
        UUID guestCartId = entityManager.persistAndFlush(guestCart).getId();
        entityManager.clear();

        service().mergeGuestCartIntoUserCart(guestToken, userId);
        entityManager.flush();
        entityManager.clear();

        Cart reloadedGuestCart = entityManager.find(Cart.class, guestCartId);
        assertThat(reloadedGuestCart.getStatus()).isEqualTo(CartStatus.CONVERTED);
    }

    @Test
    void mergeCreatesUserCartWhenNoneExists() {
        UUID productId = seedProductWithStock(10);
        UUID userId = persistUser();
        Cart guestCart = new Cart(null, UUID.randomUUID());
        guestCart.addOrIncrementLine(productId, 3);
        UUID guestToken = guestCart.getGuestToken();
        entityManager.persistAndFlush(guestCart);
        entityManager.clear();

        service().mergeGuestCartIntoUserCart(guestToken, userId);

        Cart userCart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE).orElseThrow();
        assertThat(userCart.findLineByProductId(productId)).isPresent().get().extracting(line -> line.getQuantity())
                .isEqualTo(3);
    }

    @Test
    void mergeIsNoOpWhenGuestTokenIsNull() {
        UUID userId = UUID.randomUUID();

        service().mergeGuestCartIntoUserCart(null, userId);

        assertThat(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).isEmpty();
    }

    @Test
    void mergeIsNoOpWhenNoActiveGuestCartExists() {
        UUID userId = UUID.randomUUID();

        service().mergeGuestCartIntoUserCart(UUID.randomUUID(), userId);

        assertThat(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).isEmpty();
    }
}
