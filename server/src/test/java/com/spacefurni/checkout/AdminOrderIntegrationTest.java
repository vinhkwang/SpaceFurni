package com.spacefurni.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.infrastructure.OrderRepository;
import com.spacefurni.inventory.domain.InventoryItem;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.pricing.application.PricingLine;
import com.spacefurni.pricing.application.PricingService;
import com.spacefurni.pricing.domain.PriceBreakdown;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.ArrayList;
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
class AdminOrderIntegrationTest extends AbstractIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@spacefurni.dev";
    private static final String ADMIN_PASSWORD = "DevAdmin123!";

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

    private String adminAccessToken() throws Exception {
        JsonNode data = performAndReadData(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(("{ \"email\": \"%s\", \"password\": \"%s\" }").formatted(ADMIN_EMAIL, ADMIN_PASSWORD)));
        return data.get("accessToken").asString();
    }

    private String customerAccessToken() throws Exception {
        String email = "admin-orders-customer-" + UUID.randomUUID() + "@example.com";
        JsonNode data = performAndReadData(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(("{ \"email\": \"%s\", \"password\": \"password1\", \"fullName\": \"Customer Tester\" }")
                        .formatted(email)));
        return data.get("accessToken").asString();
    }

    private UUID seedProductWithStock(int quantityOnHand) {
        Category category =
                categoryRepository.save(new Category(null, "Zzq Sofa", "zzq-sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Zzq Admin Order Test Sofa",
                "zzq-admin-order-test-sofa-" + UUID.randomUUID(), category, Money.ofVnd(1_000_000L), null,
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

    private MockHttpServletRequestBuilder placeOrderRequest(String accessToken, DeliveryWindow deliveryWindow,
            String paymentMethod, String fullName) {
        String body = ("{ \"deliveryDetails\": { \"fullName\": \"%s\", \"phone\": \"0901234567\", "
                + "\"street\": \"1 Le Loi\", \"district\": \"District 1\", \"city\": \"Ho Chi Minh City\" }, "
                + "\"deliveryWindow\": \"%s\", \"paymentMethod\": \"%s\" }")
                .formatted(fullName, deliveryWindow, paymentMethod);
        return post("/api/v1/orders").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("Idempotency-Key", UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    private JsonNode placeOrderSuccessfully(String accessToken, UUID productId, int quantity,
            DeliveryWindow deliveryWindow, String paymentMethod, String fullName) throws Exception {
        addToCart(accessToken, productId, quantity);
        return performAndReadData(placeOrderRequest(accessToken, deliveryWindow, paymentMethod, fullName));
    }

    private JsonNode listOrders(String adminToken, String status, String q) throws Exception {
        MockHttpServletRequestBuilder request =
                get("/api/v1/admin/orders").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken);
        if (status != null) {
            request = request.param("status", status);
        }
        if (q != null) {
            request = request.param("q", q);
        }
        return performAndReadData(request);
    }

    private JsonNode getOrderDetail(String adminToken, String orderNumber) throws Exception {
        return performAndReadData(get("/api/v1/admin/orders/{orderNumber}", orderNumber)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken));
    }

    private MockHttpServletRequestBuilder transitionStatusRequest(String adminToken, String orderNumber,
            String targetStatus, long version) {
        return patch("/api/v1/admin/orders/{orderNumber}/status", orderNumber)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON)
                .content("{ \"status\": \"%s\", \"version\": %d }".formatted(targetStatus, version));
    }

    private long currentVersion(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).orElseThrow().getVersion();
    }

    private String shortUniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private List<Boolean> timelineCompletionFlags(JsonNode detail) {
        List<Boolean> flags = new ArrayList<>();
        detail.get("timeline").forEach(step -> flags.add(step.get("complete").asBoolean()));
        return flags;
    }

    @Test
    void listOrdersFiltersByStatus() throws Exception {
        String customerToken = customerAccessToken();
        String adminToken = adminAccessToken();
        UUID productId = seedProductWithStock(10);
        String uniqueCustomerName = "Zzq Status Filter " + shortUniqueSuffix();

        placeOrderSuccessfully(customerToken, productId, 1, DeliveryWindow.STANDARD, "CASH_ON_DELIVERY",
                uniqueCustomerName);
        JsonNode packingOrder = placeOrderSuccessfully(customerToken, productId, 1, DeliveryWindow.STANDARD,
                "CASH_ON_DELIVERY", uniqueCustomerName);
        String packingOrderNumber = packingOrder.get("orderNumber").asString();
        mockMvc.perform(transitionStatusRequest(adminToken, packingOrderNumber, "PACKING",
                        currentVersion(packingOrderNumber)))
                .andExpect(status().isOk());

        JsonNode result = listOrders(adminToken, "PACKING", uniqueCustomerName);

        JsonNode content = result.get("orders").get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("orderNumber").asString()).isEqualTo(packingOrderNumber);
    }

    @Test
    void searchMatchesOrderNumberAndCustomerName() throws Exception {
        String customerToken = customerAccessToken();
        String adminToken = adminAccessToken();
        UUID productId = seedProductWithStock(10);
        String uniqueCustomerName = "Zzq Search Customer " + shortUniqueSuffix();

        JsonNode placedOrder = placeOrderSuccessfully(customerToken, productId, 1, DeliveryWindow.STANDARD,
                "CASH_ON_DELIVERY", uniqueCustomerName);
        String orderNumber = placedOrder.get("orderNumber").asString();

        JsonNode byOrderNumber = listOrders(adminToken, null, orderNumber);
        assertThat(byOrderNumber.get("orders").get("content").get(0).get("orderNumber").asString())
                .isEqualTo(orderNumber);

        JsonNode byCustomerName = listOrders(adminToken, null, uniqueCustomerName);
        assertThat(byCustomerName.get("orders").get("content").get(0).get("orderNumber").asString())
                .isEqualTo(orderNumber);
    }

    @Test
    void detailReturnsCorrectLinesAndTotals() throws Exception {
        String customerToken = customerAccessToken();
        String adminToken = adminAccessToken();
        UUID productId = seedProductWithStock(10);

        JsonNode placedOrder = placeOrderSuccessfully(customerToken, productId, 3, DeliveryWindow.NEXT_DAY,
                "CASH_ON_DELIVERY", "Zzq Detail Customer");
        String orderNumber = placedOrder.get("orderNumber").asString();

        JsonNode detail = getOrderDetail(adminToken, orderNumber);

        PriceBreakdown expected = pricingService.calculate(List.of(new PricingLine(Money.ofVnd(1_000_000L), 3)),
                null, DeliveryWindow.NEXT_DAY);
        assertThat(detail.get("orderNumber").asString()).isEqualTo(orderNumber);
        assertThat(detail.get("customer").get("fullName").asString()).isEqualTo("Zzq Detail Customer");
        assertThat(detail.get("lines")).hasSize(1);
        assertThat(detail.get("lines").get(0).get("quantity").asInt()).isEqualTo(3);
        assertThat(detail.get("lines").get(0).get("lineTotalAmount").asLong()).isEqualTo(3_000_000L);
        assertThat(detail.get("subtotalAmount").asLong()).isEqualTo(expected.subtotal().amount());
        assertThat(detail.get("shippingAmount").asLong()).isEqualTo(expected.shipping().amount());
        assertThat(detail.get("totalAmount").asLong()).isEqualTo(expected.total().amount());
    }

    @Test
    void timelineCompletionMatchesStatus() throws Exception {
        String customerToken = customerAccessToken();
        String adminToken = adminAccessToken();
        UUID productId = seedProductWithStock(10);

        JsonNode placedOrder = placeOrderSuccessfully(customerToken, productId, 1, DeliveryWindow.STANDARD, "CARD",
                "Zzq Timeline Customer");
        String orderNumber = placedOrder.get("orderNumber").asString();

        JsonNode paidDetail = getOrderDetail(adminToken, orderNumber);
        assertThat(paidDetail.get("status").asString()).isEqualTo("PAID");
        assertThat(timelineCompletionFlags(paidDetail)).containsExactly(true, true, false, false, false);

        mockMvc.perform(
                        transitionStatusRequest(adminToken, orderNumber, "PACKING", currentVersion(orderNumber)))
                .andExpect(status().isOk());

        JsonNode packingDetail = getOrderDetail(adminToken, orderNumber);
        assertThat(timelineCompletionFlags(packingDetail)).containsExactly(true, true, true, false, false);
    }

    @Test
    void legalTransitionSucceedsAndAnIllegalTransitionIsRejected() throws Exception {
        String customerToken = customerAccessToken();
        String adminToken = adminAccessToken();
        UUID productId = seedProductWithStock(10);

        JsonNode placedOrder = placeOrderSuccessfully(customerToken, productId, 1, DeliveryWindow.STANDARD, "CARD",
                "Zzq Transition Customer");
        String orderNumber = placedOrder.get("orderNumber").asString();

        mockMvc.perform(
                        transitionStatusRequest(adminToken, orderNumber, "PACKING", currentVersion(orderNumber)))
                .andExpect(status().isOk());
        assertThat(getOrderDetail(adminToken, orderNumber).get("status").asString()).isEqualTo("PACKING");

        mockMvc.perform(
                        transitionStatusRequest(adminToken, orderNumber, "DELIVERED", currentVersion(orderNumber)))
                .andExpect(status().isOk());
        assertThat(getOrderDetail(adminToken, orderNumber).get("status").asString()).isEqualTo("DELIVERED");

        mockMvc.perform(
                        transitionStatusRequest(adminToken, orderNumber, "PENDING", currentVersion(orderNumber)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void staleVersionReturns409() throws Exception {
        String customerToken = customerAccessToken();
        String adminToken = adminAccessToken();
        UUID productId = seedProductWithStock(10);

        JsonNode placedOrder = placeOrderSuccessfully(customerToken, productId, 1, DeliveryWindow.STANDARD,
                "CASH_ON_DELIVERY", "Zzq Stale Version Customer");
        String orderNumber = placedOrder.get("orderNumber").asString();
        long staleVersion = currentVersion(orderNumber);

        mockMvc.perform(transitionStatusRequest(adminToken, orderNumber, "PACKING", staleVersion))
                .andExpect(status().isOk());

        mockMvc.perform(transitionStatusRequest(adminToken, orderNumber, "DELIVERED", staleVersion))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONCURRENT_MODIFICATION"));
    }

    @Test
    void cancellingReleasesReservedStock() throws Exception {
        String customerToken = customerAccessToken();
        String adminToken = adminAccessToken();
        UUID productId = seedProductWithStock(10);

        JsonNode placedOrder = placeOrderSuccessfully(customerToken, productId, 3, DeliveryWindow.STANDARD,
                "CASH_ON_DELIVERY", "Zzq Cancel Customer");
        String orderNumber = placedOrder.get("orderNumber").asString();
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(7);

        mockMvc.perform(
                        transitionStatusRequest(adminToken, orderNumber, "CANCELLED", currentVersion(orderNumber)))
                .andExpect(status().isOk());

        InventoryItem inventoryItem = inventoryItemRepository.findById(productId).orElseThrow();
        assertThat(inventoryItem.getQuantityOnHand()).isEqualTo(10);
        assertThat(inventoryItem.getQuantityReserved()).isEqualTo(0);
    }

    @Test
    void aCustomerTokenGets403OnEveryAdminOrderEndpoint() throws Exception {
        String customerToken = customerAccessToken();
        UUID productId = seedProductWithStock(10);
        JsonNode placedOrder = placeOrderSuccessfully(customerToken, productId, 1, DeliveryWindow.STANDARD,
                "CASH_ON_DELIVERY", "Zzq Forbidden Customer");
        String orderNumber = placedOrder.get("orderNumber").asString();

        mockMvc.perform(get("/api/v1/admin/orders").header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/orders/{orderNumber}", orderNumber)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(transitionStatusRequest(customerToken, orderNumber, "PACKING", 0))
                .andExpect(status().isForbidden());
    }

    private JsonNode performAndReadData(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }
}
