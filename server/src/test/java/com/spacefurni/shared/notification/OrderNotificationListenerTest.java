package com.spacefurni.shared.notification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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
import com.spacefurni.checkout.application.AdminOrderService;
import com.spacefurni.checkout.application.CheckoutService;
import com.spacefurni.checkout.application.OrderCancellationService;
import com.spacefurni.checkout.domain.DeliveryDetails;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderItem;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.infrastructure.OrderRepository;
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

    @Autowired
    private AdminOrderService adminOrderService;

    @Autowired
    private OrderCancellationService orderCancellationService;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private NotificationService notificationService;

    private UUID persistUser() {
        User user = new User("user-" + UUID.randomUUID() + "@example.com", "hash", "Test User", UserRole.CUSTOMER);
        return userRepository.saveAndFlush(user).getId();
    }

    private String emailOf(UUID userId) {
        return userRepository.findById(userId).orElseThrow().getEmail();
    }

    private Order persistOrderAtStatus(UUID userId, UUID productId, OrderStatus status) {
        DeliveryDetails deliveryDetails = new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi",
                "District 1", "Ho Chi Minh City", null);
        String orderNumber = "SF-" + Math.abs(UUID.randomUUID().getLeastSignificantBits() % 1_000_000L);
        Order order = new Order(orderNumber, userId, Money.ofVnd(1_000_000L), Money.ofVnd(300_000L),
                Money.zeroVnd(), Money.ofVnd(1_300_000L), null, deliveryDetails, DeliveryWindow.STANDARD,
                PaymentMethod.CARD);
        order.addItem(new OrderItem(productId, "Test Sofa", "SKU-1", 1_000_000L, 1, 1_000_000L));
        advanceToStatus(order, status);
        return orderRepository.save(order);
    }

    private void advanceToStatus(Order order, OrderStatus target) {
        if (target == OrderStatus.PENDING) {
            return;
        }
        order.transitionTo(OrderStatus.PAID);
        if (target == OrderStatus.PAID) {
            return;
        }
        order.transitionTo(OrderStatus.PACKING);
    }

    private UUID seedProductWithStock(int quantityOnHand) {
        return seedProductWithReservedStock(quantityOnHand, 0);
    }

    private UUID seedProductWithReservedStock(int quantityOnHand, int quantityReserved) {
        Category category = categoryRepository
                .save(new Category(null, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Test Sofa", "test-sofa-" + UUID.randomUUID(),
                category, Money.ofVnd(1_000_000L), null, ProductStatus.DRAFT, "short", "long", "1x1x1cm", "Fabric",
                "Grey", new BigDecimal("4.0"), 0, false, false);
        productRepository.saveAndFlush(product);
        inventoryItemRepository.saveAndFlush(new InventoryItem(product.getId(), quantityOnHand, quantityReserved));
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

    @Test
    void adminTransitionFromPendingToPaidSendsCorrectStatusUpdateContent() {
        UUID userId = persistUser();
        String email = emailOf(userId);
        UUID productId = seedProductWithStock(10);
        Order order = persistOrderAtStatus(userId, productId, OrderStatus.PENDING);

        adminOrderService.transitionOrderStatus(order.getOrderNumber(), OrderStatus.PAID, order.getVersion());

        verify(notificationService).sendOrderStatusUpdate(email, order.getOrderNumber(), "PENDING", "PAID");
    }

    @Test
    void adminTransitionFromPendingToPackingSendsCorrectStatusUpdateContent() {
        UUID userId = persistUser();
        String email = emailOf(userId);
        UUID productId = seedProductWithStock(10);
        Order order = persistOrderAtStatus(userId, productId, OrderStatus.PENDING);

        adminOrderService.transitionOrderStatus(order.getOrderNumber(), OrderStatus.PACKING, order.getVersion());

        verify(notificationService).sendOrderStatusUpdate(email, order.getOrderNumber(), "PENDING", "PACKING");
    }

    @Test
    void adminTransitionFromPendingToCancelledSendsCorrectStatusUpdateContent() {
        UUID userId = persistUser();
        String email = emailOf(userId);
        UUID productId = seedProductWithReservedStock(10, 1);
        Order order = persistOrderAtStatus(userId, productId, OrderStatus.PENDING);

        adminOrderService.transitionOrderStatus(order.getOrderNumber(), OrderStatus.CANCELLED, order.getVersion());

        verify(notificationService).sendOrderStatusUpdate(email, order.getOrderNumber(), "PENDING", "CANCELLED");
    }

    @Test
    void adminTransitionFromPaidToPackingSendsCorrectStatusUpdateContent() {
        UUID userId = persistUser();
        String email = emailOf(userId);
        UUID productId = seedProductWithStock(10);
        Order order = persistOrderAtStatus(userId, productId, OrderStatus.PAID);

        adminOrderService.transitionOrderStatus(order.getOrderNumber(), OrderStatus.PACKING, order.getVersion());

        verify(notificationService).sendOrderStatusUpdate(email, order.getOrderNumber(), "PAID", "PACKING");
    }

    @Test
    void adminTransitionFromPaidToCancelledSendsCorrectStatusUpdateContent() {
        UUID userId = persistUser();
        String email = emailOf(userId);
        UUID productId = seedProductWithReservedStock(10, 1);
        Order order = persistOrderAtStatus(userId, productId, OrderStatus.PAID);

        adminOrderService.transitionOrderStatus(order.getOrderNumber(), OrderStatus.CANCELLED, order.getVersion());

        verify(notificationService).sendOrderStatusUpdate(email, order.getOrderNumber(), "PAID", "CANCELLED");
    }

    @Test
    void adminTransitionFromPackingToDeliveredSendsCorrectStatusUpdateContent() {
        UUID userId = persistUser();
        String email = emailOf(userId);
        UUID productId = seedProductWithStock(10);
        Order order = persistOrderAtStatus(userId, productId, OrderStatus.PACKING);

        adminOrderService.transitionOrderStatus(order.getOrderNumber(), OrderStatus.DELIVERED, order.getVersion());

        verify(notificationService).sendOrderStatusUpdate(email, order.getOrderNumber(), "PACKING", "DELIVERED");
    }

    @Test
    void customerCancellationSendsCorrectStatusUpdateContent() {
        UUID userId = persistUser();
        String email = emailOf(userId);
        UUID productId = seedProductWithReservedStock(10, 1);
        Order order = persistOrderAtStatus(userId, productId, OrderStatus.PAID);

        orderCancellationService.cancelOrder(userId, order.getId(), "Changed my mind");

        verify(notificationService).sendOrderStatusUpdate(email, order.getOrderNumber(), "PAID", "CANCELLED");
    }

    @Test
    void refundAfterCancellationDoesNotTriggerAnAdditionalStatusUpdate() {
        UUID userId = persistUser();
        String email = emailOf(userId);
        UUID productId = seedProductWithReservedStock(10, 1);
        Order order = persistOrderAtStatus(userId, productId, OrderStatus.PAID);

        orderCancellationService.cancelOrder(userId, order.getId(), "Changed my mind");
        adminOrderService.processRefund(order.getOrderNumber(), order.getTotal());

        verify(notificationService, times(1)).sendOrderStatusUpdate(email, order.getOrderNumber(), "PAID",
                "CANCELLED");
    }
}
