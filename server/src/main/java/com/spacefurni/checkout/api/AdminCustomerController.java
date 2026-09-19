package com.spacefurni.checkout.api;

import com.spacefurni.checkout.api.dto.AdminCustomerDetailResponse;
import com.spacefurni.checkout.api.dto.AdminCustomerListResponse;
import com.spacefurni.checkout.api.dto.AdminCustomerRowResponse;
import com.spacefurni.checkout.application.AdminCustomerQueryService;
import com.spacefurni.checkout.domain.CustomerTier;
import com.spacefurni.shared.api.ApiResponse;
import com.spacefurni.shared.api.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminCustomerQueryService adminCustomerQueryService;

    public AdminCustomerController(AdminCustomerQueryService adminCustomerQueryService) {
        this.adminCustomerQueryService = adminCustomerQueryService;
    }

    @GetMapping
    public ApiResponse<AdminCustomerListResponse> listCustomers(@RequestParam(required = false) CustomerTier tier,
            @RequestParam(required = false) String q, @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Page<AdminCustomerRowResponse> result = adminCustomerQueryService.listCustomers(tier, q,
                PageRequest.of(resolvePage(page), resolvePageSize(size)));
        PageResponse<AdminCustomerRowResponse> pageResponse = new PageResponse<>(result.getContent(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
        return ApiResponse
                .success(new AdminCustomerListResponse(pageResponse, adminCustomerQueryService.countCustomersByTier()));
    }

    @GetMapping("/{customerId}")
    public ApiResponse<AdminCustomerDetailResponse> getCustomerDetail(@PathVariable UUID customerId) {
        return ApiResponse.success(adminCustomerQueryService.findCustomerDetail(customerId));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCustomers(@RequestParam(required = false) CustomerTier tier,
            @RequestParam(required = false) String q) {
        byte[] csv = adminCustomerQueryService.exportCustomersCsv(tier, q);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customers.csv")
                .contentType(MediaType.parseMediaType("text/csv")).body(csv);
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
