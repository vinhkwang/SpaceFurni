package com.spacefurni.catalog.api;

import com.spacefurni.catalog.api.dto.CategoryTreeResponse;
import com.spacefurni.catalog.api.dto.ProductDetailResponse;
import com.spacefurni.catalog.api.dto.ProductFilterRequest;
import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.application.CatalogQueryService;
import com.spacefurni.catalog.application.ProductFilter;
import com.spacefurni.catalog.application.ProductSortOption;
import com.spacefurni.shared.api.ApiResponse;
import com.spacefurni.shared.api.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private static final int DEFAULT_PAGE_SIZE = 24;
    private static final int MAX_PAGE_SIZE = 48;
    private static final int RELATED_PRODUCTS_LIMIT = 3;
    private static final int SEARCH_SUGGESTIONS_LIMIT = 5;

    private final CatalogQueryService catalogQueryService;

    public CatalogController(CatalogQueryService catalogQueryService) {
        this.catalogQueryService = catalogQueryService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryTreeResponse>> categoryTree() {
        return ApiResponse.success(catalogQueryService.findCategoryTree());
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductSummaryResponse>> listProducts(
            @ModelAttribute ProductFilterRequest filterRequest) {
        ProductFilter filter = new ProductFilter(filterRequest.categorySlug(), filterRequest.subCategorySlug(),
                filterRequest.minPrice(), filterRequest.maxPrice(), resolveSortOption(filterRequest.sort()));
        int page = filterRequest.page() == null ? 0 : filterRequest.page();
        int size = resolvePageSize(filterRequest.size());
        Page<ProductSummaryResponse> result = catalogQueryService.findPublishedProducts(filter,
                PageRequest.of(page, size));
        return ApiResponse.success(new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages()));
    }

    @GetMapping("/products/{slug}")
    public ApiResponse<ProductDetailResponse> productDetail(@PathVariable String slug) {
        return ApiResponse.success(catalogQueryService.findProductDetailBySlug(slug));
    }

    @GetMapping("/products/{slug}/related")
    public ApiResponse<List<ProductSummaryResponse>> relatedProducts(@PathVariable String slug) {
        return ApiResponse.success(catalogQueryService.findRelatedProducts(slug, RELATED_PRODUCTS_LIMIT));
    }

    @GetMapping("/products/search")
    public ApiResponse<List<ProductSummaryResponse>> searchSuggestions(@RequestParam String q) {
        return ApiResponse.success(catalogQueryService.suggestProducts(q, SEARCH_SUGGESTIONS_LIMIT));
    }

    private ProductSortOption resolveSortOption(String rawSort) {
        if (rawSort == null || rawSort.isBlank()) {
            return ProductSortOption.NEWEST;
        }
        return switch (rawSort) {
            case "newest" -> ProductSortOption.NEWEST;
            case "priceAsc" -> ProductSortOption.PRICE_ASC;
            case "priceDesc" -> ProductSortOption.PRICE_DESC;
            case "rating" -> ProductSortOption.RATING;
            default -> throw new InvalidSortKeyException("Unknown sort key: " + rawSort);
        };
    }

    private int resolvePageSize(Integer requestedSize) {
        if (requestedSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.max(1, Math.min(requestedSize, MAX_PAGE_SIZE));
    }
}
