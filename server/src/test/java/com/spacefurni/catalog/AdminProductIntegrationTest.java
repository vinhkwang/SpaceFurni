package com.spacefurni.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.inventory.infrastructure.InventoryItemRepository;
import com.spacefurni.support.AbstractIntegrationTest;
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
class AdminProductIntegrationTest extends AbstractIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@spacefurni.dev";
    private static final String ADMIN_PASSWORD = "DevAdmin123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminAccessToken() throws Exception {
        JsonNode data = performAndReadData(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(("{ \"email\": \"%s\", \"password\": \"%s\" }").formatted(ADMIN_EMAIL, ADMIN_PASSWORD)));
        return data.get("accessToken").asString();
    }

    private String customerAccessToken() throws Exception {
        String email = "admin-products-customer-" + UUID.randomUUID() + "@example.com";
        JsonNode data = performAndReadData(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(("{ \"email\": \"%s\", \"password\": \"password1\", \"fullName\": \"Customer Tester\" }")
                        .formatted(email)));
        return data.get("accessToken").asString();
    }

    private String productRequestBody(String title, long price, int stock, String status, Long version) {
        return ("{ \"title\": \"%s\", \"departmentSlug\": \"living-room\", \"subCategorySlug\": \"sofa\", "
                + "\"price\": %d, \"stock\": %d, \"status\": \"%s\", \"version\": %s }").formatted(title, price,
                stock, status, version == null ? "null" : version);
    }

    private UUID createProduct(String adminToken, String title, long price, int stock, String status)
            throws Exception {
        JsonNode data = performAndReadData(post("/api/v1/admin/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON)
                .content(productRequestBody(title, price, stock, status, null)));
        return UUID.fromString(data.asString());
    }

    @Test
    void creatingAProductProvisionsItsInventoryRow() throws Exception {
        String adminToken = adminAccessToken();

        UUID productId = createProduct(adminToken, "Zzq Integration Provisioning Sofa", 3_000_000L, 15, "DRAFT");

        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(15);
    }

    @Test
    void duplicateTitlesGetDistinctSlugs() throws Exception {
        String adminToken = adminAccessToken();

        UUID firstId = createProduct(adminToken, "Zzq Integration Duplicate Sofa", 3_000_000L, 5, "DRAFT");
        UUID secondId = createProduct(adminToken, "Zzq Integration Duplicate Sofa", 3_000_000L, 5, "DRAFT");

        String firstSlug = productRepository.findById(firstId).orElseThrow().getSlug();
        String secondSlug = productRepository.findById(secondId).orElseThrow().getSlug();
        assertThat(firstSlug).isNotEqualTo(secondSlug);
    }

    @Test
    void updatingAProductChangesFieldsAndAStaleVersionReturns409() throws Exception {
        String adminToken = adminAccessToken();
        UUID productId = createProduct(adminToken, "Zzq Integration Update Sofa", 3_000_000L, 5, "DRAFT");
        long currentVersion = productRepository.findById(productId).orElseThrow().getVersion();

        mockMvc.perform(put("/api/v1/admin/products/{id}", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestBody("Zzq Integration Updated Sofa Name", 4_500_000L, 5, "DRAFT",
                                currentVersion)))
                .andExpect(status().isOk());

        Product updated = productRepository.findById(productId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Zzq Integration Updated Sofa Name");
        assertThat(updated.getPrice().amount()).isEqualTo(4_500_000L);

        mockMvc.perform(put("/api/v1/admin/products/{id}", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestBody("Zzq Integration Stale Update Sofa", 5_000_000L, 5, "DRAFT",
                                currentVersion)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONCURRENT_MODIFICATION"));
    }

    @Test
    void stockAdjustmentMovesStockAndRejectsAnOverDecrement() throws Exception {
        String adminToken = adminAccessToken();
        UUID productId = createProduct(adminToken, "Zzq Integration Stock Adjustment Sofa", 3_000_000L, 10, "DRAFT");

        mockMvc.perform(patch("/api/v1/admin/products/{id}/stock", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{ \"delta\": 5 }"))
                .andExpect(status().isOk());
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(15);

        mockMvc.perform(patch("/api/v1/admin/products/{id}/stock", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{ \"delta\": -50 }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_STOCK"));
        assertThat(inventoryItemRepository.findById(productId).orElseThrow().getQuantityOnHand()).isEqualTo(15);
    }

    @Test
    void archivingHidesAProductFromThePublicCatalogueButKeepsItInTheAdminListing() throws Exception {
        String adminToken = adminAccessToken();
        UUID productId = createProduct(adminToken, "Zzq Integration Archive Sofa", 3_000_000L, 5, "PUBLISHED");
        String slug = productRepository.findById(productId).orElseThrow().getSlug();

        mockMvc.perform(get("/api/v1/products/{slug}", slug)).andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/admin/products/{id}", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/products/{slug}", slug)).andExpect(status().isNotFound());

        JsonNode adminListing = performAndReadData(get("/api/v1/admin/products")
                .param("q", "Zzq Integration Archive Sofa")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken));
        assertThat(adminListing.get("content").get(0).get("status").asString()).isEqualTo("ARCHIVED");
    }

    @Test
    void aCustomerTokenGets403OnEveryAdminProductEndpoint() throws Exception {
        String adminToken = adminAccessToken();
        String customerToken = customerAccessToken();
        UUID productId = createProduct(adminToken, "Zzq Integration Customer 403 Sofa", 3_000_000L, 5, "DRAFT");

        mockMvc.perform(get("/api/v1/admin/products").header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/products").header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestBody("Zzq Integration Forbidden Sofa", 1_000_000L, 1, "DRAFT", null)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/admin/products/{id}", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestBody("Zzq Integration Forbidden Sofa", 1_000_000L, 1, "DRAFT", 0L)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/admin/products/{id}", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/admin/products/{id}/stock", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{ \"delta\": 1 }"))
                .andExpect(status().isForbidden());
    }

    private JsonNode performAndReadData(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }
}
