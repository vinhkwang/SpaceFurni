package com.spacefurni.catalog.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class CatalogResponseSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void productSummaryResponseSerializesAllFieldsIncludingBadge() {
        ProductBadgeResponse badge = new ProductBadgeResponse("-20%", "SALE");
        ProductSummaryResponse summary = new ProductSummaryResponse(UUID.randomUUID(), "SKU-1", "oslo-sofa",
                "Oslo Sofa", "Sofa", 800_000L, 1_000_000L, "VND", new BigDecimal("4.5"), 12,
                "https://example.com/a.jpg", badge, "Beige", "#8B5E3C");

        JsonNode json = objectMapper.valueToTree(summary);

        assertThat(json.get("slug").asString()).isEqualTo("oslo-sofa");
        assertThat(json.get("priceAmount").asLong()).isEqualTo(800_000L);
        assertThat(json.get("compareAtPriceAmount").asLong()).isEqualTo(1_000_000L);
        assertThat(json.get("currencyCode").asString()).isEqualTo("VND");
        assertThat(json.get("badge").get("label").asString()).isEqualTo("-20%");
        assertThat(json.get("badge").get("variant").asString()).isEqualTo("SALE");
    }

    @Test
    void productSummaryResponseSerializesNullBadgeAndCompareAtPrice() {
        ProductSummaryResponse summary = new ProductSummaryResponse(UUID.randomUUID(), "SKU-1", "oslo-sofa",
                "Oslo Sofa", "Sofa", 800_000L, null, "VND", new BigDecimal("4.5"), 12, "https://example.com/a.jpg",
                null, "Beige", "#8B5E3C");

        JsonNode json = objectMapper.valueToTree(summary);

        assertThat(json.get("compareAtPriceAmount").isNull()).isTrue();
        assertThat(json.get("badge").isNull()).isTrue();
    }

    @Test
    void productDetailResponseSerializesChildCollectionsAndRelatedProducts() {
        ProductSummaryResponse related = new ProductSummaryResponse(UUID.randomUUID(), "SKU-2", "claire-sofa",
                "Claire Sofa", "Sofa", 1_950_000L, null, "VND", new BigDecimal("4.6"), 74,
                "https://example.com/b.jpg", null, "Beige", "#8B5E3C");
        ProductDetailResponse detail = new ProductDetailResponse(UUID.randomUUID(), "SKU-1", "oslo-sofa",
                "Oslo Sofa", "Sofa", 800_000L, 1_000_000L, "VND", new BigDecimal("4.5"), 12, "short", "long",
                "200x90x80cm", "Fabric", "Beige", new ProductBadgeResponse("-20%", "SALE"),
                List.of("https://example.com/a.jpg"),
                List.of(new ProductDetailResponse.SpecificationEntry("Material", "Fabric")), List.of("#8B5E3C"), 3,
                "Only 3 left", List.of(related));

        JsonNode json = objectMapper.valueToTree(detail);

        assertThat(json.get("imageUrls").get(0).asString()).isEqualTo("https://example.com/a.jpg");
        assertThat(json.get("specifications").get(0).get("key").asString()).isEqualTo("Material");
        assertThat(json.get("specifications").get(0).get("value").asString()).isEqualTo("Fabric");
        assertThat(json.get("colorSwatchHexCodes").get(0).asString()).isEqualTo("#8B5E3C");
        assertThat(json.get("relatedProducts").get(0).get("slug").asString()).isEqualTo("claire-sofa");
        assertThat(json.get("availableQuantity").asInt()).isEqualTo(3);
        assertThat(json.get("stockLabel").asString()).isEqualTo("Only 3 left");
    }

    @Test
    void categoryTreeResponseSerializesNestedSubCategories() {
        CategoryTreeResponse subCategory = new CategoryTreeResponse(UUID.randomUUID(), "Sofa", "sofa", null, 8L,
                List.of());
        CategoryTreeResponse department = new CategoryTreeResponse(UUID.randomUUID(), "Living room", "living-room",
                "https://example.com/room.jpg", 8L, List.of(subCategory));

        JsonNode json = objectMapper.valueToTree(department);

        assertThat(json.get("slug").asString()).isEqualTo("living-room");
        assertThat(json.get("subCategories").size()).isEqualTo(1);
        assertThat(json.get("subCategories").get(0).get("slug").asString()).isEqualTo("sofa");
        assertThat(json.get("subCategories").get(0).get("productCount").asLong()).isEqualTo(8L);
    }
}
