package com.spacefurni.checkout.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.checkout.api.dto.AdminOrderDetailResponse;
import com.spacefurni.checkout.api.dto.AdminOrderRowResponse;
import com.spacefurni.checkout.application.AdminOrderQueryService;
import com.spacefurni.checkout.application.AdminOrderService;
import com.spacefurni.checkout.domain.DeliveryWindow;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.checkout.domain.PaymentMethod;
import com.spacefurni.checkout.domain.PaymentStatus;
import com.spacefurni.identity.security.JwtTokenProvider;
import com.spacefurni.identity.security.SecurityConfiguration;
import com.spacefurni.identity.security.SpaceFurniUserDetailsService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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

@WebMvcTest(controllers = AdminOrderController.class)
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
class AdminOrderControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvcWithSecurity() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @MockitoBean
    private AdminOrderQueryService adminOrderQueryService;

    @MockitoBean
    private AdminOrderService adminOrderService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SpaceFurniUserDetailsService userDetailsService;

    @Test
    void listOrdersIsRejectedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listOrdersIsForbiddenForACustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listOrdersReturnsPagedRowsAndStatusCounts() throws Exception {
        AdminOrderRowResponse row = new AdminOrderRowResponse("SF-3001", "Nguyen Van A", "District 1", "Test Sofa",
                1, "Card", Instant.parse("2026-08-12T02:24:00Z"), 1_300_000L, "VND", OrderStatus.PACKING);
        when(adminOrderQueryService.listOrders(eq(OrderStatus.PACKING), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(row)));
        when(adminOrderQueryService.countOrdersByStatus()).thenReturn(Map.of(OrderStatus.PACKING, 1L));

        mockMvc.perform(get("/api/v1/admin/orders").param("status", "PACKING")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orders.content[0].orderNumber").value("SF-3001"))
                .andExpect(jsonPath("$.data.statusCounts.PACKING").value(1));

        verify(adminOrderQueryService).listOrders(eq(OrderStatus.PACKING), isNull(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getOrderDetailDelegatesToTheServiceForTheGivenOrderNumber() throws Exception {
        AdminOrderDetailResponse detail = new AdminOrderDetailResponse("SF-3001", OrderStatus.PACKING,
                new AdminOrderDetailResponse.CustomerResponse("Nguyen Van A", "a@example.com", "0901234567"),
                new AdminOrderDetailResponse.DeliveryAddressResponse("1 Le Loi", "District 1", "Ho Chi Minh City",
                        null),
                DeliveryWindow.STANDARD, PaymentMethod.CARD, PaymentStatus.CAPTURED, 1_000_000L, 300_000L, 0L,
                1_300_000L, "VND", Instant.parse("2026-08-12T02:24:00Z"), List.of(), List.of());
        when(adminOrderQueryService.findOrderDetail("SF-3001")).thenReturn(detail);

        mockMvc.perform(get("/api/v1/admin/orders/{orderNumber}", "SF-3001")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNumber").value("SF-3001"))
                .andExpect(jsonPath("$.data.customer.email").value("a@example.com"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getOrderDetailIsForbiddenForACustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders/{orderNumber}", "SF-3001")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void transitionStatusDelegatesToTheServiceWithTargetStatusAndVersion() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/orders/{orderNumber}/status", "SF-3001").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"PACKING\", \"version\": 3}"))
                .andExpect(status().isOk());

        verify(adminOrderService).transitionOrderStatus("SF-3001", OrderStatus.PACKING, 3L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void transitionStatusRejectsAMissingVersion() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/orders/{orderNumber}/status", "SF-3001").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"PACKING\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void transitionStatusIsForbiddenForACustomer() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/orders/{orderNumber}/status", "SF-3001").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"PACKING\", \"version\": 3}"))
                .andExpect(status().isForbidden());
    }
}
