package com.spacefurni.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.checkout.api.dto.AdminOrderRowResponse;
import com.spacefurni.checkout.domain.DeliveryDetails;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderItem;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.infrastructure.OrderItemRepository;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
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
class AdminOrderQueryServiceTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private AdminOrderQueryService adminOrderQueryService;

    @BeforeEach
    void setUp() {
        adminOrderQueryService = new AdminOrderQueryService(orderRepository, orderItemRepository);
    }

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

    private Order persistOrderWithItems(String orderNumber, UUID userId, UUID productId, String customerName,
            int lineCount) {
        DeliveryDetails deliveryDetails =
                new DeliveryDetails(customerName, "0901234567", "1 Le Loi", "District 1", "Ho Chi Minh City", null);
        Order order = new Order(orderNumber, userId, Money.ofVnd(1_000_000L), Money.ofVnd(300_000L), Money.zeroVnd(),
                Money.ofVnd(1_300_000L), null, deliveryDetails, DeliveryWindow.STANDARD, PaymentMethod.CARD);
        for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            order.addItem(
                    new OrderItem(productId, "Test Sofa " + lineIndex, "SKU-" + lineIndex, 1_000_000L, 1, 1_000_000L));
        }
        return entityManager.persistAndFlush(order);
    }

    private void transitionOrderTo(Order order, OrderStatus target) {
        if (target == OrderStatus.DELIVERED) {
            order.transitionTo(OrderStatus.PACKING);
            order.transitionTo(OrderStatus.DELIVERED);
        } else if (target != OrderStatus.PENDING) {
            order.transitionTo(target);
        }
        entityManager.persistAndFlush(order);
    }

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    @Test
    void listOrdersIssuesAConstantNumberOfStatementsRegardlessOfPageSize() {
        UUID userId = persistUser();
        UUID productId = persistProduct();
        for (int orderIndex = 0; orderIndex < 6; orderIndex++) {
            persistOrderWithItems("SF-40" + orderIndex, userId, productId, "Customer " + orderIndex, 2);
        }
        entityManager.clear();

        Statistics statistics = statistics();
        statistics.clear();
        Page<AdminOrderRowResponse> smallPage = adminOrderQueryService.listOrders(null, null, PageRequest.of(0, 3));
        long statementsForSmallPage = statistics.getPrepareStatementCount();

        entityManager.clear();
        statistics.clear();
        Page<AdminOrderRowResponse> largePage = adminOrderQueryService.listOrders(null, null, PageRequest.of(0, 6));
        long statementsForLargePage = statistics.getPrepareStatementCount();

        assertThat(smallPage.getContent()).hasSize(3);
        assertThat(largePage.getContent()).hasSize(6);
        assertThat(statementsForLargePage).isEqualTo(statementsForSmallPage);
    }

    @Test
    void listOrdersMapsRowFieldsFromTheOrderAndItsItems() {
        UUID userId = persistUser();
        UUID productId = persistProduct();
        persistOrderWithItems("SF-4101", userId, productId, "Pham Thu Ha", 2);
        entityManager.clear();

        Page<AdminOrderRowResponse> page = adminOrderQueryService.listOrders(null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        AdminOrderRowResponse row = page.getContent().get(0);
        assertThat(row.orderNumber()).isEqualTo("SF-4101");
        assertThat(row.customerName()).isEqualTo("Pham Thu Ha");
        assertThat(row.district()).isEqualTo("District 1");
        assertThat(row.lineCount()).isEqualTo(2);
        assertThat(row.itemSummary()).isEqualTo("Test Sofa 0 +1 more");
        assertThat(row.paymentLabel()).isEqualTo("Card");
        assertThat(row.totalAmount()).isEqualTo(1_300_000L);
        assertThat(row.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void listOrdersFiltersByStatus() {
        UUID userId = persistUser();
        UUID productId = persistProduct();
        persistOrderWithItems("SF-4201", userId, productId, "Alice", 1);
        Order packingOrder = persistOrderWithItems("SF-4202", userId, productId, "Bob", 1);
        transitionOrderTo(packingOrder, OrderStatus.PACKING);
        entityManager.clear();

        Page<AdminOrderRowResponse> page =
                adminOrderQueryService.listOrders(OrderStatus.PACKING, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(AdminOrderRowResponse::orderNumber).containsExactly("SF-4202");
    }

    @Test
    void listOrdersSearchesByOrderNumberOrCustomerName() {
        UUID userId = persistUser();
        UUID productId = persistProduct();
        persistOrderWithItems("SF-4301", userId, productId, "Nguyen Minh Anh", 1);
        persistOrderWithItems("SF-4302", userId, productId, "Le Quang Duc", 1);
        entityManager.clear();

        Page<AdminOrderRowResponse> byOrderNumber =
                adminOrderQueryService.listOrders(null, "4301", PageRequest.of(0, 10));
        Page<AdminOrderRowResponse> byCustomerName =
                adminOrderQueryService.listOrders(null, "quang duc", PageRequest.of(0, 10));

        assertThat(byOrderNumber.getContent()).extracting(AdminOrderRowResponse::orderNumber)
                .containsExactly("SF-4301");
        assertThat(byCustomerName.getContent()).extracting(AdminOrderRowResponse::orderNumber)
                .containsExactly("SF-4302");
    }

    @Test
    void countOrdersByStatusGroupsAllStatusesInOneStatement() {
        UUID userId = persistUser();
        UUID productId = persistProduct();
        persistOrderWithItems("SF-4401", userId, productId, "Alice", 1);
        persistOrderWithItems("SF-4402", userId, productId, "Bob", 1);
        Order packingOrder = persistOrderWithItems("SF-4403", userId, productId, "Carol", 1);
        transitionOrderTo(packingOrder, OrderStatus.PACKING);
        entityManager.clear();

        Statistics statistics = statistics();
        statistics.clear();
        Map<OrderStatus, Long> counts = adminOrderQueryService.countOrdersByStatus();

        assertThat(counts.get(OrderStatus.PENDING)).isEqualTo(2L);
        assertThat(counts.get(OrderStatus.PACKING)).isEqualTo(1L);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }
}
