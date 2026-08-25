package com.spacefurni.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class CatalogIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void filtersByDepartment() throws Exception {
        JsonNode data = performAndReadData(get("/api/v1/products").param("categorySlug", "living-room")
                .param("size", "48"));

        assertThat(names(data.get("content"))).containsExactlyInAnyOrder("Cloud 3-Seater Sofa",
                "Claire 3-Seater Sofa", "Axis Round Coffee Table", "Anita Wall Shelf", "Halden Tub Chair");
    }

    @Test
    void filtersBySubCategory() throws Exception {
        JsonNode data = performAndReadData(get("/api/v1/products").param("subCategorySlug", "sofa")
                .param("size", "48"));

        assertThat(names(data.get("content"))).containsExactlyInAnyOrder("Cloud 3-Seater Sofa",
                "Claire 3-Seater Sofa");
    }

    @Test
    void filtersByPriceRange() throws Exception {
        JsonNode data = performAndReadData(get("/api/v1/products").param("minPrice", "5000000")
                .param("maxPrice", "8000000").param("size", "48"));

        assertThat(names(data.get("content"))).containsExactlyInAnyOrder("Axis Round Coffee Table",
                "Halden Tub Chair", "Rowan Kitchen Trolley");
    }

    @Test
    void sortsByPriceAscending() throws Exception {
        JsonNode data = performAndReadData(get("/api/v1/products").param("sort", "priceAsc").param("size", "48"));

        assertThat(names(data.get("content"))).containsExactly("Anita Wall Shelf", "Spindle Bedside Table",
                "Rowan Kitchen Trolley", "Axis Round Coffee Table", "Halden Tub Chair", "Meridian Writing Desk",
                "Claire 3-Seater Sofa", "Cloud 3-Seater Sofa");
    }

    @Test
    void sortsByPriceDescending() throws Exception {
        JsonNode data = performAndReadData(get("/api/v1/products").param("sort", "priceDesc").param("size", "48"));

        assertThat(names(data.get("content"))).containsExactly("Cloud 3-Seater Sofa", "Claire 3-Seater Sofa",
                "Meridian Writing Desk", "Halden Tub Chair", "Axis Round Coffee Table", "Rowan Kitchen Trolley",
                "Spindle Bedside Table", "Anita Wall Shelf");
    }

    @Test
    void sortsByRatingNonIncreasing() throws Exception {
        JsonNode data = performAndReadData(get("/api/v1/products").param("sort", "rating").param("size", "48"));

        List<Double> ratings = data.get("content").valueStream().map(node -> node.get("ratingAverage").asDouble())
                .toList();
        assertThat(ratings).isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Test
    void sortsByNewestWithoutErrorAndReturnsAllPublished() throws Exception {
        JsonNode data = performAndReadData(get("/api/v1/products").param("sort", "newest").param("size", "48"));

        assertThat(data.get("totalElements").asLong()).isEqualTo(8L);
    }

    @Test
    void pagingCapsSizeAtFortyEightAndReportsTotals() throws Exception {
        JsonNode data = performAndReadData(get("/api/v1/products").param("size", "1000"));

        assertThat(data.get("size").asInt()).isEqualTo(48);
        assertThat(data.get("totalElements").asLong()).isEqualTo(8L);
        assertThat(data.get("content").size()).isEqualTo(8);
    }

    @Test
    void pagingRespectsExplicitPageAndSize() throws Exception {
        JsonNode data = performAndReadData(get("/api/v1/products").param("size", "3").param("page", "0")
                .param("sort", "priceAsc"));

        assertThat(data.get("content").size()).isEqualTo(3);
        assertThat(data.get("totalPages").asInt()).isEqualTo(3);
        assertThat(names(data.get("content"))).containsExactly("Anita Wall Shelf", "Spindle Bedside Table",
                "Rowan Kitchen Trolley");
    }

    @Test
    void detailBySlugReturnsFullPayloadWithBadgeAndRelatedProducts() throws Exception {
        JsonNode data = performAndReadData(get("/api/v1/products/cloud-3-seater-sofa"));

        assertThat(data.get("sku").asString()).isEqualTo("SF-CLOUD-SOFA");
        assertThat(data.get("imageUrls").size()).isEqualTo(1);
        assertThat(data.get("specifications").size()).isEqualTo(3);
        assertThat(data.get("colorSwatchHexCodes").size()).isEqualTo(3);
        assertThat(data.get("badge").get("variant").asString()).isEqualTo("NEW");
        assertThat(names(data.get("relatedProducts"))).containsExactly("Claire 3-Seater Sofa");
    }

    @Test
    void detailByUnknownSlugReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/products/does-not-exist-anywhere")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void badgesAreCorrectAcrossTheWholeCatalogue() throws Exception {
        JsonNode data = performAndReadData(get("/api/v1/products").param("size", "48"));

        for (JsonNode product : data.get("content")) {
            String name = product.get("name").asString();
            JsonNode badge = product.get("badge");
            String variant = badge.isNull() ? null : badge.get("variant").asString();
            String expected = switch (name) {
                case "Cloud 3-Seater Sofa", "Halden Tub Chair", "Spindle Bedside Table" -> "NEW";
                case "Claire 3-Seater Sofa", "Rowan Kitchen Trolley" -> "SALE";
                case "Axis Round Coffee Table" -> "BESTSELLER";
                case "Anita Wall Shelf", "Meridian Writing Desk" -> null;
                default -> throw new IllegalStateException("Unexpected seeded product: " + name);
            };
            assertThat(variant).as("badge variant for %s", name).isEqualTo(expected);
        }
    }

    @Test
    void listEndpointIssuesOneStatementNotOnePerRow() throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mockMvc.perform(get("/api/v1/products").param("size", "48")).andExpect(status().isOk());

        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2);
    }

    private List<String> names(JsonNode content) {
        return content.valueStream().map(node -> node.get("name").asString()).toList();
    }

    private JsonNode performAndReadData(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }
}
