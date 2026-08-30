package com.spacefurni.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.checkout.domain.CardPaymentStrategy;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.pricing.application.PricingLine;
import com.spacefurni.pricing.application.PricingService;
import com.spacefurni.pricing.domain.PriceBreakdown;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.exception.BusinessRuleViolationException;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class CheckoutIntegrationTest extends AbstractIntegrationTest {

    private static final String DEFAULT_PHONE = "0901234567";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PricingService pricingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String registerAndGetAccessToken() throws Exception {
        String email = "checkout-" + UUID.randomUUID() + "@example.com";
        JsonNode data = performAndReadData(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(("{ \"email\": \"%s\", \"password\": \"password1\", \"fullName\": \"Checkout Tester\" }")
                        .formatted(email)));
        return data.get("accessToken").asString();
    }

    private UUID seedProductWithStock(int quantityOnHand) {
        Category category = categoryRepository
                .save(new Category(null, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Checkout Test Sofa",
                "checkout-test-sofa-" + UUID.randomUUID(), category, Money.ofVnd(1_000_000L), null,
                ProductStatus.DRAFT, "short", "long", "1x1x1cm", "Fabric", "Grey", new BigDecimal("4.0"), 0, false,
                false);
        productRepository.saveAndFlush(product);
        inventoryItemRepository.saveAndFlush(new InventoryItem(product.getId(), quantityOnHand, 0));
        return product.getId();
    }

    private void addToCart(String accessToken, UUID productId, int quantity) throws Exception {
        mockMvc.perform(post("/api/v1/cart/items").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"productId\": \"%s\", \"quantity\": %d }".formatted(productId, quantity)))
                .andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder placeOrderRequest(String accessToken, String idempotencyKey,
            DeliveryWindow deliveryWindow, String paymentMethod, String phone) {
        String body = ("{ \"deliveryDetails\": { \"fullName\": \"Nguyen Van A\", \"phone\": \"%s\", "
                + "\"street\": \"1 Le Loi\", \"district\": \"District 1\", \"city\": \"Ho Chi Minh City\" }, "
                + "\"deliveryWindow\": \"%s\", \"paymentMethod\": \"%s\" }")
                .formatted(phone, deliveryWindow, paymentMethod);
        return post("/api/v1/orders").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("Idempotency-Key", idempotencyKey).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private JsonNode placeOrderSuccessfully(String accessToken, UUID productId, int quantity,
            DeliveryWindow deliveryWindow, String paymentMethod) throws Exception {
        addToCart(accessToken, productId, quantity);
        return performAndReadData(placeOrderRequest(accessToken, UUID.randomUUID().toString(), deliveryWindow,
                paymentMethod, DEFAULT_PHONE));
    }

    @Test
    void cashOnDeliveryHappyPathPlacesAPendingOrder() throws Exception {
        String accessToken = registerAndGetAccessToken();
        UUID productId = seedProductWithStock(10);

        JsonNode order = placeOrderSuccessfully(accessToken, productId, 2, DeliveryWindow.STANDARD,
                "CASH_ON_DELIVERY");

        assertThat(order.get("status").asString()).isEqualTo("PENDING");
        assertThat(order.get("paymentStatus").asString()).isEqualTo("PENDING");
        assertThat(order.get("paymentMethod").asString()).isEqualTo("CASH_ON_DELIVERY");
        assertThat(order.get("items").get(0).get("quantity").asInt()).isEqualTo(2);
    }

    @Test
    void bankTransferHappyPathPlacesAPendingOrder() throws Exception {
        String accessToken = registerAndGetAccessToken();
        UUID productId = seedProductWithStock(10);

        JsonNode order = placeOrderSuccessfully(accessToken, productId, 1, DeliveryWindow.STANDARD, "BANK_TRANSFER");

        assertThat(order.get("status").asString()).isEqualTo("PENDING");
        assertThat(order.get("paymentStatus").asString()).isEqualTo("PENDING");
        assertThat(order.get("paymentMethod").asString()).isEqualTo("BANK_TRANSFER");
    }

    @Test
    void cardHappyPathCapturesPaymentAndMarksTheOrderPaid() throws Exception {
        String accessToken = registerAndGetAccessToken();
        UUID productId = seedProductWithStock(10);

        JsonNode order = placeOrderSuccessfully(accessToken, productId, 1, DeliveryWindow.STANDARD, "CARD");

        assertThat(order.get("status").asString()).isEqualTo("PAID");
        assertThat(order.get("paymentStatus").asString()).isEqualTo("CAPTURED");
        assertThat(order.get("paymentMethod").asString()).isEqualTo("CARD");
    }

    @Test
    void totalsMatchThePricingServiceCalculation() throws Exception {
        String accessToken = registerAndGetAccessToken();
        UUID productId = seedProductWithStock(10);

        JsonNode order = placeOrderSuccessfully(accessToken, productId, 3, DeliveryWindow.NEXT_DAY,
                "CASH_ON_DELIVERY");

        PriceBreakdown expected = pricingService.calculate(List.of(new PricingLine(Money.ofVnd(1_000_000L), 3)),
                null, DeliveryWindow.NEXT_DAY);
        assertThat(order.get("subtotalAmount").asLong()).isEqualTo(expected.subtotal().amount());
        assertThat(order.get("shippingAmount").asLong()).isEqualTo(expected.shipping().amount());
        assertThat(order.get("discountAmount").asLong()).isEqualTo(expected.discount().amount());
        assertThat(order.get("totalAmount").asLong()).isEqualTo(expected.total().amount());
    }

    @Test
    void stockIsDecrementedByExactlyTheOrderedQuantity() throws Exception {
        String accessToken = registerAndGetAccessToken();
        UUID productId = seedProductWithStock(10);

        placeOrderSuccessfully(accessToken, productId, 4, DeliveryWindow.STANDARD, "CASH_ON_DELIVERY");

        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(6);
    }

    @Test
    void aDeclinedCardPaymentRollsBackTheOrderTheItemsAndTheStockReservation() throws Exception {
        String accessToken = registerAndGetAccessToken();
        UUID productId = seedProductWithStock(10);
        addToCart(accessToken, productId, 2);

        mockMvc.perform(placeOrderRequest(accessToken, UUID.randomUUID().toString(), DeliveryWindow.STANDARD, "CARD",
                        CardPaymentStrategy.DECLINED_TEST_PHONE_NUMBER))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("PAYMENT_FAILED"));

        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(10);
        JsonNode history = performAndReadData(get("/api/v1/orders").header(HttpHeaders.AUTHORIZATION,
                "Bearer " + accessToken));
        assertThat(history.get("content")).isEmpty();
    }

    @Test
    void placingAnOrderFromAnEmptyCartIsRejected() throws Exception {
        String accessToken = registerAndGetAccessToken();

        mockMvc.perform(placeOrderRequest(accessToken, UUID.randomUUID().toString(), DeliveryWindow.STANDARD,
                        "CASH_ON_DELIVERY", DEFAULT_PHONE))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void anOutOfStockLineIsRejectedWithTheOffendingProductIdentified() throws Exception {
        String accessToken = registerAndGetAccessToken();
        UUID productId = seedProductWithStock(5);
        addToCart(accessToken, productId, 5);

        String competingBuyerToken = registerAndGetAccessToken();
        placeOrderSuccessfully(competingBuyerToken, productId, 3, DeliveryWindow.STANDARD, "CASH_ON_DELIVERY");

        mockMvc.perform(placeOrderRequest(accessToken, UUID.randomUUID().toString(), DeliveryWindow.STANDARD,
                        "CASH_ON_DELIVERY", DEFAULT_PHONE))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error.code").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.error.message").value(containsString(productId.toString())));
    }

    @Test
    void anotherUsersOrderNumberReturnsNotFound() throws Exception {
        String ownerToken = registerAndGetAccessToken();
        String otherToken = registerAndGetAccessToken();
        UUID productId = seedProductWithStock(10);

        JsonNode order = placeOrderSuccessfully(ownerToken, productId, 1, DeliveryWindow.STANDARD,
                "CASH_ON_DELIVERY");
        String orderNumber = order.get("orderNumber").asString();

        mockMvc.perform(get("/api/v1/orders/" + orderNumber).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + otherToken))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void anIllegalOrderStatusTransitionIsRejected() throws Exception {
        String accessToken = registerAndGetAccessToken();
        UUID productId = seedProductWithStock(10);

        JsonNode data = placeOrderSuccessfully(accessToken, productId, 1, DeliveryWindow.STANDARD,
                "CASH_ON_DELIVERY");
        Order order = orderRepository.findByOrderNumber(data.get("orderNumber").asString()).orElseThrow();

        assertThatThrownBy(() -> order.transitionTo(OrderStatus.DELIVERED))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private JsonNode performAndReadData(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }
}
