package com.spacefurni.catalog.api;

import com.spacefurni.catalog.api.dto.AdminProductDetailResponse;
import com.spacefurni.catalog.api.dto.AdminProductRequest;
import com.spacefurni.catalog.api.dto.AdminProductRowResponse;
import com.spacefurni.catalog.api.dto.StockAdjustmentRequest;
import com.spacefurni.catalog.application.AdminProductService;
import com.spacefurni.shared.api.ApiResponse;
import com.spacefurni.shared.api.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminProductRowResponse>> listProducts(
            @RequestParam(required = false) String q, @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Page<AdminProductRowResponse> result =
                adminProductService.listProducts(q, PageRequest.of(resolvePage(page), resolvePageSize(size)));
        return ApiResponse.success(new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages()));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminProductDetailResponse> getProduct(@PathVariable UUID id) {
        return ApiResponse.success(adminProductService.getProduct(id));
    }

    @PostMapping
    public ApiResponse<UUID> createProduct(@Valid @RequestBody AdminProductRequest request) {
        return ApiResponse.success(adminProductService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateProduct(@PathVariable UUID id, @Valid @RequestBody AdminProductRequest request) {
        adminProductService.updateProduct(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> archiveProduct(@PathVariable UUID id) {
        adminProductService.archiveProduct(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/stock")
    public ApiResponse<Void> adjustStock(@PathVariable UUID id, @Valid @RequestBody StockAdjustmentRequest request) {
        adminProductService.adjustStock(id, request);
        return ApiResponse.success(null);
    }

    private int resolvePage(Integer requestedPage) {
        return requestedPage == null ? 0 : requestedPage;
    }

    private int resolvePageSize(Integer requestedSize) {
        if (requestedSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.max(1, Math.min(requestedSize, MAX_PAGE_SIZE));
    }
}
