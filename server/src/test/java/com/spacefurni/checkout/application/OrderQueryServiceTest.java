package com.spacefurni.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.checkout.api.dto.OrderResponse;
import com.spacefurni.checkout.api.dto.OrderSummaryResponse;
import com.spacefurni.checkout.domain.DeliveryDetails;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderItem;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.identity.infrastructure.UserRepository;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class OrderQueryServiceTest extends AbstractIntegrationTest {

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private UUID persistUser() {
        User user = new User("user-" + UUID.randomUUID() + "@example.com", "hash", "Test User", UserRole.CUSTOMER);
        return userRepository.saveAndFlush(user).getId();
    }

    private UUID persistProduct() {
        Category category = categoryRepository
                .save(new Category(null, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Test Sofa", "test-sofa-" + UUID.randomUUID(),
                category, Money.ofVnd(500_000L), null, ProductStatus.DRAFT, "short", "long", "1x1x1cm", "Fabric",
                "Grey", new BigDecimal("4.0"), 0, false, false);
        return productRepository.saveAndFlush(product).getId();
    }

    private Order persistOrder(UUID userId, int itemCount) {
        DeliveryDetails deliveryDetails = new DeliveryDetails("Nguyen Van A", "0901234567", "1 Le Loi", "District 1",
                "Ho Chi Minh City", null);
        Order order = new Order("SF-" + System.nanoTime(), userId, Money.ofVnd(1_000_000L), Money.ofVnd(300_000L),
                Money.zeroVnd(), Money.ofVnd(1_300_000L), null, deliveryDetails, DeliveryWindow.STANDARD,
                PaymentMethod.CASH_ON_DELIVERY);
        for (int i = 0; i < itemCount; i++) {
            order.addItem(new OrderItem(persistProduct(), "Product " + i, "SKU-" + i, 500_000L, 1, 500_000L));
        }
        return orderRepository.saveAndFlush(order);
    }

    @Test
    void findOrderDetailReturnsTheOwnersOwnOrder() {
        UUID userId = persistUser();
        Order order = persistOrder(userId, 2);

        OrderResponse response = orderQueryService.findOrderDetail(userId, order.getOrderNumber());

        assertThat(response.orderNumber()).isEqualTo(order.getOrderNumber());
        assertThat(response.items()).hasSize(2);
        assertThat(response.totalAmount()).isEqualTo(1_300_000L);
        assertThat(response.deliveryDetails().city()).isEqualTo("Ho Chi Minh City");
    }

    @Test
    void findOrderDetailRejectsAnotherUsersOrderAsNotFoundRatherThanForbidden() {
        UUID ownerId = persistUser();
        UUID otherUserId = persistUser();
        Order order = persistOrder(ownerId, 1);

        assertThatThrownBy(() -> orderQueryService.findOrderDetail(otherUserId, order.getOrderNumber()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findOrderDetailThrowsForAnUnknownOrderNumber() {
        UUID userId = persistUser();

        assertThatThrownBy(() -> orderQueryService.findOrderDetail(userId, "SF-DOES-NOT-EXIST"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findOrderHistoryReturnsOnlyTheCallersOrdersNewestFirst() {
        UUID userId = persistUser();
        UUID otherUserId = persistUser();
        Order older = persistOrder(userId, 1);
        Order newer = persistOrder(userId, 1);
        persistOrder(otherUserId, 1);

        Page<OrderSummaryResponse> history = orderQueryService.findOrderHistory(userId, PageRequest.of(0, 10));

        assertThat(history.getContent()).extracting(OrderSummaryResponse::orderNumber)
                .containsExactly(newer.getOrderNumber(), older.getOrderNumber());
    }
}
