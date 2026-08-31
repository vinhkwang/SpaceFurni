package com.spacefurni.catalog.api;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.catalog.application.AdminProductService;
import com.spacefurni.checkout.application.AdminOrderQueryService;
import com.spacefurni.checkout.domain.OrderStatus;
import com.spacefurni.identity.security.JwtTokenProvider;
import com.spacefurni.identity.security.SecurityConfiguration;
import com.spacefurni.identity.security.SpaceFurniUserDetailsService;
import com.spacefurni.inventory.application.InventoryService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = AdminSummaryController.class)
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
class AdminSummaryControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvcWithSecurity() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @MockitoBean
    private AdminProductService adminProductService;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private AdminOrderQueryService adminOrderQueryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SpaceFurniUserDetailsService userDetailsService;

    @Test
    void getSummaryIsRejectedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/summary")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getSummaryIsForbiddenForACustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/summary")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSummaryCombinesCountsFromAllThreeServices() throws Exception {
        when(adminProductService.countPublishedProducts()).thenReturn(42L);
        when(adminOrderQueryService.countOrdersPlacedToday()).thenReturn(5L);
        when(adminOrderQueryService.countOrdersByStatus()).thenReturn(Map.of(OrderStatus.PENDING, 3L));
        when(inventoryService.countLowStockItems()).thenReturn(2L);

        mockMvc.perform(get("/api/v1/admin/summary")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.publishedProductCount").value(42))
                .andExpect(jsonPath("$.data.ordersTodayCount").value(5))
                .andExpect(jsonPath("$.data.pendingOrdersCount").value(3))
                .andExpect(jsonPath("$.data.lowStockProductCount").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSummaryDefaultsPendingOrdersToZeroWhenNoPendingOrdersExist() throws Exception {
        when(adminProductService.countPublishedProducts()).thenReturn(10L);
        when(adminOrderQueryService.countOrdersPlacedToday()).thenReturn(0L);
        when(adminOrderQueryService.countOrdersByStatus()).thenReturn(Map.of(OrderStatus.DELIVERED, 4L));
        when(inventoryService.countLowStockItems()).thenReturn(0L);

        mockMvc.perform(get("/api/v1/admin/summary")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pendingOrdersCount").value(0));
    }
}
