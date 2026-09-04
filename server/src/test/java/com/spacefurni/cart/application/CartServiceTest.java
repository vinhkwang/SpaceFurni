package com.spacefurni.cart.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartStatus;
import com.spacefurni.cart.infrastructure.CartRepository;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.inventory.domain.InsufficientStockException;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.pricing.application.DiscountStrategyFactory;
import com.spacefurni.pricing.application.PricingService;
import com.spacefurni.pricing.application.PromotionNotApplicableException;
import com.spacefurni.pricing.application.ShippingFeeStrategyResolver;
import com.spacefurni.pricing.domain.FixedAmountDiscountStrategy;
import com.spacefurni.pricing.domain.NextDayShippingFeeStrategy;
import com.spacefurni.pricing.domain.NoDiscountStrategy;
import com.spacefurni.pricing.domain.PercentageDiscountStrategy;
import com.spacefurni.pricing.domain.StandardShippingFeeStrategy;
import com.spacefurni.pricing.infrastructure.PromotionRepository;
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
class CartServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private CartService service() {
        PricingService pricingService = new PricingService(promotionRepository,
                new DiscountStrategyFactory(new PercentageDiscountStrategy(), new FixedAmountDiscountStrategy(),
                        new NoDiscountStrategy()),
                new ShippingFeeStrategyResolver(new StandardShippingFeeStrategy(), new NextDayShippingFeeStrategy()));
        return new CartService(cartRepository, new InventoryService(inventoryItemRepository), pricingService);
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
    void resolveOrCreateActiveCartCreatesGuestCartWhenNoneExists() {
        UUID guestToken = UUID.randomUUID();

        Cart cart = service().resolveOrCreateActiveCart(null, guestToken);

        assertThat(cart.getId()).isNotNull();
        assertThat(cart.getGuestToken()).isEqualTo(guestToken);
        assertThat(cart.getStatus()).isEqualTo(CartStatus.ACTIVE);
    }

    @Test
    void resolveOrCreateActiveCartReturnsExistingCartRatherThanCreatingAnother() {
        UUID guestToken = UUID.randomUUID();
        Cart first = service().resolveOrCreateActiveCart(null, guestToken);

        Cart second = service().resolveOrCreateActiveCart(null, guestToken);

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(cartRepository.findAll()).hasSize(1);
    }

    @Test
    void addLineAddsNewLineWithinAvailableStock() {
        UUID productId = seedProductWithStock(10);
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());

        Cart updated = service().addLine(cart, productId, 3, null);

        assertThat(updated.findLineByProductId(productId)).isPresent().get().extracting(line -> line.getQuantity())
                .isEqualTo(3);
    }

    @Test
    void addLineRejectsQuantityAboveAvailableStock() {
        UUID productId = seedProductWithStock(2);
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());

        assertThatThrownBy(() -> service().addLine(cart, productId, 5, null))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(cart.findLineByProductId(productId)).isEmpty();
    }

    @Test
    void addLineAccountsForQuantityAlreadyInCartWhenCheckingStock() {
        UUID productId = seedProductWithStock(5);
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());
        service().addLine(cart, productId, 4, null);

        assertThatThrownBy(() -> service().addLine(cart, productId, 2, null))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void addLineRejectsNonPositiveQuantity() {
        UUID productId = seedProductWithStock(5);
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());

        assertThatThrownBy(() -> service().addLine(cart, productId, 0, null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void updateLineQuantitySetsAbsoluteQuantity() {
        UUID productId = seedProductWithStock(10);
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());
        service().addLine(cart, productId, 2, null);

        Cart updated = service().updateLineQuantity(cart, productId, 7);

        assertThat(updated.findLineByProductId(productId)).isPresent().get().extracting(line -> line.getQuantity())
                .isEqualTo(7);
    }

    @Test
    void updateLineQuantityToZeroRemovesTheLine() {
        UUID productId = seedProductWithStock(10);
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());
        service().addLine(cart, productId, 2, null);

        Cart updated = service().updateLineQuantity(cart, productId, 0);

        assertThat(updated.findLineByProductId(productId)).isEmpty();
    }

    @Test
    void updateLineQuantityRejectsAmountAboveAvailableStock() {
        UUID productId = seedProductWithStock(3);
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());
        service().addLine(cart, productId, 2, null);

        assertThatThrownBy(() -> service().updateLineQuantity(cart, productId, 10))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void removeLineDeletesTheLine() {
        UUID productId = seedProductWithStock(10);
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());
        service().addLine(cart, productId, 2, null);

        Cart updated = service().removeLine(cart, productId);

        assertThat(updated.getItems()).isEmpty();
    }

    @Test
    void clearRemovesAllLines() {
        UUID first = seedProductWithStock(10);
        UUID second = seedProductWithStock(10);
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());
        service().addLine(cart, first, 1, null);
        service().addLine(cart, second, 1, null);

        Cart cleared = service().clear(cart);

        assertThat(cleared.getItems()).isEmpty();
    }

    @Test
    void applyPromotionUppercasesAndPersistsTheCode() {
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());

        Cart updated = service().applyPromotion(cart, "space10");

        assertThat(updated.getPromotionCode()).isEqualTo("SPACE10");
    }

    @Test
    void applyPromotionRejectsAnUnknownCodeWithoutPersistingIt() {
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());

        assertThatThrownBy(() -> service().applyPromotion(cart, "NOT-A-REAL-CODE"))
                .isInstanceOf(PromotionNotApplicableException.class);
        assertThat(cart.getPromotionCode()).isNull();
    }

    @Test
    void clearPromotionRemovesThePersistedCode() {
        Cart cart = service().resolveOrCreateActiveCart(null, UUID.randomUUID());
        service().applyPromotion(cart, "SPACE10");

        Cart cleared = service().clearPromotion(cart);

        assertThat(cleared.getPromotionCode()).isNull();
    }
}
