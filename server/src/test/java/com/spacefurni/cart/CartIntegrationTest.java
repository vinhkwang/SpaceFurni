package com.spacefurni.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.support.AbstractIntegrationTest;
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
class CartIntegrationTest extends AbstractIntegrationTest {

    private static final String GUEST_TOKEN_HEADER = "X-Guest-Token";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void guestCartLifecycleThenMergeIntoUserCartOnLogin() throws Exception {
        String sofaId = productIdBySlug("claire-3-seater-sofa");
        String chairId = productIdBySlug("halden-tub-chair");

        JsonNode afterFirstAdd = performAndReadData(post("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"productId\": \"%s\", \"quantity\": 2 }".formatted(sofaId)));
        String guestToken = afterFirstAdd.get("guestToken").asString();
        assertThat(singleLineQuantity(afterFirstAdd, sofaId)).isEqualTo(2);

        JsonNode afterDuplicateAdd = performAndReadData(post("/api/v1/cart/items")
                .header(GUEST_TOKEN_HEADER, guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"productId\": \"%s\", \"quantity\": 1 }".formatted(sofaId)));
        assertThat(afterDuplicateAdd.get("lines")).hasSize(1);
        assertThat(singleLineQuantity(afterDuplicateAdd, sofaId)).isEqualTo(3);

        JsonNode afterQuantityUpdate = performAndReadData(patch("/api/v1/cart/items/" + sofaId)
                .header(GUEST_TOKEN_HEADER, guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"quantity\": 4 }"));
        assertThat(singleLineQuantity(afterQuantityUpdate, sofaId)).isEqualTo(4);

        mockMvc.perform(post("/api/v1/cart/items")
                        .header(GUEST_TOKEN_HEADER, guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"productId\": \"%s\", \"quantity\": 1 }".formatted(sofaId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_STOCK"));

        JsonNode afterRemove = performAndReadData(
                delete("/api/v1/cart/items/" + sofaId).header(GUEST_TOKEN_HEADER, guestToken));
        assertThat(afterRemove.get("lines")).isEmpty();

        performAndReadData(post("/api/v1/cart/items")
                .header(GUEST_TOKEN_HEADER, guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"productId\": \"%s\", \"quantity\": 2 }".formatted(chairId)));

        String email = "merge-flow-user@spacefurni.com";
        JsonNode registerData = performAndReadData(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"email\": \"%s\", \"password\": \"password1\", \"fullName\": \"Merge Flow User\" }"
                        .formatted(email)));
        String accessToken = registerData.get("accessToken").asString();

        performAndReadData(post("/api/v1/cart/items")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"productId\": \"%s\", \"quantity\": 1 }".formatted(chairId)));

        JsonNode afterMerge = performAndReadData(post("/api/v1/cart/merge")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(GUEST_TOKEN_HEADER, guestToken));
        assertThat(singleLineQuantity(afterMerge, chairId)).isEqualTo(3);

        JsonNode afterMergeReload = performAndReadData(
                get("/api/v1/cart").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
        assertThat(singleLineQuantity(afterMergeReload, chairId)).isEqualTo(3);
    }

    private int singleLineQuantity(JsonNode cartData, String productId) {
        for (JsonNode line : cartData.get("lines")) {
            if (line.get("productId").asString().equals(productId)) {
                return line.get("quantity").asInt();
            }
        }
        throw new AssertionError("No line found for product " + productId);
    }

    private String productIdBySlug(String slug) throws Exception {
        return performAndReadData(get("/api/v1/products/" + slug)).get("id").asString();
    }

    private JsonNode performAndReadData(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }
}
