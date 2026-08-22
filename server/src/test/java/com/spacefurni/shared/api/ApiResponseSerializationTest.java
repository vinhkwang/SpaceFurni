package com.spacefurni.shared.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class ApiResponseSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void successResponseSerializesToSpecShape() {
        ApiResponse<Map<String, String>> response = ApiResponse.success(Map.of("id", "42"));

        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.get("success").asBoolean()).isTrue();
        assertThat(json.get("data").get("id").asString()).isEqualTo("42");
        assertThat(json.get("error").isNull()).isTrue();
    }

    @Test
    void failureResponseSerializesToSpecShape() {
        ApiError error = new ApiError(
                "INSUFFICIENT_STOCK", "Not enough stock", Map.of("productId", "must have available stock"));
        ApiResponse<Void> response = ApiResponse.failure(error);

        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.get("success").asBoolean()).isFalse();
        assertThat(json.get("data").isNull()).isTrue();
        assertThat(json.get("error").get("code").asString()).isEqualTo("INSUFFICIENT_STOCK");
        assertThat(json.get("error").get("message").asString()).isEqualTo("Not enough stock");
        assertThat(json.get("error").get("details").get("productId").asString())
                .isEqualTo("must have available stock");
    }

    @Test
    void pageResponseSerializesAllFields() {
        PageResponse<String> page = new PageResponse<>(List.of("a", "b"), 0, 20, 2L, 1);

        JsonNode json = objectMapper.valueToTree(page);

        assertThat(json.get("content").size()).isEqualTo(2);
        assertThat(json.get("page").asInt()).isEqualTo(0);
        assertThat(json.get("size").asInt()).isEqualTo(20);
        assertThat(json.get("totalElements").asLong()).isEqualTo(2L);
        assertThat(json.get("totalPages").asInt()).isEqualTo(1);
    }
}
