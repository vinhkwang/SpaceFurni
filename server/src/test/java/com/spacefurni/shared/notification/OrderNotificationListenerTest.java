package com.spacefurni.shared.notification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.spacefurni.cart.application.CartService;
import com.spacefurni.cart.domain.Cart;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.checkout.api.dto.DeliveryDetailsRequest;
import com.spacefurni.checkout.api.dto.PlaceOrderRequest;
import com.spacefurni.checkout.application.CheckoutService;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.inventory.domain.InsufficientStockException;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class OrderNotificationListenerTest extends AbstractIntegrationTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private NotificationService notificationService;

    private UUID persistUser() {
        User user = new User("user-" + UUID.randomUUID() + "@example.com", "hash", "Test User", UserRole.CUSTOMER);
        return userRepository.saveAndFlush(user).getId();
    }

    private UUID seedProductWithStock(int quantityOnHand) {
        Category category = categoryRepository
                .save(new Category(null, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Test Sofa", "test-sofa-" + UUID.randomUUID(),
                category, Money.ofVnd(1_000_000L), null, ProductStatus.DRAFT, "short", "long", "1x1x1cm", "Fabric",
                "Grey", new BigDecimal("4.0"), 0, false, false);
        productRepository.saveAndFlush(product);
        inventoryItemRepository.saveAndFlush(new InventoryItem(product.getId(), quantityOnHand, 0));
        return product.getId();
    }

    private Cart cartWithLine(UUID userId, UUID productId, int quantity) {
        Cart cart = cartService.resolveOrCreateActiveCart(userId, null);
        return cartService.addLine(cart, productId, quantity, null);
    }

    private PlaceOrderRequest placeOrderRequest() {
        DeliveryDetailsRequest deliveryDetails = new DeliveryDetailsRequest("Nguyen Van A", "0901234567", "1 Le Loi",
                "District 1", "Ho Chi Minh City", null);
        return new PlaceOrderRequest(deliveryDetails, DeliveryWindow.STANDARD, PaymentMethod.CASH_ON_DELIVERY);
    }

    @Test
    void rolledBackCheckoutNeverTriggersANotification() {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(2);
        cartWithLine(userId, productId, 2);
        inventoryService.adjustQuantityOnHand(productId, -2);

        assertThatThrownBy(
                () -> checkoutService.placeOrder(userId, UUID.randomUUID().toString(), placeOrderRequest()))
                        .isInstanceOf(InsufficientStockException.class);

        verifyNoInteractions(notificationService);
    }

    @Test
    void successfulCheckoutTriggersExactlyOneOrderConfirmation() {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(5);
        cartWithLine(userId, productId, 2);

        Order order = checkoutService.placeOrder(userId, UUID.randomUUID().toString(), placeOrderRequest());

        verify(notificationService).sendOrderConfirmation(any(), eq(order.getOrderNumber()), any(), any());
    }
}
