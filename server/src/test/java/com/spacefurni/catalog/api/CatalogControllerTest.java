package com.spacefurni.catalog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spacefurni.catalog.api.dto.CategoryTreeResponse;
import com.spacefurni.catalog.api.dto.ProductDetailResponse;
import com.spacefurni.catalog.application.CatalogQueryService;
import com.spacefurni.catalog.application.ProductFilter;
import com.spacefurni.catalog.application.ProductSortOption;
import com.spacefurni.identity.security.JwtTokenProvider;
import com.spacefurni.identity.security.SecurityConfiguration;
import com.spacefurni.identity.security.SpaceFurniUserDetailsService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = CatalogController.class)
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
class CatalogControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvcWithSecurity() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @MockitoBean
    private CatalogQueryService catalogQueryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SpaceFurniUserDetailsService userDetailsService;

    @Test
    void categoryTreeReturnsWrappedList() throws Exception {
        when(catalogQueryService.findCategoryTree())
                .thenReturn(List.of(new CategoryTreeResponse(UUID.randomUUID(), "Living room", "living-room", null,
                        8L, List.of())));

        mockMvc.perform(get("/api/v1/categories")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].slug").value("living-room"));
    }

    @Test
    void listProductsPassesFilterAndCappedPageSizeToService() throws Exception {
        when(catalogQueryService.findPublishedProducts(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/products").param("categorySlug", "living-room")
                        .param("subCategorySlug", "sofa").param("minPrice", "1000000").param("maxPrice", "5000000")
                        .param("sort", "priceAsc").param("size", "1000"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<ProductFilter> filterCaptor = ArgumentCaptor.forClass(ProductFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(catalogQueryService).findPublishedProducts(filterCaptor.capture(), pageableCaptor.capture());
        ProductFilter capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter.departmentSlug()).isEqualTo("living-room");
        assertThat(capturedFilter.subCategorySlug()).isEqualTo("sofa");
        assertThat(capturedFilter.minPriceAmount()).isEqualTo(1000000L);
        assertThat(capturedFilter.maxPriceAmount()).isEqualTo(5000000L);
        assertThat(capturedFilter.sort()).isEqualTo(ProductSortOption.PRICE_ASC);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(48);
    }

    @Test
    void listProductsRejectsUnknownSortKey() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("sort", "bogus")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void productDetailReturnsWrappedDetail() throws Exception {
        ProductDetailResponse detail = new ProductDetailResponse(UUID.randomUUID(), "SKU-1", "oslo-sofa",
                "Oslo Sofa", "Sofa", 800_000L, null, "VND", new BigDecimal("4.5"), 12, "short", "long", "dims",
                "material", "color", null, List.of(), List.of(), List.of(), List.of());
        when(catalogQueryService.findProductDetailBySlug("oslo-sofa")).thenReturn(detail);

        mockMvc.perform(get("/api/v1/products/oslo-sofa")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("oslo-sofa"));
    }

    @Test
    void relatedProductsRequestsThreeFromService() throws Exception {
        when(catalogQueryService.findRelatedProducts(eq("oslo-sofa"), eq(3))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products/oslo-sofa/related")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(catalogQueryService).findRelatedProducts("oslo-sofa", 3);
    }

    @Test
    void searchSuggestionsRequestsFiveFromService() throws Exception {
        when(catalogQueryService.suggestProducts(eq("sofa"), eq(5))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products/search").param("q", "sofa")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(catalogQueryService).suggestProducts("sofa", 5);
    }
}
