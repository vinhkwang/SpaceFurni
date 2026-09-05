package com.spacefurni.catalog.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.catalog.api.dto.AdminProductDetailResponse;
import com.spacefurni.catalog.api.dto.AdminProductRowResponse;
import com.spacefurni.catalog.application.AdminProductService;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.identity.security.JwtTokenProvider;
import com.spacefurni.identity.security.SecurityConfiguration;
import com.spacefurni.identity.security.SpaceFurniUserDetailsService;
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

@WebMvcTest(controllers = AdminProductController.class)
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
class AdminProductControllerTest {

    private static final String VALID_PRODUCT_BODY = "{\"title\": \"Halden Tub Chair\","
            + " \"departmentSlug\": \"living-room\", \"subCategorySlug\": \"sofa\", \"price\": 7200000,"
            + " \"stock\": 12, \"status\": \"PUBLISHED\"}";

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
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SpaceFurniUserDetailsService userDetailsService;

    @Test
    void listProductsIsRejectedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listProductsIsForbiddenForACustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listProductsDelegatesToTheServiceWithTheSearchTerm() throws Exception {
        AdminProductRowResponse row = new AdminProductRowResponse(UUID.randomUUID(), null, "Halden Tub Chair",
                "LIV-0001", "Living room · Sofa", 7_200_000L, "VND", 12, ProductStatus.PUBLISHED);
        when(adminProductService.listProducts(eq("chair"), any())).thenReturn(new PageImpl<>(List.of(row)));

        mockMvc.perform(get("/api/v1/admin/products").param("q", "chair")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].sku").value("LIV-0001"));

        verify(adminProductService).listProducts(eq("chair"), any());
    }

    @Test
    void getProductIsRejectedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products/{id}", UUID.randomUUID())).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getProductIsForbiddenForACustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products/{id}", UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProductDelegatesToTheServiceForTheGivenId() throws Exception {
        UUID productId = UUID.randomUUID();
        AdminProductDetailResponse detail = new AdminProductDetailResponse(productId, "Halden Tub Chair",
                "living-room", "sofa", 7_200_000L, 12, "Short", "Long", "80x75x70cm", "Oak, linen", "Terracotta",
                "https://example.com/chair.jpg", ProductStatus.PUBLISHED, 0L);
        when(adminProductService.getProduct(productId)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/admin/products/{id}", productId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Halden Tub Chair"))
                .andExpect(jsonPath("$.data.departmentSlug").value("living-room"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProductDelegatesToTheServiceAndReturnsTheNewId() throws Exception {
        UUID productId = UUID.randomUUID();
        when(adminProductService.createProduct(any())).thenReturn(productId);

        mockMvc.perform(post("/api/v1/admin/products").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PRODUCT_BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(productId.toString()));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createProductIsForbiddenForACustomer() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PRODUCT_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProductRejectsAMissingTitle() throws Exception {
        String invalidBody = "{\"departmentSlug\": \"living-room\", \"price\": 7200000, \"stock\": 12}";

        mockMvc.perform(post("/api/v1/admin/products").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProductDelegatesToTheServiceForTheGivenId() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/admin/products/{id}", productId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT_BODY))
                .andExpect(status().isOk());

        verify(adminProductService).updateProduct(eq(productId), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void archiveProductDelegatesToTheServiceForTheGivenId() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/products/{id}", productId).with(csrf()))
                .andExpect(status().isOk());

        verify(adminProductService).archiveProduct(productId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adjustStockDelegatesToTheServiceForTheGivenId() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/products/{id}/stock", productId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"delta\": 5}"))
                .andExpect(status().isOk());

        verify(adminProductService).adjustStock(eq(productId), any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void adjustStockIsForbiddenForACustomer() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/products/{id}/stock", productId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"delta\": 5}"))
                .andExpect(status().isForbidden());
    }
}
