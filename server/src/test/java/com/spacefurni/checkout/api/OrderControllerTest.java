package com.spacefurni.checkout.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.checkout.api.dto.OrderResponse;
import com.spacefurni.checkout.api.dto.OrderSummaryResponse;
import com.spacefurni.checkout.api.mapper.OrderResponseMapper;
import com.spacefurni.checkout.application.CheckoutService;
import com.spacefurni.checkout.application.OrderQueryService;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.Order;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.domain.PaymentStatus;
import com.spacefurni.identity.application.CurrentUserQueryService;
import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.security.JwtTokenProvider;
import com.spacefurni.identity.security.SecurityConfiguration;
import com.spacefurni.identity.security.SpaceFurniUserDetailsService;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
class OrderControllerTest {

    private static final String EMAIL = "user@spacefurni.com";
    private static final String PLACE_ORDER_BODY = "{"
            + "\"deliveryDetails\": {\"fullName\": \"Nguyen Van A\", \"phone\": \"0901234567\","
            + " \"street\": \"1 Le Loi\", \"district\": \"District 1\", \"city\": \"Ho Chi Minh City\"},"
            + "\"deliveryWindow\": \"STANDARD\", \"paymentMethod\": \"CASH_ON_DELIVERY\"}";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvcWithSecurity() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @MockitoBean
    private CheckoutService checkoutService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @MockitoBean
    private OrderResponseMapper orderResponseMapper;

    @MockitoBean
    private CurrentUserQueryService currentUserQueryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SpaceFurniUserDetailsService userDetailsService;

    private UUID stubUser() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(currentUserQueryService.getByEmail(EMAIL)).thenReturn(user);
        return userId;
    }

    private OrderResponse dummyOrderResponse() {
        return new OrderResponse(UUID.randomUUID(), "SF-1001", OrderStatus.PENDING, 1_000_000L, 300_000L, 0L,
                1_300_000L, "VND", null, new OrderResponse.DeliveryDetailsResponse("Nguyen Van A", "0901234567",
                        "1 Le Loi", "District 1", "Ho Chi Minh City", null),
                DeliveryWindow.STANDARD, PaymentMethod.CASH_ON_DELIVERY, PaymentStatus.PENDING, Instant.now(),
                List.of());
    }

    @Test
    void placeOrderIsRejectedWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/orders").with(csrf()).header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON).content(PLACE_ORDER_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void placeOrderRejectsAMissingIdempotencyKeyHeader() throws Exception {
        mockMvc.perform(post("/api/v1/orders").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(PLACE_ORDER_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));

        verifyNoInteractions(checkoutService);
    }

    @Test
    @WithMockUser(username = EMAIL)
    void placeOrderDelegatesToCheckoutServiceWithTheGivenIdempotencyKey() throws Exception {
        UUID userId = stubUser();
        String idempotencyKey = UUID.randomUUID().toString();
        Order order = mock(Order.class);
        when(checkoutService.placeOrder(eq(userId), eq(idempotencyKey), any())).thenReturn(order);
        when(orderResponseMapper.toResponse(order)).thenReturn(dummyOrderResponse());

        mockMvc.perform(post("/api/v1/orders").with(csrf()).header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON).content(PLACE_ORDER_BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.orderNumber").value("SF-1001"));

        verify(checkoutService).placeOrder(eq(userId), eq(idempotencyKey), any());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void orderHistoryDelegatesToQueryServiceForTheCurrentUser() throws Exception {
        UUID userId = stubUser();
        OrderSummaryResponse summary = new OrderSummaryResponse(UUID.randomUUID(), "SF-1001", OrderStatus.PENDING,
                1_300_000L, "VND", 1, Instant.now());
        when(orderQueryService.findOrderHistory(eq(userId), any())).thenReturn(new PageImpl<>(List.of(summary)));

        mockMvc.perform(get("/api/v1/orders")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("SF-1001"));

        verify(orderQueryService).findOrderHistory(eq(userId), any());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void orderDetailDelegatesToQueryServiceForTheGivenOrderNumber() throws Exception {
        UUID userId = stubUser();
        when(orderQueryService.findOrderDetail(userId, "SF-1001")).thenReturn(dummyOrderResponse());

        mockMvc.perform(get("/api/v1/orders/SF-1001")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNumber").value("SF-1001"));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void orderDetailReturnsNotFoundRatherThanForbiddenForAnotherUsersOrder() throws Exception {
        UUID userId = stubUser();
        when(orderQueryService.findOrderDetail(userId, "SF-9999"))
                .thenThrow(new ResourceNotFoundException("Order not found: SF-9999"));

        mockMvc.perform(get("/api/v1/orders/SF-9999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void orderHistoryIsRejectedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/orders")).andExpect(status().isUnauthorized());
    }
}
