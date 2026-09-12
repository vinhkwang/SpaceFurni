package com.spacefurni.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.domain.PaymentStatus;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.exception.BusinessRuleViolationException;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminRefundServiceTest extends AbstractIntegrationTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private AdminOrderService adminOrderService;

    @Autowired
    private OrderCancellationService orderCancellationService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

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

    private PlaceOrderRequest placeOrderRequest(PaymentMethod paymentMethod) {
        DeliveryDetailsRequest deliveryDetails = new DeliveryDetailsRequest("Nguyen Van A", "0901234567", "1 Le Loi",
                "District 1", "Ho Chi Minh City", null);
        return new PlaceOrderRequest(deliveryDetails, DeliveryWindow.STANDARD, paymentMethod);
    }

    private Order placeOrder(UUID userId, UUID productId, int quantity, PaymentMethod paymentMethod) {
        cartWithLine(userId, productId, quantity);
        return checkoutService.placeOrder(userId, UUID.randomUUID().toString(), placeOrderRequest(paymentMethod));
    }

    @Test
    void refundSucceedsOnACancelledOrderAndRecordsTheRefundedAmount() {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(10);
        Order order = placeOrder(userId, productId, 1, PaymentMethod.CARD);
        orderCancellationService.cancelOrder(userId, order.getId(), "Changed my mind");

        adminOrderService.processRefund(order.getOrderNumber(), order.getTotal());

        Order refundedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(refundedOrder.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(refundedOrder.getRefundedAmount()).isEqualTo(order.getTotal().amount());
    }

    @Test
    void refundIsRejectedOnANonCancelledNonReturnedOrder() {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(10);
        Order order = placeOrder(userId, productId, 1, PaymentMethod.CASH_ON_DELIVERY);

        assertThatThrownBy(() -> adminOrderService.processRefund(order.getOrderNumber(), order.getTotal()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
