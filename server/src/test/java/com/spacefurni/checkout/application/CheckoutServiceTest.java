package com.spacefurni.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.cart.application.CartService;
import com.spacefurni.cart.domain.Cart;
import com.spacefurni.cart.domain.CartStatus;
import com.spacefurni.cart.infrastructure.CartRepository;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.checkout.api.dto.DeliveryDetailsRequest;
import com.spacefurni.checkout.api.dto.PlaceOrderRequest;
import com.spacefurni.checkout.domain.CardPaymentStrategy;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.domain.PaymentStatus;
import com.spacefurni.checkout.infrastructure.IdempotencyKeyRepository;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.checkout.infrastructure.PaymentRepository;
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
import org.springframework.data.domain.PageRequest;

class CheckoutServiceTest extends AbstractIntegrationTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

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
        return cartService.addLine(cart, productId, quantity);
    }

    private PlaceOrderRequest placeOrderRequest(DeliveryWindow deliveryWindow, PaymentMethod paymentMethod,
            String phone) {
        DeliveryDetailsRequest deliveryDetails = new DeliveryDetailsRequest("Nguyen Van A", phone, "1 Le Loi",
                "District 1", "Ho Chi Minh City", null);
        return new PlaceOrderRequest(deliveryDetails, deliveryWindow, paymentMethod);
    }

    @Test
    void placesACashOnDeliveryOrderAndConvertsTheCart() {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(10);
        UUID cartId = cartWithLine(userId, productId, 2).getId();
        PlaceOrderRequest request =
                placeOrderRequest(DeliveryWindow.STANDARD, PaymentMethod.CASH_ON_DELIVERY, "0901234567");

        Order order = checkoutService.placeOrder(userId, UUID.randomUUID().toString(), request);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().iterator().next().getLineTotalAmount()).isEqualTo(2_000_000L);
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(8);
        assertThat(cartRepository.findById(cartId).orElseThrow().getStatus()).isEqualTo(CartStatus.CONVERTED);
        assertThat(paymentRepository.findAll()).anyMatch(payment -> payment.getOrder().getId().equals(order.getId())
                && payment.getMethod() == PaymentMethod.CASH_ON_DELIVERY);
    }

    @Test
    void capturesACardPaymentAndMarksTheOrderPaid() {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(10);
        cartWithLine(userId, productId, 1);
        PlaceOrderRequest request = placeOrderRequest(DeliveryWindow.STANDARD, PaymentMethod.CARD, "0901234567");

        Order order = checkoutService.placeOrder(userId, UUID.randomUUID().toString(), request);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    @Test
    void rejectsCheckoutFromAnEmptyCart() {
        UUID userId = persistUser();
        cartService.resolveOrCreateActiveCart(userId, null);
        PlaceOrderRequest request = placeOrderRequest(DeliveryWindow.STANDARD, PaymentMethod.CARD, "0901234567");

        assertThatThrownBy(() -> checkoutService.placeOrder(userId, UUID.randomUUID().toString(), request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void aDeclinedCardPaymentRollsBackTheOrderAndTheStockReservation() {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(10);
        cartWithLine(userId, productId, 3);
        String idempotencyKey = UUID.randomUUID().toString();
        PlaceOrderRequest declinedRequest = placeOrderRequest(DeliveryWindow.STANDARD, PaymentMethod.CARD,
                CardPaymentStrategy.DECLINED_TEST_PHONE_NUMBER);

        assertThatThrownBy(() -> checkoutService.placeOrder(userId, idempotencyKey, declinedRequest))
                .isInstanceOf(PaymentFailedException.class);

        assertThat(orderRepository.findAllByUserIdOrderByPlacedAtDesc(userId, PageRequest.of(0, 10))).isEmpty();
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(10);
        assertThat(cartService.resolveOrCreateActiveCart(userId, null).getStatus()).isEqualTo(CartStatus.ACTIVE);
        assertThat(idempotencyKeyRepository.findById(idempotencyKey)).isEmpty();
    }

    @Test
    void retryingAfterARolledBackFailureWithTheSameKeySucceeds() {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(10);
        cartWithLine(userId, productId, 1);
        String idempotencyKey = UUID.randomUUID().toString();
        PlaceOrderRequest declinedRequest = placeOrderRequest(DeliveryWindow.STANDARD, PaymentMethod.CARD,
                CardPaymentStrategy.DECLINED_TEST_PHONE_NUMBER);
        assertThatThrownBy(() -> checkoutService.placeOrder(userId, idempotencyKey, declinedRequest))
                .isInstanceOf(PaymentFailedException.class);
        cartWithLine(userId, productId, 1);
        PlaceOrderRequest codRequest = placeOrderRequest(DeliveryWindow.STANDARD, PaymentMethod.CASH_ON_DELIVERY,
                "0901234567");

        Order order = checkoutService.placeOrder(userId, idempotencyKey, codRequest);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void replayingTheSameIdempotencyKeyReturnsTheOriginalOrderWithoutDecrementingStockAgain() {
        UUID userId = persistUser();
        UUID productId = seedProductWithStock(10);
        cartWithLine(userId, productId, 2);
        String idempotencyKey = UUID.randomUUID().toString();
        PlaceOrderRequest request =
                placeOrderRequest(DeliveryWindow.STANDARD, PaymentMethod.CASH_ON_DELIVERY, "0901234567");
        Order firstOrder = checkoutService.placeOrder(userId, idempotencyKey, request);

        Order replayedOrder = checkoutService.placeOrder(userId, idempotencyKey, request);

        assertThat(replayedOrder.getId()).isEqualTo(firstOrder.getId());
        assertThat(orderRepository.findAllByUserIdOrderByPlacedAtDesc(userId, PageRequest.of(0, 10)))
                .hasSize(1);
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(8);
    }
}
